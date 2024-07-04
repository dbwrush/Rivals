package net.sudologic.rivals.commands;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import net.sudologic.rivals.util.Policy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PolicyCommand implements CommandExecutor {

    private static final Set<String> ALLOWED_SETTINGS = new HashSet<>(Arrays.asList(
            "warDelay",
            "nowWarPower",
            "votePassRatio",
            "votePassTime",
            "minVotes",
            "minShopPower",
            "killEntityPower",
            "killAllyPower",
            "killEnemyPower",
            "killNeutralPower",
            "deathPowerLoss",
            "tradePower",
            "defaultPower",
            "killMonsterPower"
    ));

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        //policy propose <type> <arg1> <arg2>
        //policy vote <id>
        //policy get <sel1> <sel2>
        if(commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if(args.length < 1) {
                if(args.length < 1) {
                    commandSender.sendMessage("[Rivals] Usage: /policy <propose|vote|get|proposals|help> [arguments]\n" +
                            "For detailed instructions on each subcommand, use /policy help");
                    return true;
                }
            }
            if(args[0].equals("propose")) {
                if(args.length < 3) {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                    return true;
                }
                Faction f = Rivals.getFactionManager().getFactionByPlayer(p.getUniqueId());
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
                            id = Rivals.getPoliticsManager().propose(new Policy(type, target, time, f.getID()));
                        else {
                            commandSender.sendMessage("[Rivals] Invalid syntax");
                            return true;
                        }
                    }
                    case budget -> {
                        Float budget = Floats.tryParse(args[2]);
                        if(budget != null)
                            id = Rivals.getPoliticsManager().propose(new Policy(type, budget, null, f.getID()));
                        else {
                            commandSender.sendMessage("[Rivals] Invalid syntax");
                            return true;
                        }
                    }
                    case mandate -> id = Rivals.getPoliticsManager().propose(new Policy(type, args[2], null, f.getID()));
                    case setting -> {
                        if(args.length < 4) {
                            commandSender.sendMessage("[Rivals] Invalid syntax");
                            return true;
                        }
                        if(!ALLOWED_SETTINGS.contains(args[2])) {
                            commandSender.sendMessage("[Rivals] Proposals for this setting are not permitted.");
                            return true;
                        }
                        if(!Rivals.validSetting(args[2], args[3])) {
                            commandSender.sendMessage("[Rivals] Invalid setting value.");
                            return true;
                        }
                        id = Rivals.getPoliticsManager().propose(new Policy(type, args[2], args[3], f.getID()));
                    }
                    case unsanction -> {
                        Integer target = Ints.tryParse(args[2]);
                        if(target != null) {
                            id = Rivals.getPoliticsManager().propose(new Policy(type, target, null, f.getID()));
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
                if(args[2].equals("yay") || args[2].equals("yes") || args[2].equals("aye") || args[2].equals("y")) {
                    yay = true;
                } else if(!args[2].equals("nay") && !args[2].equals("no") && !args[2].equals("n")) {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                    return true;
                }
                Faction faction = Rivals.getFactionManager().getFactionByPlayer(((Player) commandSender).getUniqueId());
                if(faction != null) {
                    Rivals.getPoliticsManager().getProposed().get(id).vote(faction.getID(), yay);
                    if(yay) {
                        commandSender.sendMessage("[Rivals] Your faction has voted in favor of resolution " + id);
                    } else {
                        commandSender.sendMessage("[Rivals] Your faction has voted in opposition of resolution " + id);
                    }
                } else {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                }
                return true;

            }
            else if(args[0].equals("proposals")) {
                listProposals((Player) commandSender);
                return true;
            }
            else if(args[0].equals("get")) {
                if(args.length < 2) {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                    return true;
                }
                if(args[1].equals("setting") && args.length < 3) {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                    return true;
                }
                try {
                    if (Integer.parseInt(args[1]) != 0) {
                        Policy policy = Rivals.getPoliticsManager().getProposed().get(Integer.parseInt(args[1]));
                        if (policy == null) {
                            commandSender.sendMessage("[Rivals] No proposal with that id.");
                            return true;
                        }
                        commandSender.sendMessage(describePolicy(policy));
                        return true;
                    }
                    Rivals.getPoliticsManager().displayPolicy(args, (Player) commandSender);
                    return true;
                } catch (NumberFormatException e) {
                    commandSender.sendMessage("[Rivals] Invalid syntax");
                    return true;
                }
            } else if(args[0].equals("help")) {
                commandSender.sendMessage("[Rivals] Policy Command Help:\n" +
                        "- /policy propose <type> <arg1> <arg2>...: Propose a new policy.\n" +
                        "- /policy vote <id> <yay|nay>: Vote on a proposed policy.\n" +
                        "- /policy get <setting | proposal-id>: Get details on a specific policy, setting, or proposal\n" +
                        "- /policy proposals: List all current proposals\n" +
                        "Types include: denounce, sanction, intervention, custodian, budget, mandate, setting, unsanction.");
                return true;
            }
        }
        return false;
    }

    public void listProposals(Player p) {
        p.sendMessage("[Rivals] Current proposals:");
        for(Policy policy : Rivals.getPoliticsManager().getProposed().values()) {
            p.sendMessage(describePolicy(policy));
        }
    }

    public String describePolicy(Policy p) {
        String s = "" + p.getId() + ": " + (p.support() * 100) + "% support | Proposed by " + Rivals.getFactionManager().getFactionByID(p.getProposedBy()).getName() + " | " + (p.getTimeLeft() / 3600000) + " hours remaining\n";
        switch (p.getType()) {
            case denounce -> s += "Denounce Faction " + p.getTargetFaction().getColor() + p.getTargetFaction().getName();
            case sanction -> s += "Sanction Faction " + p.getTargetFaction().getColor() + p.getTargetFaction().getName();
            case intervention -> s += "Intervene against Faction " + p.getTargetFaction().getColor() + p.getTargetFaction().getName();
            case custodian -> s += "Nominate Custodian" + p.getTargetFaction().getColor() + p.getTargetFaction().getName();
            case unsanction -> s += "Unsanction Faction " + p.getTargetFaction().getColor() + p.getTargetFaction().getName();
            case setting -> s += "Set " + p.getSettingName() + " to " + p.getSettingValue();
            case budget -> s += "Set budget to " + (p.getBudget() * 100) + "%";
            case mandate -> s += "Set Custodian mandate to " + p.getMandate();
        }

        return s;
    }
}
