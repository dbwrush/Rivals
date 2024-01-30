package net.sudologic.rivals.commands;

import net.sudologic.rivals.Rivals;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            if(!commandSender.isOp() && !commandSender.hasPermission("rivals.admin")) {
                commandSender.sendMessage("[Rivals] You do not have permission to use this command.");
                return true;
            }
        }

        if(args.length < 1) {
            commandSender.sendMessage("[Rivals] Options: setMainShopRegion <id>, scanForShopRegions");
            return true;
        }
        if("setMainShopRegion".equals(args[0])) {
            if(args.length < 2) {
                commandSender.sendMessage("[Rivals] This subcommand requires a region ID.");
                return true;
            }
            Rivals.getShopManager().setMainRegionString(args[1]);
            commandSender.sendMessage("[Rivals] Set main shop region to " + args[1]);
            return true;
        } else if("scanForShopRegions".equals(args[0])) {
            int count = Rivals.getShopManager().addSubregions();
            commandSender.sendMessage("[Rivals] There are now " + count + " shop subregions.");
            return true;
        }
        return false;
    }
}
