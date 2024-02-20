package net.sudologic.rivals.resources;

import net.sudologic.rivals.Rivals;
import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class ResourceManager implements ConfigurationSerializable {
    int maxSpawners = 128;
    HashMap<Chunk, ResourceSpawner> spawners;

    public ResourceManager() {
        spawners = new HashMap<Chunk, ResourceSpawner>();
    }

    public void addSpawner() {
        World w = Bukkit.getWorlds().get((int) (Math.random() * Bukkit.getWorlds().size()));
        if((w.getEnvironment().equals(World.Environment.NETHER) || w.getEnvironment().equals(World.Environment.THE_END)) && Math.random() < 0.8) {
            w = Bukkit.getWorlds().get((int) (Math.random() * Bukkit.getWorlds().size()));
        }
        Location l = pickLocation(w);
        if(!spawners.containsKey(l.getChunk()))
            spawners.put(l.getChunk(), new ResourceSpawner(l));
    }

    public Location pickLocation(World w) {
        WorldBorder b = w.getWorldBorder();
        double x = b.getCenter().getBlockX() + (Math.random() * 2 - 1) * (b.getSize() / 2);
        double z = b.getCenter().getBlockZ() + (Math.random() * 2 - 1) * (b.getSize() / 2);
        double y = (w.getHighestBlockYAt((int) x, (int) z) + w.getMinHeight()) * Math.random() - w.getMinHeight();
        return new Location(w, x, y, z);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();

        ArrayList<Map<String, Object>> sObjects = new ArrayList<>();
        for(ResourceSpawner s : spawners.values()) {
            sObjects.add(s.serialize());
        }
        serialized.put("spawners", sObjects);

        return serialized;
    }

    public ResourceSpawner getSpawnerAtChunk(Chunk c) {
        if(spawners.containsKey(c)) {
            return spawners.get(c);
        }
        return null;
    }

    public ResourceManager(Map<String, Object> serialized) {
        spawners = new HashMap<>();
        ArrayList<Map<String, Object>> sObjects = (ArrayList<Map<String, Object>>) serialized.get("spawners");
        for(Object sObject : sObjects) {
            ResourceSpawner r = new ResourceSpawner((Map<String, Object>) sObject);
            spawners.put(r.getLocation().getChunk(), r);
        }
    }

    public void update() {
        Collection<ResourceSpawner> list = spawners.values();
        for(ResourceSpawner s : list) {
            s.spawnResource();
            if(s.getChance() < 0.1)
                spawners.remove(s.getLocation().getChunk());
        }

        if(spawners.size() < maxSpawners) {
            addSpawner();
        }
    }


}
