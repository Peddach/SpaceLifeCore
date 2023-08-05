package de.petropia.spacelifeCore.teleport;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.teleport.dto.TeleportQueryDTO;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CrossServerMessageListener {

   public static CompletableFuture<CrossServerLocation> getForPlayer(CrossServerLocation location){
       return CompletableFuture.supplyAsync(() -> {
           if(location.getPlayerUUID() == null || location.getPlayerUUID().isEmpty()){
               return null;
           }
           CloudPlayer cloudPlayer = SpacelifeCore.getInstance().getCloudNetAdapter().playerManagerInstance().onlinePlayer(UUID.fromString(location.getPlayerUUID()));
           if (cloudPlayer == null) {
               return null;
           }
           if(cloudPlayer.connectedService() == null){
               return null;
           }
           List<String> groups = new ArrayList<>(cloudPlayer.connectedService().groups());
           if(!groups.contains("SpaceLife")){
               return null;
           }
           ChannelMessage response = ChannelMessage.builder()
                   .channel("spacelife_teleport_query")
                   .message(location.getPlayerUUID())
                   .targetService(cloudPlayer.connectedService().serviceId().name())
                   .build()
                   .sendSingleQuery();
           if(response == null){
               return null;
           }
           DataBuf dataBuf = response.content();
           TeleportQueryDTO playerPositionQueryDTO = new TeleportQueryDTO(
                   dataBuf.readString(),
                   dataBuf.readDouble(),
                   dataBuf.readDouble(),
                   dataBuf.readDouble(),
                   dataBuf.readFloat(),
                   dataBuf.readFloat(),
                   dataBuf.readString()
           );
           if(!playerPositionQueryDTO.result().equalsIgnoreCase("success")){
               return null;
           }
           location.setX(playerPositionQueryDTO.x());
           location.setY(playerPositionQueryDTO.y());
           location.setZ(playerPositionQueryDTO.z());
           location.setYaw(playerPositionQueryDTO.yaw());
           location.setPitch(playerPositionQueryDTO.pitch());
           location.setWorld(playerPositionQueryDTO.world());
           location.setServer(cloudPlayer.connectedService().serviceId().name());
           return location;
       });
   }

   @EventListener
   public void onMessageRecive(ChannelMessageReceiveEvent event){
        if(!event.query()){
            return;
        }
        if(!event.channel().equalsIgnoreCase("spacelife_teleport_query")){
             return;
        }
        UUID uuid = UUID.fromString(event.message());
        Player player = Bukkit.getPlayer(uuid);
        if(player == null){
            TeleportQueryDTO teleportQueryDTO = new TeleportQueryDTO("fail", 0, 0, 0 ,0 ,0, "unkown");
            event.queryResponse(ChannelMessage.buildResponseFor(event.channelMessage())
                    .buffer(DataBuf.empty().writeObject(teleportQueryDTO))
                    .build());
            return;
        }
        Location loc = player.getLocation();
        TeleportQueryDTO teleportQueryDTO = new TeleportQueryDTO(
                "success",
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getPitch(),
                loc.getYaw(),
                loc.getWorld().getName()
        );
        event.queryResponse(ChannelMessage.buildResponseFor(event.channelMessage()).buffer(DataBuf.empty().writeObject(teleportQueryDTO)).build());
   }

}
