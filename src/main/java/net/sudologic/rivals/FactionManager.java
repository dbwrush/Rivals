package net.sudologic.rivals;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class FactionManager implements ConfigurationSerializable {
    private Map<UUID, Faction> factions;
    private List<Invite> invites;

    public FactionManager(Map<String, Object> serializedFactionManager) {
        factions = new HashMap<>();
        invites = new ArrayList<>();
        List<Object> fObjects = (List<Object>) serializedFactionManager.get("factions");
        for(Object o : fObjects) {
            Faction f = new Faction((Map<String, Object>) o);
            factions.put(f.getID(), f);
        }
        List<Object> iObjects = (List<Object>) serializedFactionManager.get("invites");
        for(Object o : iObjects) {
            Invite i = new Invite((Map<String, Object>) o);
            invites.add(i);
        }
    }

    public FactionManager() {
        factions = new HashMap<>();
        invites = new ArrayList<>();
    }

    public void addFaction(Faction f) {
        if(!factions.containsKey(f.getID())) {
            factions.put(f.getID(), f);
        }
    }

    public void removeFaction(Faction f) {
        if(factions.containsKey(f.getID())) {
            factions.remove(f.getID());
        }
    }

    public Faction getFactionByID(UUID uuid) {
        return factions.get(uuid);
    }

    public Faction getFactionByPlayer(UUID id) {
        for(Faction f : factions.values()) {
            if(f.getMembers().contains(id)) {
                return f;
            }
        }
        return null;
    }

    public void addInvite(UUID id, UUID f) {
        invites.add(new Invite(f, id));
    }

    public void removeInvite(UUID id, UUID f) {
        Invite s = null;
        for(Invite i : invites) {
            if(i.getFaction() == f && i.getPlayer() == id) {
                s = i;
            }
        }
        if(s != null) {
            invites.remove(s);
        }
    }

    public List<UUID> getInvitesForPlayer(UUID pId) {
        List list = new ArrayList();
        for(Invite i : invites) {
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
        for(Invite i : invites) {
            iObjects.add(i.serialize());
        }
        mapSerializer.put("factions", fObjects);
        mapSerializer.put("invites", iObjects);

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

    public class Invite implements ConfigurationSerializable{
        private UUID faction;
        private UUID player;

        public Invite(Map<String, Object> serialized) {
            this.faction = (UUID) serialized.get("faction");
            this.player = (UUID) serialized.get("player");
        }
        public Invite(UUID faction, UUID id) {
            this.faction = faction;
            this.player = id;
        }

        public UUID getFaction() {
            return faction;
        }

        public UUID getPlayer() {
            return player;
        }

        @Override
        public Map<String, Object> serialize() {
            HashMap<String, Object> mapSerializer = new HashMap<>();

            mapSerializer.put("player", player.toString());
            mapSerializer.put("faction", faction.toString());

            return mapSerializer;
        }
    }
}
