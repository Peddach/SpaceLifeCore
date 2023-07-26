package de.petropia.spacelifeCore.player;

import dev.morphia.annotations.Entity;

@Entity
public class JobStats {
    private final String id;
    private int level;
    private double currentMoney;
    private double allTimeMoney;

    public JobStats(String id){
        this.id = id;
    }

    /**
     * Add money to the current and allTimeMoney
     * @param amount as double
     */
    public void addMoney(int amount){
        currentMoney += amount;
        allTimeMoney += amount;
    }

    /**
     * Add a level
     */
    public void levelUp(){
        level++;
    }

    /**
     * Get the Level to the id
     * @return level as int
     */
    public int getLevel(){
        return level;
    }

    /**
     * Get the current not already pay out money
     * @return money as double
     */
    public double getCurrentMoney() {
        return currentMoney;
    }

    /**
     * Get the allTimeMoney, seen as a sort of xp for the Job level
     */
    public double getAllTimeMoney() {
        return allTimeMoney;
    }

    /**
     * Reset the current money after a pay-out
     */
    public void resetCurrentMoney(){
        currentMoney = 0;
    }

    /**
     * Get the JobID
     * @return JobID as String
     */
    public String getId() {
        return id;
    }
}
