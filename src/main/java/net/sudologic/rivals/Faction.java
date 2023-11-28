package net.sudologic.rivals;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class Faction implements ConfigurationSerializable {
    private UUID factionID;
    private String factionName;
    private List<UUID> enemyFactions;
    private List<UUID> allyFactions;
    private List<UUID> members;
    private double power;

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> mapSerializer = new HashMap<>();

        mapSerializer.put("factionID", factionID.toString());
        mapSerializer.put("factionName", factionName.toString());
        mapSerializer.put("enemyFactions", enemyFactions.toString());
        mapSerializer.put("allyFactions", allyFactions.toString());
        mapSerializer.put("members", members.toString());
        mapSerializer.put("power", power);

        return mapSerializer;
    }

    public Faction(Map<String, Object> serializedFaction) {
        this.factionID = (UUID) serializedFaction.get("factionID");
        this.factionName = (String) serializedFaction.get("factionName");
        this.enemyFactions = (List<UUID>) serializedFaction.get("enemyFactions");
        this.allyFactions = (List<UUID>) serializedFaction.get("allyFactions");
        this.members = (List<UUID>) serializedFaction.get("members");
        this.power = (double) serializedFaction.get("power");
    }

    public Faction(UUID firstPlayer, String name) {
        factionID = UUID.randomUUID();
        while(Rivals.getFactionManager().getFactionByID(factionID) != null) {
            factionID = UUID.randomUUID();
        }
        factionName = name;
        enemyFactions = new ArrayList<>();
        allyFactions = new ArrayList<>();
        members = new ArrayList<>();
        members.add(firstPlayer);
        power = 10;
    }

    public void addMember(UUID member) {
        if(!members.contains(member)) {
            members.add(member);
        }
    }

    public void removeMember(UUID member) {
        if(members.contains(member)) {
            members.remove(member);
        }
    }

    public void addAlly(UUID allyID, boolean recur) {
        if(!allyFactions.contains(allyID)) {
            allyFactions.add(allyID);
            if(recur) {
                Rivals.getFactionManager().getFactionByID(allyID).addAlly(factionID, false);
            }
        }
    }

    public void removeAlly(UUID allyID, boolean recur) {
        if(allyFactions.contains(allyID)) {
            allyFactions.remove(allyID);
            if(recur) {
                Rivals.getFactionManager().getFactionByID(allyID).removeAlly(factionID, false);
            }
        }
    }

    public void addEnemy(UUID enemyID, boolean recur) {
        if(!enemyFactions.contains(enemyID)) {
            enemyFactions.add(enemyID);
            if(recur) {
                Rivals.getFactionManager().getFactionByID(enemyID).addEnemy(factionID, false);
            }
        }
    }

    public void removeEnemy(UUID enemyID, boolean recur) {
        if(enemyFactions.contains(enemyID)) {
            enemyFactions.remove(enemyID);
            if(recur) {
                Rivals.getFactionManager().getFactionByID(enemyID).removeEnemy(factionID, false);
            }
        }
    }

    public void powerChange(double amount) {
        this.power += (amount / members.size());
    }

    public List<UUID> getMembers() {
        return members;
    }

    public UUID getID() {
        return factionID;
    }

    public String getName() {
        return factionName;
    }
}
