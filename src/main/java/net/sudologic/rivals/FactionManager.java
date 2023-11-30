package net.sudologic.rivals;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class FactionManager implements ConfigurationSerializable {
    private Map<Integer, Faction> factions;
    private List<MemberInvite> memberInvites;
    private List<AllyInvite> allyInvites;

    public FactionManager(Map<String, Object> serializedFactionManager) {
        factions = new HashMap<>();
        memberInvites = new ArrayList<>();
        List<Object> fObjects = (List<Object>) serializedFactionManager.get("factions");
        for(Object o : fObjects) {
            Faction f = new Faction((Map<String, Object>) o);
            factions.put(f.getID(), f);
        }
        List<Object> iObjects = (List<Object>) serializedFactionManager.get("memberInvites");
        for(Object o : iObjects) {
            MemberInvite i = new MemberInvite((Map<String, Object>) o);
            memberInvites.add(i);
        }
        allyInvites = new ArrayList<>();
        List<Object> aObjects = (List<Object>) serializedFactionManager.get("allyInvites");
        for(Object o : aObjects) {
            AllyInvite a = new AllyInvite((Map<String, Object>) o);
            allyInvites.add(a);
        }
    }
    public int getUnusedFactionID() {
        int m = (int) factions.keySet().toArray()[factions.keySet().size() - 1];
        for(int i = 0; i < m; i++) {
            if(getFactionByID(i) == null) {
                return i;
            }
        }
        return m + 1;
    }

    public FactionManager() {
        factions = new HashMap<>();
        memberInvites = new ArrayList<>();
    }

    public boolean addFaction(Faction f) {
        if(!factions.containsKey(f.getID()) && !nameAlreadyExists(f.getName())) {
            factions.put(f.getID(), f);
            return true;
        }
        return false;
    }

    public boolean removeFaction(Faction f) {
        if(factions.containsKey(f.getID())) {
            factions.remove(f.getID());
            return true;
        }
        return false;
    }

    public Faction getFactionByID(int id) {
        return factions.get(id);
    }

    public Faction getFactionByName(String name) {
        for(Faction f : factions.values()) {
            if(f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    public Faction getFactionByPlayer(UUID id) {
        for(Faction f : factions.values()) {
            if(f.getMembers().contains(id)) {
                return f;
            }
        }
        return null;
    }

    public void addInvite(UUID id, int f) {
        memberInvites.add(new MemberInvite(f, id));
    }

    public void removeInvite(UUID id, int f) {
        MemberInvite s = null;
        for(MemberInvite i : memberInvites) {
            if(i.getFaction() == f && i.getPlayer() == id) {
                s = i;
            }
        }
        if(s != null) {
            memberInvites.remove(s);
        }
    }

    public List<Integer> getInvitesForPlayer(UUID pId) {
        List list = new ArrayList();
        for(MemberInvite i : memberInvites) {
            if(i.getPlayer() == pId)
                list.add(i.getFaction());
        }

        return list;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> mapSerializer = new HashMap<>();

        List<Object> fObjects = new ArrayList<>();
        for(Faction f : factions.values()) {
            fObjects.add(f.serialize());
        }
        List<Object> iObjects = new ArrayList<>();
        for(MemberInvite i : memberInvites) {
            iObjects.add(i.serialize());
        }
        List<Object> aObjects = new ArrayList<>();
        for(AllyInvite a : allyInvites) {
            aObjects.add(a.serialize());
        }
        mapSerializer.put("factions", fObjects);
        mapSerializer.put("memberInvites", iObjects);
        mapSerializer.put("allyInvites", aObjects);
        return mapSerializer;
    }

    public boolean nameAlreadyExists(String name){
        for(Faction f : factions.values()) {
            if(f.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public class MemberInvite implements ConfigurationSerializable{
        private int faction;
        private UUID player;

        public MemberInvite(Map<String, Object> serialized) {
            this.faction = (int) serialized.get("faction");
            this.player = (UUID) serialized.get("player");
        }
        public MemberInvite(int faction, UUID id) {
            this.faction = faction;
            this.player = id;
        }

        public int getFaction() {
            return faction;
        }

        public UUID getPlayer() {
            return player;
        }

        @Override
        public Map<String, Object> serialize() {
            HashMap<String, Object> mapSerializer = new HashMap<>();

            mapSerializer.put("player", player.toString());
            mapSerializer.put("faction", faction);

            return mapSerializer;
        }
    }

    public class AllyInvite implements ConfigurationSerializable{
        private int inviter;
        private int invitee;

        public AllyInvite(Map<String, Object> serialized) {
            this.inviter = (int) serialized.get("inviter");
            this.invitee = (int) serialized.get("invitee");
        }
        public AllyInvite(int inviter, int invitee) {
            this.inviter = inviter;
            this.invitee = invitee;
        }

        public int getInvitee() {
            return invitee;
        }

        public int getInviter() {
            return inviter;
        }

        @Override
        public Map<String, Object> serialize() {
            HashMap<String, Object> mapSerializer = new HashMap<>();

            mapSerializer.put("inviter", inviter);
            mapSerializer.put("invitee", invitee);

            return mapSerializer;
        }
    }
}
