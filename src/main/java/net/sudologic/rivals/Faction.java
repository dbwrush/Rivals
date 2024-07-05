package net.sudologic.rivals;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;

public class Faction implements ConfigurationSerializable {
    private int factionID;
    private String factionName;
    private List<Integer> enemyFactions;
    private List<Integer> allyFactions;
    private List<UUID> members;
    private double power;
    private Map<String, Home> homes;
    private ChatColor color;
    private double influence;

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
        mapSerializer.put("color", color.getChar());
        mapSerializer.put("homes", homes);
        mapSerializer.put("influence", influence);

        return mapSerializer;
    }

    public int taxInfluence(float prop) {
        int tax = (int) (influence * prop);
        influence -= tax;
        return tax;
    }

    public void payInfluence() {
        influence += power * 0.1;//(int) (power * 0.1);
    }

    public void addInfluence(int amount) {
        influence += amount;
    }

    public double getInfluence() {
        return influence;
    }

    public Faction(Map<String, Object> serializedFaction) {
        this.factionID = (int) serializedFaction.get("factionID");
        this.factionName = (String) serializedFaction.get("factionName");
        List<Integer> enemyIds = (List<Integer>) serializedFaction.get("enemyFactions");
        this.enemyFactions = new ArrayList<>();
        for(Integer s : enemyIds) {
            enemyFactions.add(s);
        }
        List<Integer> allyIds = (List<Integer>) serializedFaction.get("allyFactions");
        this.allyFactions = new ArrayList<>();
        for(Integer s : allyIds) {
            allyFactions.add(s);
        }
        List<String> memberStrings = (List<String>) serializedFaction.get("members");
        this.members = new ArrayList<>();
        for(String s : memberStrings) {
            members.add(UUID.fromString(s));
        }
        this.power = (double) serializedFaction.get("power");
        this.regions = (List<String>) serializedFaction.get("regions");
        this.color = ChatColor.getByChar((String) serializedFaction.get("color"));
        if(serializedFaction.containsKey("homes")) {
            homes = (Map<String, Home>) serializedFaction.get("homes");
        } else {
            homes = new HashMap<>();
        }
        try{
            influence = (double) serializedFaction.get("influence");
        } catch (Exception e) {
            influence = 0;
        }
    }

    public Faction(UUID firstPlayer, String name, int id) {
        factionID = id;
        factionName = name;
        enemyFactions = new ArrayList<>();
        allyFactions = new ArrayList<>();
        members = new ArrayList<>();
        members.add(firstPlayer);
        power = (double) Rivals.getSettings().get("defaultPower");
        regions = new ArrayList<>();
        color = ChatColor.values()[(int) (Math.random() * ChatColor.values().length)];
        if(color.equals(ChatColor.MAGIC) || color.equals(ChatColor.BLACK)) {
            color = ChatColor.RESET;
        }
        homes = new HashMap<>();
    }

    public Map<String, Home> getHomes() {
        return homes;
    }

    public Home getHome(String s) {
        return homes.get(s);
    }

    public boolean setHome(String s, Location location) {
        if(homes.containsKey(s)) {
            return false;
        }
        if(homes.size() < getMaxHomes()) {
            homes.put(s, new Home(location));
            return true;
        }
        return false;
    }

    public boolean delHome(String s) {
        if(homes.remove(s) != null) {
            return true;
        }
        return false;
    }

    public int getMaxHomes() {
        return (int) Math.round(Math.sqrt(getPower())/2);
    }

    public UUID getLeader() {
        return members.get(0);
    }

    public boolean setLeader(UUID uuid) {
        if(members.contains(uuid)) {
            members.remove(uuid);
            members.add(0, uuid);
            return true;
        }
        return false;
    }

    public void setName(String name) {
        factionName = name;
    }

    public void setColor(ChatColor color) {
        if(color.equals(ChatColor.MAGIC) || color.equals(ChatColor.BLACK)) {
            color = ChatColor.RESET;
        }
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

    public double getPower() {
        try {//shouldn't be necessary but just in case
            if (Rivals.getPoliticsManager().getSanctionedFactions().containsKey(factionID))
                return power / 2;
        } catch (NullPointerException e) {
            return power;
        }
        return power;
    }

    public double remInfluence(double amount) {
        double ret;
        if(amount < influence) {
            influence -= amount;
            ret = amount;
        } else {
            ret = Math.floor(influence);
            influence = influence - ret;
        }
        return ret;
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
            if(members.size() == 0) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage("[Rivals] Faction " + factionName + " has disbanded because all its players have left.");
                }
                Rivals.getFactionManager().removeFaction(this);
                return true;
            }
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
            addAlly(allyID, true);
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
            if(Rivals.getFactionManager().getFactionByID(enemyID) == null) {
                return false;
            }
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
        power += (amount / members.size()) / Math.max(power, 1);
        if(power < 0) {
            power = 0;
        }
        power = Math.round(power * 100.0) / 100.0;
        Rivals.getFactionManager().updateFactionRank(this);
    }

    public void rawPowerChange(double amount) {
        power += amount;
        Rivals.getFactionManager().updateFactionRank(this);
    }

    public List<UUID> getMembers() {
        return members;
    }

    public List<Integer> getEnemies() {
        return enemyFactions;
    }

    public List<Integer> getHostileFactions() {
        List<Integer> l = new ArrayList<>();
        l.addAll(enemyFactions);
        l.addAll(Rivals.getPoliticsManager().getInterventionFactions().keySet());
        l.removeAll(allyFactions);
        return l;
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
        if(Rivals.getClaimManager().createClaim(c, this)) {
            regions.add(getClaimName(c));
            return true;
        }
        return false;
    }

    public boolean removeClaim(Chunk c) {
        if(Rivals.getClaimManager().removeClaim(c, this)) {
            regions.remove(getClaimName(c));
            return true;
        }
        return false;
    }

    public List<String> getRegions() {
        return regions;
    }

    public String getClaimName(Chunk c) {
        return "rfclaims_" + c.getWorld().getName() + "_" + factionID + "_" + c.getX() + "_" + c.getZ();
    }

    public void sendMessageToOnlineMembers(String s) {
        for(UUID id : members) {
            if(Bukkit.getOfflinePlayer(id).isOnline()) {
                Bukkit.getPlayer(id).sendMessage("[Rivals] " + s);
            }
        }
    }

    public class Home implements ConfigurationSerializable{
        private Location location;
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> serialized = new HashMap<>();
            serialized.put("location", location);
            return serialized;
        }

        public Home(Map<String, Object> serialized) {
            this.location = (Location) serialized.get("location");
        }

        public Home(Location location) {
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }
    }
}
