package de.petropia.spacelifeCore.player;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.warp.Warp;
import de.petropia.spacelifeCore.warp.WarpVisit;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.filters.Filters;
import net.kyori.adventure.text.Component;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SpacelifeDatabase {
    private static SpacelifeDatabase instance;
    private List<Warp> warps = new ArrayList<>();
    private final Hashtable<UUID, SpacelifePlayer> spacelifePlayerCache = new Hashtable<>();
    private Datastore datastore;

    public SpacelifeDatabase(){
        if(instance != null){
            return;
        }
        instance = this;
        FileConfiguration configuration = SpacelifeCore.getInstance().getConfig();   //loading credentails from config
        String hostname = ensureNotMissingStr(configuration, "Mongo.Hostname");
        int port = ensureNotMissingInt(configuration, "Mongo.Port");
        String username = ensureNotMissingStr(configuration, "Mongo.User");
        String database = ensureNotMissingStr(configuration, "Mongo.Database");
        String password = ensureNotMissingStr(configuration,"Mongo.Password");
        CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings setting = MongoClientSettings.builder()
                .credential(MongoCredential.createCredential(username, database, password.toCharArray()))
                .applyToClusterSettings(builder -> builder.hosts(List.of(new ServerAddress(hostname, port))))
                .codecRegistry(pojoCodecRegistry)
                .build();
        MongoClient mongoClient = MongoClients.create(setting); //connecting
        datastore = Morphia.createDatastore(mongoClient, database);
        datastore.getMapper().map(SpacelifePlayer.class);
        datastore.getMapper().map(Warp.class);
        datastore.ensureIndexes();
        fetchWarps();
        Bukkit.getScheduler().runTaskTimerAsynchronously(SpacelifeCore.getInstance(), this::fetchWarps, 20*60, 20*60*2);
    }

    /**
     * Save a SpacelifePlayer to db async for internal use
     * @param player player
     * @return CompletableFuture with nothing. Completes when saved
     */
    @ApiStatus.Internal
    protected CompletableFuture<Void> save(final SpacelifePlayer player){
        return CompletableFuture.supplyAsync(() -> {
            datastore.save(player);
            return null;
        });
    }

    public void deleteFromDB(SpacelifePlayer player){
        Player bukkitPlayer = Bukkit.getPlayer(player.getUUID());
        if(bukkitPlayer != null){
            SpacelifePlayerLoadingListener.blockInvSave(bukkitPlayer);
            bukkitPlayer.kick(Component.text("Dein Profil wurde gelöscht!"));
        }
        Bukkit.getScheduler().runTaskAsynchronously(SpacelifeCore.getInstance(), () -> datastore.delete(player));
    }

    /**
     * Load a spacelife player to cache. Used on join event
     * @param player Player to cache
     */
    @ApiStatus.Internal
    protected void cachePlayer(SpacelifePlayer player){
        spacelifePlayerCache.put(player.getUUID(), player);
    }

    /**
     * Unload a spacelifeplayer from cache. Used on quit event
     * @param player player to remove
     */
    @ApiStatus.Internal
    protected void uncachePlayer(SpacelifePlayer player){
        spacelifePlayerCache.remove(player.getUUID());
    }

    /**
     * Get a cached player. Only online players are cached!
     * @param uuid UUID of online player
     * @return SpacelifePlayer or null when not cached/online
     */
    public SpacelifePlayer getCachedPlayer(UUID uuid){
        return spacelifePlayerCache.get(uuid);
    }

    /**
     * Get a SpacelifePlayer from db async
     * @param uuid UUID of player to query
     * @return CompletableFuture with SpacelifePlayer
     */
    public CompletableFuture<SpacelifePlayer> getSpacelifePlayer(UUID uuid){
        return CompletableFuture.supplyAsync(() -> {
            if(spacelifePlayerCache.get(uuid) != null){
                return spacelifePlayerCache.get(uuid);
            }
            Optional<SpacelifePlayer> playerOptional = datastore.find(SpacelifePlayer.class).filter(Filters.eq("uuid", uuid.toString())).stream().findFirst();
            return playerOptional.orElse(null);
        });
    }

    /**
     * Get a warp case-sensitive by its name from caches warps
     * @param name Name of warp
     */
    public Warp getWarp(String name){
        for(Warp warp: warps){
            if(warp.getName().equalsIgnoreCase(name)){
                return warp;
            }
        }
        return null;
    }

    /**
     * Query a warp from the database async
     * @param name case-sensetive name of the warp
     * @return CompletableFuture with warp or null if not found
     */
    public CompletableFuture<Warp> queryWarpFromDB(String name){
        return CompletableFuture.supplyAsync(() -> datastore.find(Warp.class).filter(Filters.eq("name", name)).stream().findFirst().orElse(null));
    }

    /**
     * Fetches all warps from db and refresh warp list. Removes invalid warp and invalid visits
     */
    private void fetchWarps(){
        List<Warp> fetchedWarps = new ArrayList<>(datastore.find(Warp.class).stream().toList());
        List<Warp> invalidWarps = new ArrayList<>();
        fetchedWarps.forEach(warp -> {
            if(warp.getExpireDate().toInstant().getEpochSecond() < Instant.now().getEpochSecond()){
                invalidWarps.add(warp);
                SpacelifeCore.getInstance().getLogger().info("Detected expired warp: " + warp.getName());
                return;
            }
            List<WarpVisit> fetchedWarpVisits = new ArrayList<>(warp.getWarpVisitList());
            List<WarpVisit> invalidVisits = new ArrayList<>();
            fetchedWarpVisits.forEach(warpVisit -> {
                if(warpVisit.getVisitExpireDate().toInstant().getEpochSecond() < Instant.now().getEpochSecond()){
                    invalidVisits.add(warpVisit);
                }
            });
            warp.getWarpVisitList().removeAll(invalidVisits);
            datastore.save(warp);
        });
        fetchedWarps.removeAll(invalidWarps);
        this.warps = fetchedWarps;
        sortVisits();
        if(invalidWarps.size() == 0){
            return;
        }
        for(Warp warp : invalidWarps){
            SpacelifeCore.getInstance().getLogger().info("Deleted Warp: " + warp.getName());
            datastore.delete(warp);
        }
    }

    /**
     * Adds a warp and refetch all warps
     * @param warp {@link Warp} to add
     */
    public void addWarp(Warp warp){
        Bukkit.getScheduler().runTaskAsynchronously(SpacelifeCore.getInstance(), () -> {
            datastore.save(warp);
            fetchWarps();
        });
    }

    /**
     * Delete a warp case-sensitive
     * @param name Name of the warp
     */
    public void removeWarp(String name){
        Bukkit.getScheduler().runTaskAsynchronously(SpacelifeCore.getInstance(), () -> datastore.find(Warp.class).filter(Filters.eq("name", name)).delete().getDeletedCount());
    }

    public List<Warp> getWarps(){
        return warps;
    }

    public static SpacelifeDatabase getInstance() {
        return instance;
    }

    /**
     * Ensure that a config entry is set
     * @param configuration Config
     * @param path Path of value
     * @return String
     */
    private String ensureNotMissingStr(FileConfiguration configuration, String path) {
        String str = configuration.getString(path);
        if(str == null){
            throw new NullPointerException(path + " is not set in config.yml of SpacelifeCore!");
        }
        return str;
    }

    /**
     * Ensure that a config entry is set
     * @param configuration Config
     * @param path Path of value
     * @return Integer
     */
    private int ensureNotMissingInt(FileConfiguration configuration, String path){
        int i = configuration.getInt(path);
        if(i == 0){
            throw new NullPointerException(path + " is not set as a valid int in config.yml of SpacelifeCore!");
        }
        return i;
    }

    private void sortVisits(){
        Comparator<Warp> comparator = Comparator.comparingInt(Warp::getVisits);
        warps.sort(Collections.reverseOrder(comparator));
    }

    public void updateWarp(Warp warp) {
        Bukkit.getScheduler().runTaskAsynchronously(SpacelifeCore.getInstance(),() -> datastore.save(warp));
    }
}
