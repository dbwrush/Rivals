package net.sudologic.rivals;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public class RivalsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        FactionManager manager = Rivals.getFactionManager();
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
                manager.addFaction(new Faction(p.getUniqueId(), name, manager.getUnusedFactionID()));
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
                manager.addMemberInvite(invited.getUniqueId(), faction.getID());
                faction.sendMessageToOnlineMembers(invited.getName() + " has been invited to join your faction.");
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
                List<Integer> invites = manager.getInvitesForPlayer(p.getUniqueId());
                for(int f : invites) {
                    if(manager.getFactionByID(f).getName().equals(fName)) {
                        manager.getFactionByID(f).addMember(p.getUniqueId());
                        p.sendMessage("[Rivals] You've joined " + fName);
                        manager.removeMemberInvite(p.getUniqueId(), f);
                        return true;
                    }
                }
                p.sendMessage("[Rivals] That faction either hasn't invited you or doesn't exist.");
            }
            else if(args[0].equals("leave")) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction in order to leave it.");
                    return true;
                }
                faction.removeMember(p.getUniqueId());
                p.sendMessage("[Rivals] You are no longer a member of " + faction.getName());
                return true;
            }
            else if(args[0].equals("enemy")) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to declare war.");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] You must specify a faction to declare war on.");
                    return true;
                }
                String enemyName = args[1];
                Faction enemy = manager.getFactionByName(enemyName);
                if(faction.addEnemy(enemy.getID())) {
                    p.sendMessage("[Rivals] You are now enemies with " + enemyName);
                } else {
                    p.sendMessage("[Rivals] Could not declare war on " + enemyName + ", there might not be a faction by that name.");
                }
                return true;
            }
            else if(args[0].equals("ally")) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to invite another faction to an alliance.");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] You must specify a faction to invite to an alliance.");
                    return true;
                }
                String allyName = args[1];
                Faction ally = manager.getFactionByName(allyName);
                if(manager.getAllyInvitesForFaction(faction.getID()).contains(ally.getID())) {
                    faction.addAlly(ally.getID());
                    manager.removeAllyInvite(ally.getID(), faction.getID());
                } else {
                    manager.addAllyInvite(faction.getID(), ally.getID());
                    p.sendMessage("[Rivals] Sent alliance invite to " + ally.getName() + ", they can run the same command to accept the invite.");
                }
                return true;
            }
            else if(args[0].equals("peace")) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to send a peace offer.");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] You must specify a faction to offer peace.");
                    return true;
                }
                String enemyName = args[1];
                Faction enemy = manager.getFactionByName(enemyName);
                if(!faction.getEnemies().contains(enemy.getID())) {
                    p.sendMessage("[Rivals] You are not at war with " + enemy.getName());
                    return true;
                }
                if(manager.getPeaceInvitesForFaction(faction.getID()).contains(enemy.getID())) {
                    faction.removeEnemy(enemy.getID());
                    manager.removePeaceInvite(enemy.getID(), faction.getID());
                } else {
                    manager.addPeaceInvite(faction.getID(), enemy.getID());
                    p.sendMessage("[Rivals] Sent peace offer to " + enemy.getName() + ", they can run the same command to accept the offer.");
                }
                return true;
            }
            else if(args[0].equals("unally")) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to end an alliance.");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] You must specify a faction to end alliance.");
                    return true;
                }
                String allyName = args[1];
                Faction ally = manager.getFactionByName(allyName);
                if(!faction.getAllies().contains(ally.getID())) {
                    p.sendMessage("[Rivals] You are not allied with " + ally.getName());
                    return true;
                }
                faction.removeAlly(ally.getID());
                return true;
            }
            else if(args[0].equals("claim")) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to claim land.");
                    return true;
                }
                ClaimManager claimManager = Rivals.getClaimManager();
                Chunk c = p.getLocation().getChunk();
                if(claimManager.createClaim(c, faction)) {
                    faction.sendMessageToOnlineMembers("Claimed chunk X:" + c.getX() + " Z: " + c.getZ() + " in " + c.getWorld().getName() + ".");
                } else {
                    ProtectedRegion existingClaim = claimManager.getExistingClaim(c);
                    if(existingClaim != null) {
                        String id = existingClaim.getId();
                        Faction f = manager.getFactionByName(id.split("-")[2]);
                        if(f != null && faction.getEnemies().contains(f.getID())) {
                            p.sendMessage("[Rivals] This chunk belongs to an enemy!");
                        }
                    }
                }
            }
            else if(args[0].equals("info")) {
                if(args.length < 2) {
                    if(faction == null) {
                        p.sendMessage("[Rivals] You must be in a faction to get info on your own faction. Add a faction name to look up their info.");
                        return true;
                    }
                    sendFactionInfo(p, faction, "");
                }
                String factionName = args[1];
                Faction f = manager.getFactionByName(factionName);
                if(f == null) {
                    p.sendMessage("[Rivals] There is no faction by that name.");
                    return true;
                }
                if(args.length == 3) {
                    sendFactionInfo(p, f, args[2]);
                    return true;
                }
                sendFactionInfo(p, f, "");
            }
            else {
                p.sendMessage("[Rivals] Invalid syntax");
            }
        }
        else {
            p.sendMessage("[Rivals] Incomplete syntax");
        }
        return true;
    }

    public void sendFactionInfo(Player p, Faction f, String s) {
        FactionManager manager = Rivals.getFactionManager();
        String mess = "";
        if(s.equals("")) {
            mess = "[Rivals] Info on " + f.getName();
            String members = "\n";
            if(f.getMembers().size() > 3) {
                for(int i = 0; i < 3; i++) {
                    members += Bukkit.getPlayer(f.getMembers().get(i)) + ", ";
                }
                members += "+ " + (f.getMembers().size() - 3);
            } else if(f.getMembers().size() > 1){
                for(int i = 0; i < f.getMembers().size() - 1; i++) {
                    members += Bukkit.getPlayer(f.getMembers().get(i)) + ", ";
                }
                members += " and " + Bukkit.getPlayer(f.getMembers().get(2));
            } else {
                members += Bukkit.getPlayer(f.getMembers().get(0));
            }
            mess += members;

            String allies = "\n";
            if(f.getAllies().size() > 3) {
                for(int i = 0; i < 3; i++) {
                    allies += manager.getFactionByID(f.getAllies().get(i)) + ", ";
                }
                allies += "+ " + (f.getAllies().size() - 3);
            } else if(f.getAllies().size() > 1){
                for(int i = 0; i < f.getAllies().size() - 1; i++) {
                    allies += manager.getFactionByID(f.getAllies().get(i)) + ", ";
                }
                allies += " and " + manager.getFactionByID(f.getAllies().get(2));
            } else {
                allies += manager.getFactionByID(f.getAllies().get(0));
            }
            mess += allies;

            String enemies = "\n";
            if(f.getEnemies().size() > 3) {
                for(int i = 0; i < 3; i++) {
                    enemies += manager.getFactionByID(f.getEnemies().get(i)) + ", ";
                }
                enemies += "+ " + (f.getEnemies().size() - 3);
            } else if(f.getEnemies().size() > 1){
                for(int i = 0; i < f.getEnemies().size() - 1; i++) {
                    enemies += manager.getFactionByID(f.getEnemies().get(i)) + ", ";
                }
                enemies += " and " + manager.getFactionByID(f.getEnemies().get(2));
            } else {
                enemies += manager.getFactionByID(f.getEnemies().get(0));
            }
            mess += enemies;

            String chunks = "\nChunks: " + f.getRegions().size();

            mess += chunks;

            String hint = "\nFor more info, add 'members', 'allies', or 'enemies' to the command.";
        } else {
            if(s.equals("members")) {
                mess = "[Rivals] Members of " + f.getName();
                String members = "\n";
                for(int i = 0; i < f.getMembers().size() - 1; i++) {
                    members += Bukkit.getPlayer(f.getMembers().get(i)) + ", ";
                }
                members += "and " + (f.getMembers().size() - 1);
                mess += members;
            }
            else if(s.equals("allies")) {
                mess = "[Rivals] Allies of " + f.getName();
                String allies = "\n";
                for(int i = 0; i < f.getAllies().size() - 1; i++) {
                    allies += manager.getFactionByID(f.getAllies().get(i)) + ", ";
                }
                allies += "and " + manager.getFactionByID(f.getAllies().get(f.getAllies().size() - 1));
                mess += allies;
            }
            else if(s.equals("enemies")) {
                mess = "[Rivals] Enemies of " + f.getName();
                String enemies = "\n";
                for(int i = 0; i < f.getEnemies().size() - 1; i++) {
                    enemies += manager.getFactionByID(f.getEnemies().get(i)) + ", ";
                }
                enemies += "and " + manager.getFactionByID(f.getEnemies().get(f.getEnemies().size() - 1));
                mess += enemies;
            }
            else {
                mess = "[Rivals] Choose either 'members', 'allies', or 'enemies' to get details about a faction.";
            }
        }
        p.sendMessage(mess);
    }
}
