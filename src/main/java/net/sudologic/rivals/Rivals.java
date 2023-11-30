package net.sudologic.rivals;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/*
TODO:
    - Write Rivals command
        - Wars
        - Make Peace
    - Add land claims
    - Add shopkeepers
 */

public final class Rivals extends JavaPlugin {
    private static FileConfiguration customConfig;

    private static FactionManager factionManager;
    private static ClaimManager claimManager;

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
        getConfig().set("data", factionManager);
        //System.out.println(getConfig().get("data"));
        saveConfig();
    }

    public FactionManager readData() {
        if(getConfig().get("data") != null) {
            FactionManager manager = (FactionManager) getConfig().get("data", FactionManager.class);
            return manager;
        } else {
            return new FactionManager();
        }
    }

    public void createCustomConfig() {
        File customConfigFile = new File(getDataFolder(), "config.yml");
        if(!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        customConfig = new YamlConfiguration();
        try{
            customConfig.load(customConfigFile);//error here
            //org.yaml.snakeyaml.error.YAMLException: Could not deserialize object
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        factionManager = readData();
    }

    public void createConfigs() {
        this.saveDefaultConfig();
        this.getConfig();
    }

    public void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
    }

    public void registerCommands() {
        this.getCommand("rivals").setExecutor(new RivalsCommand());
    }

    public void registerClasses() {
        ConfigurationSerialization.registerClass(Faction.class);
        ConfigurationSerialization.registerClass(FactionManager.class);
        ConfigurationSerialization.registerClass(FactionManager.MemberInvite.class);
        ConfigurationSerialization.registerClass(FactionManager.AllyInvite.class);
    }

    public static FactionManager getFactionManager() {
        return factionManager;
    }

    public static ClaimManager getClaimManager() {
        return claimManager;
    }
}
