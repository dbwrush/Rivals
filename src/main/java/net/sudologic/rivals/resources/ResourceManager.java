package net.sudologic.rivals.resources;

import net.sudologic.rivals.Rivals;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class ResourceManager implements ConfigurationSerializable {
    int maxSpawners = 128;
    ArrayList<ResourceSpawner> spawners;

    public ResourceManager() {
        spawners = new ArrayList<>();
    }

    public void addSpawner() {
        World w = Bukkit.getWorlds().get((int) (Math.random() * Bukkit.getWorlds().size()));
        WorldBorder b = w.getWorldBorder();
        double x = b.getCenter().getBlockX() + (Math.random() * 2 - 1) * (b.getSize() / 2);
        double z = b.getCenter().getBlockZ() + (Math.random() * 2 - 1) * (b.getSize() / 2);
        double y = (w.getHighestBlockYAt((int) x, (int) z) + w.getMinHeight()) * Math.random() - w.getMinHeight();
        spawners.add(new ResourceSpawner(new Location(w, x, y, z)));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();

        ArrayList<Map<String, Object>> sObjects = new ArrayList<>();
        for(ResourceSpawner s : spawners) {
            sObjects.add(s.serialize());
        }
        serialized.put("spawners", sObjects);

        return serialized;
    }

    public ResourceManager(Map<String, Object> serialized) {
        spawners = new ArrayList<>();
        ArrayList<Map<String, Object>> sObjects = (ArrayList<Map<String, Object>>) serialized.get("spawners");
        for(Object sObject : sObjects) {
            spawners.add(new ResourceSpawner((Map<String, Object>) sObject));
        }
    }

    public void update() {
        ArrayList<ResourceSpawner> rem = new ArrayList<>();
        for(ResourceSpawner s : spawners) {
            s.spawnResource();
            if(s.getChance() < 0.1)
                rem.add(s);
        }
        spawners.removeAll(rem);
        if(spawners.size() < maxSpawners) {
            addSpawner();
        }
    }


}
