package net.sudologic.rivals;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.sudologic.rivals.util.NameFetcher;
import net.sudologic.rivals.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RivalsCommand implements CommandExecutor {

    private double minShopPower;
    private int maxNameLength;

    public RivalsCommand(ConfigurationSection settings) {
        minShopPower = (double) settings.get("minShopPower");
        maxNameLength = (int) settings.get("maxNameLength");
        /*minShopPower = 10;
        maxNameLength = 16;*/
    }

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
            if("create".equals(args[0])) {//create submenu
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
                    return true;
                }
                if(name.length() > maxNameLength) {
                    p.sendMessage("[Rivals] That name is too long.");
                    return true;
                }
                Faction f = new Faction(p.getUniqueId(), name, manager.getUnusedFactionID());
                if(manager.addFaction(f)) {
                    p.sendMessage("[Rivals] Created the " + f.getColor() + name + " faction.");
                    p.sendMessage("For a list of things to do, use /rivals help");
                    p.sendMessage("P.S., if you don't like your randomly chosen color, use /rivals color");
                } else {
                    p.sendMessage("[Rivals] Unable to create faction.");
                }
                return true;
            }
            else if("kick".equals(args[0])) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be a part of a faction to kick people out of a faction.");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] Please include kicked player's name");
                    return true;
                }
                if(!faction.getLeader().equals(p.getUniqueId())) {
                    String leaderName = NameFetcher.getName(faction.getLeader());
                    p.sendMessage("[Rivals] You must be your faction's leader to kick players. Your faction leader is " + leaderName);
                    return true;
                }
                UUID kicked = UUIDFetcher.getUUID(args[1]);
                if(kicked == null) {
                    p.sendMessage("[Rivals] Incorrect player name, did you spell it right?");
                    return true;
                }
                if(faction.removeMember(kicked)) {
                    p.sendMessage("[Rivals] Kicked " + args[1] + " from your faction.");
                    return true;
                }
            }
            else if("leader".equals(args[0])) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be a part of a faction to set your faction's leader");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] Please include new leader's name");
                    return true;
                }
                if(!faction.getLeader().equals(p.getUniqueId())) {
                    String leaderName = NameFetcher.getName(faction.getLeader());
                    p.sendMessage("[Rivals] You must be your faction's leader to set a new leader. Your faction leader is " + leaderName);
                    return true;
                }
                UUID newLeaderID = UUIDFetcher.getUUID(args[1]);
                if(newLeaderID == null) {
                    p.sendMessage("[Rivals] Incorrect player name, did you spell it right?");
                    return true;
                }
                if(faction.removeMember(newLeaderID)) {
                    p.sendMessage("[Rivals] Made " + args[1] + " the leader of your faction.");
                    faction.sendMessageToOnlineMembers(args[1] + " is now the leader of your faction.");
                    return true;
                }
            }
            else if("invite".equals(args[0])) {
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
                invited.sendMessage("[Rivals] You have been invited to join " + faction.getColor() + faction.getName());
                faction.sendMessageToOnlineMembers("[Rivals] " + invited.getName() + " has been invited to join your faction.");
                return true;
            }
            else if("join".equals(args[0])) {
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
            else if("leave".equals(args[0])) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction in order to leave it.");
                    return true;
                }
                faction.removeMember(p.getUniqueId());
                p.sendMessage("[Rivals] You are no longer a member of " + faction.getName());
                return true;
            }
            else if("enemy".equals(args[0])) {
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
                    Faction imprecise = manager.getFactionByNameImprecise(enemyName);
                    if(imprecise != null) {
                        p.sendMessage("There is a faction named " + imprecise.getName());
                    }
                }
                return true;
            }
            else if("ally".equals(args[0])) {
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
                if(ally == null) {
                    Faction imprecise = manager.getFactionByNameImprecise(allyName);
                    if(imprecise != null) {
                        p.sendMessage("[Rivals] There is no faction with that name. Maybe you meant " + imprecise.getName());
                    }
                    return true;
                }
                if(manager.getAllyInvitesForFaction(faction.getID()).contains(ally.getID())) {
                    faction.addAlly(ally.getID());
                    manager.removeAllyInvite(ally.getID(), faction.getID());
                } else {
                    manager.addAllyInvite(faction.getID(), ally.getID());
                    p.sendMessage("[Rivals] Sent alliance invite to " + ally.getName() + ", they can run the same command to accept the invite.");
                }
                return true;
            }
            else if("peace".equals(args[0])) {
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
                    Faction imprecise = manager.getFactionByNameImprecise(enemyName);
                    if(imprecise != null && faction.getEnemies().contains(imprecise.getID())) {
                        p.sendMessage("You ARE at war with" + imprecise.getName());
                    }
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
            else if("unally".equals(args[0])) {
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
                    Faction imprecise = manager.getFactionByNameImprecise(allyName);
                    if(imprecise != null && faction.getAllies().contains(imprecise.getID())) {
                        p.sendMessage("You ARE allied with" + imprecise.getName());
                    }
                    return true;
                }
                faction.removeAlly(ally.getID());
                return true;
            }
            else if("claim".equals(args[0])) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to claim land.");
                    return true;
                }
                ClaimManager claimManager = Rivals.getClaimManager();
                Chunk c = p.getLocation().getChunk();
                double myStrength = claimManager.getClaimStrength(faction);
                if(myStrength < 1) {
                    p.sendMessage("[Rivals] Your faction is not powerful enough to claim this land.");
                    return true;
                }
                if(faction.addClaim(c)) {
                    faction.sendMessageToOnlineMembers("Claimed chunk X: " + c.getX() + " Z: " + c.getZ() + " in " + c.getWorld().getName() + ".");
                } else {
                    ProtectedRegion existingClaim = claimManager.getExistingClaim(c);
                    if(existingClaim != null) {
                        String id = existingClaim.getId();
                        Faction f = manager.getFactionByID(Integer.valueOf(id.split("_")[2]));
                        if(f != null) {
                            if(faction.getEnemies().contains(f.getID())) {
                                double enemyStrength = claimManager.getClaimStrength(f);
                                if(myStrength > enemyStrength * 1.5) {
                                    claimManager.removeClaim(c, f);
                                    claimManager.createClaim(c, faction);
                                    p.sendMessage("[Rivals] You have taken this chunk from " + f.getName());
                                } else {
                                    p.sendMessage("[Rivals] Your faction is not powerful enough to take this claim from " + f.getName());
                                }
                            } else {
                                if(f.equals(faction)) {
                                    p.sendMessage("[Rivals] Your faction already claims this chunk.");
                                } else {
                                    p.sendMessage("[Rivals] This chunk is already claimed by " + f.getName());
                                }
                            }
                            return true;
                        }
                    }
                    p.sendMessage("[Rivals] For unknown reasons, you cannot claim this chunk.");
                    return true;
                }
            }
            else if("unclaim".equals(args[0])) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to unclaim land.");
                    return true;
                }
                ClaimManager claimManager = Rivals.getClaimManager();
                Chunk c = p.getLocation().getChunk();
                if(claimManager.removeClaim(c, faction)) {
                    p.sendMessage("[Rivals] Removed your claim to chunk " + c.getX() + " " + c.getZ());
                    return true;
                } else {
                    p.sendMessage("[Rivals] Your faction does not claim chunk " + c.getX() + " " + c.getZ());
                }
            }
            else if("info".equals(args[0])) {
                if(args.length < 2) {
                    if(faction == null) {
                        p.sendMessage("[Rivals] You must be in a faction to get info on your own faction. Add a faction name to look up their info.");
                        return true;
                    }
                    p.sendMessage("[Rivals] Info on " + ChatColor.COLOR_CHAR + faction.getColor().toString() + faction.getName());
                    sendFactionInfo(p, faction, "");
                    return true;
                }
                String factionName = args[1];
                Faction f = manager.getFactionByName(factionName);
                if(f == null) {
                    p.sendMessage("[Rivals] There is no faction by that name.");
                    Faction imprecise = manager.getFactionByNameImprecise(factionName);
                    if(imprecise != null) {
                        p.sendMessage("[Rivals] Maybe you meant " + imprecise.getName());
                    }
                    return true;
                }
                if(args.length == 3) {
                    p.sendMessage("[Rivals] Info on " + ChatColor.COLOR_CHAR + f.getColor().toString() + f.getName());
                    sendFactionInfo(p, f, args[2]);
                    if(faction == f) {//player has looked up their own faction by name
                        p.sendMessage("[Rivals] HINT You don't need to specify your own faction. You could have used /rivals info");
                    }
                    return true;
                }
                p.sendMessage("[Rivals] Info on " + ChatColor.COLOR_CHAR + f.getColor().toString() + f.getName());
                sendFactionInfo(p, f, "");
            }
            else if("list".equals(args[0])) {
                int perPage = 8;
                List<Faction> factions = manager.getFactions();
                int numPages = (factions.size() / perPage) + 1;
                int start = 0;
                if(factions.size() == 0) {
                    p.sendMessage("[Rivals] There aren't any factions yet.");
                    return true;
                }
                String mess = "[Rivals] Factions List Page 1/" + numPages;
                if(args.length >= 2) {
                    Integer page = Integer.parseInt(args[1]);
                    mess = "[Rivals] Factions List Page " + page + "/" + numPages;
                    if (page > numPages) {
                        p.sendMessage("[Rivals] There are only " + numPages + " pages.");
                        return true;
                    }
                    start = (page - 1) * perPage;
                }
                for(int i = start; i < perPage + start && i < manager.getFactions().size(); i++) {
                    mess += "\n" + ChatColor.COLOR_CHAR + factions.get(i).getColor().toString() + factions.get(i).getName();
                }
                p.sendMessage(mess);
                return true;
            }
            else if("map".equals(args[0])) {
                Chunk c = p.getLocation().getChunk();
                String mess = "[Rivals] Map of your surroundings";
                String facts = "\nFactions: ";
                for(int x = 0; x < 9; x++) {
                    String row = "\n| ";
                    for(int z = 0; z < 9; z++) {
                        Chunk loc = c.getWorld().getChunkAt(c.getX() - 4 + x, c.getZ() - 4 + z);
                        ProtectedRegion claim = Rivals.getClaimManager().getExistingClaim(loc);
                        if(claim != null) {
                            Faction f = manager.getFactionByID(Integer.parseInt(claim.getId().split("_")[2]));
                            row += ChatColor.COLOR_CHAR + f.getColor().toString() + "X " + ChatColor.COLOR_CHAR + ChatColor.RESET + "| ";
                            if(!facts.contains(f.getName())) {
                                facts += ChatColor.COLOR_CHAR + f.getColor().toString() + f.getName() + ChatColor.COLOR_CHAR + ChatColor.RESET + " ";
                            }
                        } else {
                            row += "_ | ";
                        }
                    }
                    mess += row;
                }
                if("\nFactions: ".equals(facts)) {
                    facts = "There are no nearby factions";
                }
                mess += facts;
                p.sendMessage(mess);
            }
            else if("color".equals(args[0])) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to set the faction color.");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] You must include a color code to set your color. Example: c" + ChatColor.RED + " for red.");
                    return true;
                }
                String colorString = args[1];
                ChatColor c = ChatColor.getByChar(colorString);
                if(c == null) {
                    p.sendMessage("[Rivals] Invalid color code. Example: c" + ChatColor.RED + " for red.");
                    return true;
                }
                if(c.equals(ChatColor.MAGIC)) {
                    p.sendMessage("[Rivals] Looks cool, sorry I can't allow it.");
                    return true;
                }
                if(c.equals(ChatColor.BLACK)) {
                    p.sendMessage("[Rivals] Sorry, then nobody will be able to read your name.");
                    return true;
                }
                faction.setColor(c);
                p.sendMessage("[Rivals] Successfully changed faction color to " + faction.getColor() + faction.getName());
                return true;
            }

            else if("help".equals(args[0])) {
                String[] commands = {
                    "§e/rivals create <factionName> §f- Creates a new Faction.",
                    "§e/rivals invite <playerName> §f- Invites a player to your faction.",
                    "§e/rivals join <factionName> §f- Joins a faction that has invited you.",
                    "§e/rivals leave §f- Leaves your current faction.",
                    "§e/rivals enemy <factionName> §f- Declare another faction to be your enemy.",
                    "§e/rivals ally <factionName> §f- Propose/Accept faction alliance.",
                    "§e/rivals peace <factionName> §f- Propose/Accept peace with another faction.",
                    "§e/rivals unally <factionName> §f- Ends your alliance with another faction.",
                    "§e/rivals claim §f- Claim the chunk you are standing in for your faction.",
                    "§e/rivals info <factionName> §f- Display info for a faction.",
                    "§e/rivals map §f- Display a map of nearby claims.",
                    "§e/rivals list <pageNumber> §f- Display the faction list, you may specify a page number.",
                    "§e/rivals color <colorCode> §f- Sets the color for your faction using Minecraft color codes.",
                    "§e/rivals shop §f- Opens the edit menu for your faction's shop.",
                    "§e/rivals rename <newName> §f- Changes your faction's name.",
                    "§e/rivals kick <name> §f- Kicks player from faction if you are leader.",
                    "§e/rivals leader <newName> §f- Changes your faction's name."
                    // kick 
                    // leader
                    // "§e/rivals INSERT NAME HERE §f- INSERT DESCRIPTION HERE."
                };

                int perPage = 6;
                int page = 1;

                if(args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        p.sendMessage("§c[Rivals] §fInvalid page number.");
                        return true;
                    }
                }

                int start = (page - 1) * perPage;
                int end = start + perPage;

                if(start < 0 || start >= commands.length) {
                    p.sendMessage("§c[Rivals] §fInvalid page number.");
                    return true;
                }

                // Add spacing
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");

                    p.sendMessage("§6[Rivals] §fHelp Menu (Page §a" + page + "§f):");

                    for(int i = start; i < end && i < commands.length; i++) {
                        p.sendMessage(commands[i]);
                    }

                    // Page navigation
                    TextComponent pageNavigation = new TextComponent();

                    // Previous page
                    if(page > 1) {
                        TextComponent prevPage = new TextComponent("§b[Previous Page]");
                        prevPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rivals help " + (page - 1)));
                        pageNavigation.addExtra(prevPage);
                    }

                    // Separator
                    TextComponent separator = new TextComponent("   ");
                    pageNavigation.addExtra(separator);

                    // Next page
                    if(end < commands.length) {
                        TextComponent nextPage = new TextComponent("§b[Next Page]");
                        nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rivals help " + (page + 1)));
                        pageNavigation.addExtra(nextPage);
                    }

                    p.spigot().sendMessage(pageNavigation);

                    return true;
            }

            else if("shop".equals(args[0])) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to access your faction's shop");
                    return true;
                }
                ShopManager shopManager = Rivals.getShopManager();
                Shopkeeper shopkeeper = shopManager.getShopkeeperForFaction(faction);
                if(args.length == 2) {
                    if("create".equals(args[1])) {
                        if (shopkeeper != null) {
                            p.sendMessage("[Rivals] Your faction already has a shop!");
                            return true;
                        } else {
                            if (faction.getPower() < minShopPower) {
                                p.sendMessage("[Rivals] Your faction has too little power to open a shop.");
                                return true;
                            }
                            if (shopManager.setupShop(faction)) {
                                p.sendMessage("[Rivals] Created a shop for your faction.");
                                int x = shopManager.getShopkeeperForFaction(faction).getX();
                                int y = shopManager.getShopkeeperForFaction(faction).getY();
                                int z = shopManager.getShopkeeperForFaction(faction).getZ();
                                p.sendMessage("It is at (" + x + ", " + y + ", " + z + ").");
                                return true;
                            } else {
                                p.sendMessage("[Rivals] Unable to create a shop. There might not be any open spaces.");
                                return true;
                            }
                        }
                    }
                    else if("close".equals(args[1])) {
                        if (shopkeeper == null) {
                            p.sendMessage("[Rivals] Your faction doesn't have a shop.");
                            return true;
                        } else {
                            if(shopManager.removeShop(faction)) {
                                p.sendMessage("[Rivals] Closed your factions shop.");
                            } else {
                                p.sendMessage("[Rivals] For some unknown reason, we can't close your shop.");
                            }
                            return true;
                        }
                    }
                }
                if(shopkeeper != null) {
                    if(shopkeeper instanceof PlayerShopkeeper) {
                        ((PlayerShopkeeper) shopkeeper).setOwner(p);
                    }
                    if(shopkeeper.openEditorWindow(p)) {
                        p.sendMessage("[Rivals] Opening your shop's editor.");
                    } else {
                        p.sendMessage("[Rivals] Unable to open your shop's editor.");
                    }
                    return true;
                } else {//faction might not have shop.
                    p.sendMessage("[Rivals] Your faction doesn't have a shop. Create one with /rivals shop create");
                    return true;
                }
            }
            else if("rename".equals(args[0])) {
                if(faction == null) {
                    p.sendMessage("[Rivals] You must be in a faction to rename it!");
                    return true;
                }
                if(args.length < 2) {
                    p.sendMessage("[Rivals] Please include faction name");
                    return true;
                }
                String name = args[1];
                if(manager.nameAlreadyExists(name)) {
                    p.sendMessage("[Rivals] " + name + " already exists.");
                    return true;
                }
                if(name.length() > maxNameLength) {
                    p.sendMessage("[Rivals] That name is too long.");
                    return true;
                }
                faction.setName(args[1]);
                p.sendMessage("[Rivals] Your faction's name is now " + faction.getColor() + faction.getName());
                return true;
            }
            else {
                p.sendMessage("[Rivals] Invalid syntax");
            }
        }
        else {
            p.sendMessage("[Rivals] Pick a subcommand: create, invite, join, leave, enemy, ally, peace, unally, claim, info, list, map, color, shop, rename");
        }
        return true;
    }

    public void sendFactionInfo(Player p, Faction f, String s) {
        FactionManager manager = Rivals.getFactionManager();
        String mess = "";
        if("".equals(s)) {
            mess = f.getName() + "\nPower: " + f.getPower();
            String members = ChatColor.COLOR_CHAR + ChatColor.RESET.toString() + "\nMembers: ";
            if(f.getMembers().size() > 3) {
                for(int i = 0; i < 3; i++) {
                    members += Bukkit.getOfflinePlayer(f.getMembers().get(i)).getName() + ", ";
                }
                members += "+ " + (f.getMembers().size() - 3);
            } else if(f.getMembers().size() > 1){
                for(int i = 0; i < f.getMembers().size() - 1; i++) {
                    members += Bukkit.getOfflinePlayer(f.getMembers().get(i)).getName() + ", ";
                }
                members += "and " + Bukkit.getOfflinePlayer(f.getMembers().get(f.getMembers().size() - 1)).getName();
            } else {
                members += Bukkit.getOfflinePlayer(f.getMembers().get(0)).getName();
            }
            mess += members;

            String allies = "\nAllies: ";
            if(f.getAllies().size() > 0) {
                if(f.getAllies().size() > 3) {
                    for(int i = 0; i < 3; i++) {
                        allies += manager.getFactionByID(f.getAllies().get(i)) + ", ";
                    }
                    allies += "+ " + (f.getAllies().size() - 3);
                } else if(f.getAllies().size() > 1){
                    for(int i = 0; i < f.getAllies().size() - 1; i++) {
                        allies += manager.getFactionByID(f.getAllies().get(i)).getName() + ", ";
                    }
                    allies += "and " + manager.getFactionByID(f.getAllies().get(f.getAllies().size() - 1)).getName();
                } else {
                    allies += manager.getFactionByID(f.getAllies().get(0)).getName();
                }
            } else {
                allies += "None";
            }
            mess += allies;

            String enemies = "\nEnemies: ";
            if(f.getEnemies().size() > 0) {
                if(f.getEnemies().size() > 3) {
                    for(int i = 0; i < 3; i++) {
                        enemies += manager.getFactionByID(f.getEnemies().get(i)).getName() + ", ";
                    }
                    enemies += "+ " + (f.getEnemies().size() - 3);
                } else if(f.getEnemies().size() > 1){
                    for(int i = 0; i < f.getEnemies().size() - 1; i++) {
                        enemies += manager.getFactionByID(f.getEnemies().get(i)).getName() + ", ";
                    }
                    enemies += "and " + manager.getFactionByID(f.getEnemies().get(f.getEnemies().size() - 1)).getName();
                } else {
                    enemies += manager.getFactionByID(f.getEnemies().get(0)).getName();
                }
            } else {
                enemies += "None";
            }

            mess += enemies;

            String chunks = "\nChunks: " + f.getRegions().size();

            mess += chunks;

            String hint = "\nFor more info, add 'members', 'allies', or 'enemies' to the command.";

            mess += hint;
        } else {
            if("members".equals(s)) {
                mess = "[Rivals] Members of " + f.getName();
                String members = "\n";
                if(f.getMembers().size() > 1) {
                    for(int i = 0; i < f.getMembers().size() - 1; i++) {
                        members += Bukkit.getOfflinePlayer(f.getMembers().get(i)).getName() + ", ";
                    }
                    members += "and " + Bukkit.getOfflinePlayer(f.getMembers().get(f.getMembers().size() - 1)).getName();
                } else {
                    members += Bukkit.getOfflinePlayer(f.getMembers().get(f.getMembers().size() - 1)).getName();
                }
                mess += members;
            }
            else if("allies".equals(s)) {
                mess = "[Rivals] Allies of " + f.getName();
                String allies = "\n";
                if(f.getAllies().size() > 1) {
                    for(int i = 0; i < f.getAllies().size() - 1; i++) {
                        allies += manager.getFactionByID(f.getAllies().get(i)).getName() + ", ";
                    }
                    allies += "and " + manager.getFactionByID(f.getAllies().get(f.getAllies().size() - 1)).getName();
                } else if(f.getAllies().size() > 0) {
                    allies += manager.getFactionByID(f.getAllies().get(f.getAllies().size() - 1)).getName();
                } else {
                    allies += "None";
                }
                mess += allies;
            }
            else if("enemies".equals(s)) {
                mess = "[Rivals] Enemies of " + f.getName();
                String enemies = "\n";
                if(f.getEnemies().size() > 1) {
                    for (int i = 0; i < f.getEnemies().size() - 1; i++) {
                        enemies += manager.getFactionByID(f.getEnemies().get(i)).getName() + ", ";
                    }
                    enemies += "and " + manager.getFactionByID(f.getEnemies().get(f.getEnemies().size() - 1)).getName();
                } else if(f.getEnemies().size() > 0) {
                    enemies += "and " + manager.getFactionByID(f.getEnemies().get(f.getEnemies().size() - 1)).getName();
                } else {
                    enemies += "None";
                }
                mess += enemies;
            }
            else {
                mess = "[Rivals] Choose either 'members', 'allies', or 'enemies' to get details about a faction.";
            }
        }
        p.sendMessage(mess);
    }
}
