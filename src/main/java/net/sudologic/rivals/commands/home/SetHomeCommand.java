package net.sudologic.rivals.commands.home;

import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import net.sudologic.rivals.FactionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class SetHomeCommand implements CommandExecutor {
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
