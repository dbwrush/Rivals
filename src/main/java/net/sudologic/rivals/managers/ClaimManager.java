package net.sudologic.rivals.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.logging.Level;

public class ClaimManager {
    private RegionContainer container;

    public ClaimManager() {
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    public boolean createClaim(Chunk c, Faction f) {
        String name = f.getClaimName(c);
        RegionManager manager = container.get(BukkitAdapter.adapt(c.getWorld()));
        Location lMin = c.getBlock(0, c.getWorld().getMinHeight(), 0).getLocation();
        Location lMax = c.getBlock(15, c.getWorld().getMaxHeight(), 15).getLocation();
        BlockVector3 min = BlockVector3.at(lMin.getX(), lMin.getY(), lMin.getZ());
        BlockVector3 max = BlockVector3.at(lMax.getX(), lMax.getY(), lMax.getZ());
        ProtectedRegion region = new ProtectedCuboidRegion(name, min, max);
        ApplicableRegionSet set = manager.getApplicableRegions(region);
        if(set.size() == 0) {
            setRegionMembers(region, f);
            manager.addRegion(region);
            return true;
        }
        return false;
    }

    public boolean removeClaim(Chunk c, Faction f) {
        String name = f.getClaimName(c);
        World w = c.getWorld();
        RegionManager manager = container.get(BukkitAdapter.adapt(w));
        if(manager.hasRegion(name)) {
            manager.removeRegion(name);
            try {
                manager.saveChanges();
            } catch (StorageException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }

    public void updateFactionMembers(Faction f) {
        ArrayList<ProtectedRegion> regions = getRegionsForFaction(f);
        DefaultDomain domain = new DefaultDomain();
        Bukkit.getLogger().log(Level.INFO, "Updating faction claim members");
        for(UUID uuid : f.getMembers()) {
            Bukkit.getLogger().log(Level.INFO, "Added " + Bukkit.getOfflinePlayer(uuid).getName() + " to member list.");
            domain.addPlayer(uuid);
        }
        for(ProtectedRegion r : regions) {
            if(r != null) {
                Bukkit.getLogger().log(Level.INFO, "Added members to claim " + r.getId());
                r.setMembers(domain);
            }
        }
        ShopManager shopManager = Rivals.getShopManager();
        if(shopManager.getRegionIDForFaction(f.getID()) != null) {
            ProtectedRegion shopRegion = shopManager.getRegionForFaction(f);
            if(shopRegion != null) {
                shopRegion.setMembers(domain);
            }
        }
    }

    public static void setFactionAsRegionMember(Faction f, ProtectedRegion region) {
        DefaultDomain domain = new DefaultDomain();
        for(UUID uuid : f.getMembers()) {
            domain.addPlayer(uuid);
        }
        region.setMembers(domain);
    }

    private void setRegionMembers(ProtectedRegion region, Faction f) {
        DefaultDomain domain = new DefaultDomain();
        for(UUID uuid : f.getMembers()) {
            domain.addPlayer(uuid);
        }
        region.setMembers(domain);
    }

    public ArrayList<ProtectedRegion> getRegionsForFaction(Faction f) {
        ArrayList<ProtectedRegion> regions = new ArrayList<>();
        List<String> regionNames = f.getRegions();
        for(String s : regionNames) {
            String world = s.split("_")[1];
            RegionManager m = container.get(BukkitAdapter.adapt(Bukkit.getWorld(world)));
            regions.add(m.getRegion(s));
        }
        return regions;
    }

    public void removeRegionsForFaction(Faction f) {
        List<String> regionNames = f.getRegions();
        for(String s : regionNames) {
            String world = s.split("_")[1];
            RegionManager m = container.get(BukkitAdapter.adapt(Bukkit.getWorld(world)));
            m.removeRegion(s);
        }
    }

    public ProtectedRegion getExistingClaim(Chunk c) {
        String name = "test";
        RegionManager manager = container.get(BukkitAdapter.adapt(c.getWorld()));
        Location lMin = c.getBlock(0, c.getWorld().getMinHeight(), 0).getLocation();
        Location lMax = c.getBlock(15, c.getWorld().getMaxHeight(), 15).getLocation();
        BlockVector3 min = BlockVector3.at(lMin.getX(), lMin.getY(), lMin.getZ());
        BlockVector3 max = BlockVector3.at(lMax.getX(), lMax.getY(), lMax.getZ());
        ProtectedRegion region = new ProtectedCuboidRegion(name, min, max);
        ApplicableRegionSet set = manager.getApplicableRegions(region);
        Set<ProtectedRegion> regions = set.getRegions();
        for(ProtectedRegion r : regions) {
            if(r.getId().contains("rfclaims")) {
                return r;
            }
        }
        return null;
    }

    public double getClaimStrength(Faction f) {
        int claims = f.getRegions().size();
        double power = f.getPower();

        return power / claims;
    }
}
