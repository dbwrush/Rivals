package net.sudologic.rivals;

import net.sudologic.rivals.commands.AdminCommand;
import net.sudologic.rivals.commands.RivalsCommand;
import net.sudologic.rivals.commands.home.HomeCommand;
import net.sudologic.rivals.managers.ClaimManager;
import net.sudologic.rivals.managers.EventManager;
import net.sudologic.rivals.managers.FactionManager;
import net.sudologic.rivals.managers.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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
    - Scoreboard: Show faction name in its color, along with faction power.
        Number of online members and number of total members should also be shown.
        War status shown here (peace, imminent war, active war)
        Current proposal status
        Current invites
    - Add time-delay war declarations
        Immediate war declarations have a power penalty, but time-delayed ones do not.
        Delay time configurable in config.yml
    - Faction ranking
        Upon serialization/deserialization convert ranks from a list to a number stored with the faction.
        Any time that Faction.powerChange() is called, factions may need to be re-ordered.
        When Faction list is called, factions should be displayed in order by power, not by creation date.
    - Resource chunks
        Spawn randomly, quantity controlled by config.yml
        Every x time, all resource chunks have a resource spawn opportunity.
        Opportunities are taken based on a random chance, which becomes less likely on a decay curve.
        Once random chance falls below some configurable threshold, resource chunk moves to new location and resets its chance.
    - Politics
        Each Faction can propose one resolution at a time.
        Factions can vote for or against, strength of vote controlled by Faction power.
        If a resolution has a majority vote after the allotted time, it passes.
        Proposing faction gets a power change relative to the amount of support for the proposal, positive or negative.
        Proposal types: All Proposals will have a description for why it should be passed
            - Denounce: Faction faces an immediate loss of power relative to support to this proposal, but no lasting consequences.
            - Sanction: Punish a faction for perceived misbehavior. Reduce their power changes by some proportion relative to support to this proposal.
            - Unsanction: Removes sanctions.
            - Intervention: Declare a single Faction to be a threat to all players, all Factions are effectively at war with this one.
            - Change setting: Changes some setting in the config (this behavior can be enabled/disabled in the config).
            - Custodian (add/remove): Custodian faction gets faster power gain and is able to build within the shopping area. Powers are assumed to benefit common good.
                - Set Budget: Declare need for materials which will be given to the Custodian who requested them. Factions which provide materials can gain power from it.
                - Set Mandate: Require the Custodian(s) to work towards a certain goal.
 */

public final class Rivals extends JavaPlugin {
    private static FileConfiguration customConfig;
    private static FactionManager factionManager;
    private static ClaimManager claimManager;
    private static ShopManager shopManager;
    private static RivalsCommand command;
    private static ConfigurationSection settings;
    private static EventManager eventManager;

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
        eventManager = new EventManager(settings);
        pm.registerEvents(eventManager, this);
    }

    public static RivalsCommand getCommand() {
        return command;
    }

    public void registerCommands() {
        command = new RivalsCommand(settings);
        this.getCommand("rivals").setExecutor(command);
        this.getCommand("rivalsadmin").setExecutor(new AdminCommand());
        this.getCommand("home").setExecutor(new HomeCommand());
        this.getCommand("sethome").setExecutor(new HomeCommand.SetHomeCommand());
        this.getCommand("delHome").setExecutor(new HomeCommand.DelCommand());
        this.getCommand("homes").setExecutor(new HomeCommand.HomesCommand());
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

    public static EventManager getEventManager() {return eventManager;}
}
