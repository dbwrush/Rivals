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
import java.util.Map;
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
    private static FileConfiguration customConfig, savedData;

    private static FactionManager factionManager;

    @Override
    public void onEnable() {
        Bukkit.getLogger().log(Level.INFO, "[Rivals] Starting!");
        registerClasses();
        createCustomConfig();
        createConfigs();

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        saveData();
        Bukkit.getLogger().log(Level.INFO, "[Rivals] Closing!");
    }

    public void saveData() {
        getConfig().set("data", factionManager);
        System.out.println(getConfig().get("data"));
        //{invites=[], factions={787d1337-5692-448f-9ae6-c49b5881b6e1=net.sudologic.rivals.Faction@42c39711}}
        saveConfig();
    }

    public FactionManager readData() {
        if(getConfig().get("data") != null) {
            System.out.println(getConfig().get("data"));
            //MemorySection[path='data', root='YamlConfiguration']
            return (FactionManager) getConfig().get("data", FactionManager.class);//error is here
            //java.lang.ClassCastException: class org.bukkit.configuration.MemorySection cannot be cast to clas java.util.Map
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
    }

    public static FactionManager getFactionManager() {
        return factionManager;
    }
}
