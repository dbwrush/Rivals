package net.sudologic.rivals;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopManager implements ConfigurationSerializable {
    public String mainRegionString;
    public ArrayList<String> shopSubregionStrings;
    public Map<UUID, String> regionAssignments;

    public String getMainRegionString() {
        return mainRegionString;
    }

    public void setMainRegionString(String mainRegionString) {
        this.mainRegionString = mainRegionString;
    }

    public ShopManager(Map<String, Object> serializedShopManager) {
        this.mainRegionString = (String) serializedShopManager.get("mainRegionString");
        this.shopSubregionStrings = (ArrayList<String>) serializedShopManager.get("shopSubregionStrings");
        this.regionAssignments = (Map<UUID, String>) serializedShopManager.get("regionAssignments");
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> mapSerializer = new HashMap<>();
        mapSerializer.put("mainRegionString", mainRegionString);
        mapSerializer.put("shopSubregionStrings", shopSubregionStrings);
        mapSerializer.put("regionAssignments", regionAssignments);
        return mapSerializer;
    }

    public boolean assignFactionToShop(UUID uuid, String regionString) {
        if(shopSubregionStrings.contains(regionString)) {
            if(!regionAssignments.values().contains(regionString)) {
                regionAssignments.put(uuid, regionString);
                return true;
            }
        }
        return false;
    }

    public String getRegionIDForFaction(UUID uuid) {
        if(regionAssignments.containsKey(uuid)) {
            return regionAssignments.get(uuid);
        }
        return null;
    }
}
