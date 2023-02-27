package de.petropia.spacelifeCore.home;

import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import dev.morphia.annotations.Entity;
import org.bukkit.Material;

@Entity
public class Home {

    private String uuid;
    private CrossServerLocation location;
    private String name;
    private String material;
    /**
     * Get the icon material. If not set or found it will return paper
     * @return Icon Material
     */
    public Material getMaterial(){
        if(material == null){
            return Material.PAPER;
        }
        try{
            return Material.valueOf(material);
        } catch (IllegalArgumentException e){
            return Material.PAPER;
        }
    }

    /**
     * Get the name set by the user
     * @return name
     */
    public String getName(){
        return name;
    }

    /**
     * @return Location of home
     */
    public CrossServerLocation getLocation() {
        return location;
    }
    public String getUuid(){
        return uuid;
    }

    public void setLocation(CrossServerLocation location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }
        if (!(that instanceof Home thatHome)) {
            return false;
        }
        if(!thatHome.getUuid().equals(uuid)){
            return false;
        }
        if (!thatHome.getName().equals(name)) {
            return false;
        }
        if (!thatHome.getLocation().equals(location)) {
            return false;
        }
        return thatHome.getMaterial().name().equals(material);
    }
}
