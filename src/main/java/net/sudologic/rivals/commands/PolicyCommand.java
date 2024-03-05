package net.sudologic.rivals.commands;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import net.sudologic.rivals.managers.PoliticsManager;
import net.sudologic.rivals.util.Policy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PolicyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        //policy propose <type> <arg1> <arg2>
        //policy vote <id>
        //policy list
        if(commandSender instanceof Player) {
            if(args.length < 1) {
                commandSender.sendMessage("[Rivals] Invalid syntax");
                return true;
            }
            if(args[0].equals("propose")) {
                if(args.length < 3) {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                    return true;
                }
                Policy.PolicyType type;
                try {
                    type = Policy.PolicyType.valueOf(args[1].toLowerCase());
                } catch(IllegalArgumentException e) {
                    commandSender.sendMessage("[Rivals] No policy type with that name");
                    return true;
                }
                int id = -1;
                switch (type) {
                    case denounce, sanction, intervention, custodian -> {
                        if(args.length < 4) {
                            commandSender.sendMessage("[Rivals] Invalid syntax");
                            return true;
                        }
                        Integer target = Ints.tryParse(args[2]);
                        Long time = Longs.tryParse(args[3]) * 3600000 + System.currentTimeMillis();
                        if(target != null && time != null)
                            id = Rivals.getPoliticsManager().propose(new Policy(type, target, time));
                        else {
                            commandSender.sendMessage("[Rivals] Invalid syntax");
                            return true;
                        }
                    }
                    case budget -> {
                        Float budget = Floats.tryParse(args[2]);
                        if(budget != null)
                            id = Rivals.getPoliticsManager().propose(new Policy(type, budget, null));
                        else {
                            commandSender.sendMessage("[Rivals] Invalid syntax");
                            return true;
                        }
                    }
                    case mandate -> id = Rivals.getPoliticsManager().propose(new Policy(type, args[2], null));
                    case setting -> {
                        if(args.length < 4) {
                            commandSender.sendMessage("[Rivals] Invalid syntax");
                            return true;
                        }
                        id = Rivals.getPoliticsManager().propose(new Policy(type, args[2], args[3]));
                    }
                    case unsanction -> {
                        Integer target = Ints.tryParse(args[2]);
                        if(target != null) {
                            id = Rivals.getPoliticsManager().propose(new Policy(type, target, null));
                        } else {
                            commandSender.sendMessage("[Rivals] Invalid syntax");
                            return true;
                        }
                    }
                    default -> {
                        commandSender.sendMessage("[Rivals] Invalid syntax");
                        return true;
                    }
                }
                commandSender.sendMessage("[Rivals] Proposed resolution " + id);
                return true;

            } else if(args[0].equals("vote")) {
                if(args.length < 3) {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                    return true;
                }
                Integer id = Ints.tryParse(args[2]);
                if(id == null || !Rivals.getPoliticsManager().getProposed().keySet().contains(id)) {
                    commandSender.sendMessage("[Rivals] No resolution with that id.");
                    return true;
                }
                boolean yay = false;
                if(args[2].equals("yay")) {
                    yay = true;
                } else if(!args[2].equals("nay")) {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                    return true;
                }
                Faction faction = Rivals.getFactionManager().getFactionByPlayer(((Player) commandSender).getUniqueId());
                if(faction != null) {
                    Rivals.getPoliticsManager().getProposed().get(id).vote(faction.getID(), yay);
                    if(yay) {
                        commandSender.sendMessage("[Rivals] Your faction has voted in favor of resolution " + id);
                        return true;
                    } else {
                        commandSender.sendMessage("[Rivals] Your faction has voted in opposition of resolution " + id);
                        return true;
                    }
                } else {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                    return true;
                }

            } else if(args[0].equals("list")) {

            }
        }



        return false;
    }
}
