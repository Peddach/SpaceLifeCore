package de.petropia.spacelifeCore.home;

import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import dev.morphia.annotations.Entity;
import org.bukkit.Material;

@Entity
public class Home {

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

    public void setLocation(CrossServerLocation location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

}
