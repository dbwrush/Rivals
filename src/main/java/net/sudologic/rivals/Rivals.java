package net.sudologic.rivals;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/*
TODO:
    - Add shopkeepers
 */

public final class Rivals extends JavaPlugin {
    private static FileConfiguration customConfig;
    private static FactionManager factionManager;
    private static ClaimManager claimManager;
    private static ShopManager shopManager;
    private static RivalsCommand command;
    private static ConfigurationSection settings;

    @Override
    public void onEnable() {
        Bukkit.getLogger().log(Level.INFO, "[Rivals] Starting!");
        registerClasses();
        createCustomConfig();
        createConfigs();

        registerListeners();
        registerCommands();

        claimManager = new ClaimManager();
    }

    @Override
    public void onDisable() {
        saveData();
        Bukkit.getLogger().log(Level.INFO, "[Rivals] Closing!");
    }

    public void saveData() {
        if(getConfig().getConfigurationSection("settings") == null) {
            getConfig().set("settings", settings);
        }
        getConfig().set("factionManager", factionManager);
        getConfig().set("shopManager", shopManager);

        //System.out.println(getConfig().get("data"));
        saveConfig();
    }

    public void readData() {
        if(getConfig().getConfigurationSection("settings") != null) {
            settings = (ConfigurationSection) getConfig().get("settings");
        } else {
            settings = new YamlConfiguration();
            Bukkit.getLogger().log(Level.INFO, "No existing settings, creating them.");
            settings.set("minShopPower", 10.0);
            settings.set("killEntityPower", 0.0);
            settings.set("killMonsterPower", 1.0);
            settings.set("killPlayerPower", 3.0);
            settings.set("deathPowerLoss", -4.0);
            settings.set("tradePower", 1.0);
            settings.set("defaultPower", 3.0);
            settings.set("maxNameLength", 16);
        }
        if(getConfig().get("factionManager") != null) {
            factionManager = (FactionManager) getConfig().get("factionManager", FactionManager.class);
        } else {
            factionManager = new FactionManager();
        }
        if(getConfig().get("shopManager") != null) {
            shopManager = (ShopManager) getConfig().get("shopManager", ShopManager.class);
        } else {
            shopManager = new ShopManager();
        }
    }

    public static ConfigurationSection getSettings() {
        return settings;
    }

    public void createCustomConfig() {
        File customConfigFile = new File(getDataFolder(), "config.yml");
        if(!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        customConfig = new YamlConfiguration();
        try{
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        readData();
    }

    public void createConfigs() {
        this.saveDefaultConfig();
        this.getConfig();
    }

    public void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EventManager(settings), this);
    }

    public static RivalsCommand getCommand() {
        return command;
    }

    public void registerCommands() {
        command = new RivalsCommand(settings);
        this.getCommand("rivals").setExecutor(command);
        this.getCommand("rivalsadmin").setExecutor(new AdminCommand());
    }

    public void registerClasses() {
        ConfigurationSerialization.registerClass(Faction.class);
        ConfigurationSerialization.registerClass(FactionManager.class);
        ConfigurationSerialization.registerClass(FactionManager.MemberInvite.class);
        ConfigurationSerialization.registerClass(FactionManager.AllyInvite.class);
        ConfigurationSerialization.registerClass(ShopManager.class);
    }

    public static FactionManager getFactionManager() {
        return factionManager;
    }

    public static ShopManager getShopManager() {
        return shopManager;
    }

    public static ClaimManager getClaimManager() {
        return claimManager;
    }
}
