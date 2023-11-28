package net.sudologic.rivals;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RivalsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        FactionManager manager = Rivals.getFactionManager();
        /*
        /rivals
            join
            removemember
            leave
            addally
            removeally
            setenemy
            removeenemy
            claim
            info
        */
        if(!(sender instanceof Player)) {
            Bukkit.getLogger().log(Level.INFO, "[Rivals] This command may only be run by players");
            return true;
        }
        Player p = (Player)sender;
        if(args.length >= 1) {
            Faction faction = Rivals.getFactionManager().getFactionByPlayer(p.getUniqueId());
            if(args[0].equals("create")) {//create submenu
                if(faction != null) {
                    p.sendMessage("[Rivals] You must leave your current faction before making a new one.");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] Please include faction name");
                    return true;
                }
                String name = args[1];
                if(manager.nameAlreadyExists(name)) {
                    p.sendMessage("[Rivals] " + name + " already exists.");
                }
                manager.addFaction(new Faction(p.getUniqueId(), name));
                p.sendMessage("[Rivals] Created the " + name + " faction.");
                return true;
            }
            else if(args[0].equals("invite")) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be a part of a faction to invite people.");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] Please include invited player's name");
                    return true;
                }
                Player invited = Bukkit.getPlayer(args[1]);
                if(invited == null) {
                    p.sendMessage("[Rivals] That player is offline, please wait for them to log in before inviting them.");
                    return true;
                }
                manager.addInvite(invited.getUniqueId(), faction.getID());
                p.sendMessage("[Rivals] Invited " + invited.getDisplayName() + " to join " + faction.getName());
                return true;
            }
            else if(args[0].equals("join")) {
                if(faction != null) {
                    p.sendMessage("[Rivals] You must leave your current faction before you can join another.");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] Please include the faction's name.");
                    return true;
                }
                String fName = args[1];
                List<UUID> invites = manager.getInvitesForPlayer(p.getUniqueId());
                for(UUID f : invites) {
                    if(manager.getFactionByID(f).getName().equals(fName)) {
                        manager.getFactionByID(f).addMember(p.getUniqueId());
                        p.sendMessage("[Rivals] You've joined " + fName);
                        manager.removeInvite(p.getUniqueId(), f);
                        return true;
                    }
                }
                p.sendMessage("[Rivals] That faction either hasn't invited you or doesn't exist.");
            } else {
                p.sendMessage("[Rivals] Invalid syntax");
            }
        }
        else {
            p.sendMessage("[Rivals] Incomplete syntax");
        }





        return true;
    }
}
