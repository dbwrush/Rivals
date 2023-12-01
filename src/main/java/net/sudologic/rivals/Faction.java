package net.sudologic.rivals;

import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class Faction implements ConfigurationSerializable {
    private int factionID;
    private String factionName;
    private List<Integer> enemyFactions;
    private List<Integer> allyFactions;
    private List<UUID> members;
    private double power;

    private List<String> regions;

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> mapSerializer = new HashMap<>();

        mapSerializer.put("factionID", factionID);
        mapSerializer.put("factionName", factionName.toString());
        mapSerializer.put("enemyFactions", enemyFactions);
        mapSerializer.put("allyFactions", allyFactions);
        List<String> memberStrings = new ArrayList<>();
        for(UUID uuid : members) {
            memberStrings.add(uuid.toString());
        }
        mapSerializer.put("members", memberStrings);
        mapSerializer.put("power", power);
        mapSerializer.put("regions", regions);

        return mapSerializer;
    }

    public Faction(Map<String, Object> serializedFaction) {
        this.factionID = (int) serializedFaction.get("factionID");//(UUID) serializedFaction.get("factionID");
        this.factionName = (String) serializedFaction.get("factionName");
        List<String> enemyStrings = (List<String>) serializedFaction.get("enemyFactions");
        this.enemyFactions = new ArrayList<>();
        for(String s : enemyStrings) {
            enemyFactions.add(Integer.valueOf(s));
        }
        List<String> allyStrings = (List<String>) serializedFaction.get("allyFactions");
        this.allyFactions = new ArrayList<>();
        for(String s : allyStrings) {
            allyFactions.add(Integer.valueOf(s));
        }
        List<String> memberStrings = (List<String>) serializedFaction.get("members");
        this.members = new ArrayList<>();
        for(String s : memberStrings) {
            members.add(UUID.fromString(s));
        }
        this.power = (double) serializedFaction.get("power");
        this.regions = (List<String>) serializedFaction.get("regions");
    }

    public Faction(UUID firstPlayer, String name, int id) {
        factionID = id;
        factionName = name;
        enemyFactions = new ArrayList<>();
        allyFactions = new ArrayList<>();
        members = new ArrayList<>();
        members.add(firstPlayer);
        power = 10;
        regions = new ArrayList<>();
    }

    public double getPower() {
        return power;
    }

    public boolean addMember(UUID member) {
        if(!members.contains(member)) {
            sendMessageToOnlineMembers(Bukkit.getPlayer(member).getName() + " has joined your faction.");
            members.add(member);
            Rivals.getClaimManager().updateFactionMembers(this);
            return true;
        }
        return false;
    }

    public boolean removeMember(UUID member) {
        if(members.contains(member)) {
            members.remove(member);
            Rivals.getClaimManager().updateFactionMembers(this);
            sendMessageToOnlineMembers(Bukkit.getPlayer(member).getName() + " has left your faction.");
            return true;
        }
        return false;
    }

    private boolean addAlly(int allyID, boolean recur) {
        if(!allyFactions.contains(allyID)) {
            allyFactions.add(allyID);
            sendMessageToOnlineMembers("You are now allied with " + Rivals.getFactionManager().getFactionByID(allyID).getName());
            if(recur) {
                Rivals.getFactionManager().getFactionByID(allyID).addAlly(factionID, true);
            }
            return true;
        }
        return false;
    }

    public boolean addAlly(int allyID) {
        if(!allyFactions.contains(allyID)) {
            addAlly(allyID, false);
            return true;
        }
        return false;
    }

    private boolean removeAlly(int allyID, boolean recur) {
        if(allyFactions.contains(allyID)) {
            allyFactions.remove(allyID);
            sendMessageToOnlineMembers("You are no longer at allied with " + Rivals.getFactionManager().getFactionByID(allyID).getName());
            if(recur) {
                Rivals.getFactionManager().getFactionByID(allyID).removeAlly(factionID, false);
            }
            return true;
        }
        return false;
    }

    public boolean removeAlly(int allyID) {
        if(allyFactions.contains(allyID)) {
            removeAlly(allyID, true);
            return true;
        }
        return false;
    }

    private boolean addEnemy(int enemyID, boolean recur) {
        if(!enemyFactions.contains(enemyID)) {
            enemyFactions.add(enemyID);
            sendMessageToOnlineMembers("You are now war with " + Rivals.getFactionManager().getFactionByID(enemyID).getName());
            if(recur) {
                Rivals.getFactionManager().getFactionByID(enemyID).addEnemy(factionID, false);
            }
            return true;
        }
        return false;
    }

    public boolean addEnemy(int enemyID) {
        if(!enemyFactions.contains(enemyID)) {
            addEnemy(enemyID, false);
            return true;
        }
        return false;
    }

    private boolean removeEnemy(int enemyID, boolean recur) {
        if(enemyFactions.contains(enemyID)) {
            enemyFactions.remove(enemyID);
            sendMessageToOnlineMembers("You are no longer at war with " + Rivals.getFactionManager().getFactionByID(enemyID).getName());
            if(recur) {
                Rivals.getFactionManager().getFactionByID(enemyID).removeEnemy(factionID, false);
            }
            return true;
        }
        return false;
    }

    public boolean removeEnemy(int enemyID) {
        if(enemyFactions.contains(enemyID)) {
            removeEnemy(enemyID, false);
            return true;
        }
        return false;
    }

    public void powerChange(double amount) {
        this.power += (amount / members.size());
    }

    public List<UUID> getMembers() {
        return members;
    }

    public List<Integer> getEnemies() {
        return enemyFactions;
    }

    public List<Integer> getAllies() {
        return allyFactions;
    }

    public int getID() {
        return factionID;
    }

    public String getName() {
        return factionName;
    }

    public boolean addClaim(Chunk c) {
        return Rivals.getClaimManager().createClaim(c, this);
    }

    public boolean removeClaim(Chunk c) {
        return Rivals.getClaimManager().removeClaim(c, this);
    }

    public List<String> getRegions() {
        return regions;
    }

    public String getClaimName(Chunk c) {
        return "RFClaims-" + c.getWorld().getName() + "-" + factionID + "-" + c.getX() + "-" + c.getZ();
    }

    public void sendMessageToOnlineMembers(String s) {
        for(UUID id : members) {
            if(Bukkit.getPlayer(id).isOnline()) {
                Bukkit.getPlayer(id).sendMessage("[Rivals] " + s);
            }
        }
    }
}
