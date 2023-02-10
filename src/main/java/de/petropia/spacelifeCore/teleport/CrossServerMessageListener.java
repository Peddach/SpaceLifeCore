package de.petropia.spacelifeCore.teleport;

import de.dytanic.cloudnet.common.document.gson.JsonDocument;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.channel.ChannelMessage;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.channel.ChannelMessageReceiveEvent;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CrossServerMessageListener {

   public static CompletableFuture<CrossServerLocation> getForPlayer(CrossServerLocation location){
       return CompletableFuture.supplyAsync(() -> {
           if(location.getPlayerUUID() == null || location.getPlayerUUID().isEmpty()){
               return null;
           }
           ICloudPlayer cloudPlayer = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class).getOnlinePlayer(UUID.fromString(location.getPlayerUUID()));
           if (cloudPlayer == null) {
               return null;
           }
           if(cloudPlayer.getConnectedService() == null){
               return null;
           }
           List<String> groups = List.of(cloudPlayer.getConnectedService().getGroups());
           if(!groups.contains("SpaceLife")){
               return null;
           }
           ChannelMessage response = ChannelMessage.builder()
                   .channel("spacelife_teleport_query")
                   .message(location.getPlayerUUID())
                   .targetService(cloudPlayer.getConnectedService().getServiceId().getName())
                   .build()
                   .sendSingleQuery();
           if(response == null){
               return null;
           }
           JsonDocument json = response.getJson();
           if(!json.getString("result").equalsIgnoreCase("success")){
               return null;
           }
           location.setX(json.getDouble("x"));
           location.setY(json.getDouble("y"));
           location.setZ(json.getDouble("z"));
           location.setYaw(json.getFloat("yaw"));
           location.setPitch(json.getFloat("pitch"));
           location.setWorld(json.getString("world"));
           location.setServer(cloudPlayer.getConnectedService().getServiceId().getName());
           return location;
       });
   }

   @EventListener
   public void onMessageRecive(ChannelMessageReceiveEvent event){
        if(event.getMessage() == null || !event.isQuery()){
            return;
        }
        if(!event.getChannel().equalsIgnoreCase("spacelife_teleport_query")){
             return;
        }
        UUID uuid = UUID.fromString(event.getMessage());
        Player player = Bukkit.getPlayer(uuid);
        if(player == null){
            event.setQueryResponse(ChannelMessage.buildResponseFor(event.getChannelMessage())
                    .json(JsonDocument.newDocument("result", "fail"))
                    .build());
            return;
        }
        Location loc = player.getLocation();
        JsonDocument jsonDocument = JsonDocument.newDocument("result", "success")
                .append("x", loc.getX())
                .append("y", loc.getY())
                .append("z", loc.getZ())
                .append("world", loc.getWorld().getName())
                .append("yaw", loc.getYaw())
                .append("pitch", loc.getPitch());
        event.setQueryResponse(ChannelMessage.buildResponseFor(event.getChannelMessage()).json(jsonDocument).build());
   }

}
