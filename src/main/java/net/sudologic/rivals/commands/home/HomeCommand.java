package net.sudologic.rivals.commands.home;

import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import net.sudologic.rivals.managers.FactionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class HomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)) {
            Bukkit.getLogger().log(Level.INFO, "[Rivals] This command may only be run by players");
            return true;
        }
        Player p = (Player) sender;
        if(args.length < 1) {
            p.sendMessage("[Rivals] You must specify a home to visit. Use /homes to get a list of your homes, or /sethome to add a new home.");
            return true;
        }
        Faction f = Rivals.getFactionManager().getFactionByPlayer(p.getUniqueId());
        if(f != null) {
            Faction.Home h = f.getHome(args[0]);
            if(h != null) {
                if(Rivals.getEventManager().getCombat(p.getUniqueId())) {
                    p.sendMessage("[Rivals] You cannot teleport in combat.");
                    return true;
                }
                if(f.getHomes().size() > f.getMaxHomes()) {
                    p.sendMessage("[Rivals] Your faction has too many homes. Remove " + (f.getHomes().size() - f.getMaxHomes()) + " homes.");
                    return true;
                }
                p.sendMessage("[Rivals] Teleporting to " + args[0]);
                p.teleport(h.getLocation());
            }
            p.sendMessage("[Rivals] Your faction has no home by that name.");
            return true;
        }
        p.sendMessage("[Rivals] You must be in a faction to use homes.");
        return true;
    }

    public static class DelCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
            FactionManager manager = Rivals.getFactionManager();
            if(!(sender instanceof Player)) {
                Bukkit.getLogger().log(Level.INFO, "[Rivals] This command may only be run by players");
                return true;
            }
            Player p = (Player)sender;
            Faction faction = manager.getFactionByPlayer(p.getUniqueId());
            if(faction != null) {
                if(args.length < 1) {
                    p.sendMessage("[Rivals] Please specify which home to delete.");
                    return true;
                }
                if(faction.getHome(args[0]) == null) {
                    p.sendMessage("[Rivals] No home by that name, did you spell it correctly?");
                    return true;
                }
                if(faction.delHome(args[0])) {
                    p.sendMessage("[Rivals] Deleting home " + args[0]);
                    return true;
                } else {
                    p.sendMessage("[Rivals] Failed to delete home " + args[0]);
                    return true;
                }
            }
            p.sendMessage("[Rivals] You must be in a faction to delete a home.");
            return true;
        }
    }

    public static class SetHomeCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
            FactionManager manager = Rivals.getFactionManager();
            if(!(sender instanceof Player)) {
                Bukkit.getLogger().log(Level.INFO, "[Rivals] This command may only be run by players");
                return true;
            }
            Player p = (Player)sender;
            Faction faction = manager.getFactionByPlayer(p.getUniqueId());
            if(faction != null) {
                if(faction.getHomes().size() < faction.getMaxHomes()) {
                    if(args.length < 1) {
                        p.sendMessage("[Rivals] Please specify a home name.");
                        return true;
                    }
                    p.sendMessage("[Rivals] Set home " + args[0]);
                    faction.setHome(args[0], p.getLocation());
                } else {
                    p.sendMessage("[Rivals] Your faction needs more power to get more homes.");
                    return true;
                }
            }
            p.sendMessage("[Rivals] You must be in a faction to set a home.");
            return true;
        }
    }

    public static class HomesCommand implements CommandExecutor{

        @Override
        public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
            FactionManager manager = Rivals.getFactionManager();
            if(!(sender instanceof Player)) {
                Bukkit.getLogger().log(Level.INFO, "[Rivals] This command may only be run by players");
                return true;
            }
            Player p = (Player)sender;
            Faction faction = manager.getFactionByPlayer(p.getUniqueId());
            if(faction != null) {
                p.sendMessage("[Rivals] Homes for " + faction.getColor() + faction.getName());
                for(String home : faction.getHomes().keySet()) {
                    p.sendMessage(home);
                }
                p.sendMessage("Used " + faction.getHomes().size() + " / " + faction.getMaxHomes());
            }
            p.sendMessage("[Rivals] You must be in a faction to list homes.");
            return true;
        }
    }
}
