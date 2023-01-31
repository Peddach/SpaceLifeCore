package de.petropia.spacelifeCore.player;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.petropia.spacelifeCore.SpacelifeCore;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.experimental.filters.Filters;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.ApiStatus;

import java.util.Hashtable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SpacelifePlayerDatabase {
    private static SpacelifePlayerDatabase instance;
    private final Hashtable<UUID, SpacelifePlayer> spacelifePlayerCache = new Hashtable<>();
    private Datastore datastore;

    public SpacelifePlayerDatabase(){
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
        MongoClient mongoClient = MongoClients.create("mongodb://" + username + ":" + password + "@" + hostname + ":" + port + "/?authSource=" + database); //connecting
        datastore = Morphia.createDatastore(mongoClient, database);
        datastore.getMapper().map(SpacelifePlayer.class);
        datastore.ensureIndexes();
    }

    /**
     * Save a SpacelifePlayer to db async for internal use
     * @param player player
     */
    @ApiStatus.Internal
    protected void save(final SpacelifePlayer player){
        SpacelifeCore.runAync(() -> datastore.merge(player));
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
            Optional<SpacelifePlayer> playerOptional = datastore.find(SpacelifePlayer.class).filter(Filters.eq("uuid", uuid.toString())).stream().findFirst();
            return playerOptional.orElse(null);
        });
    }

    public static SpacelifePlayerDatabase getInstance() {
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
}
