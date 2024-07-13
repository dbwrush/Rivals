package net.sudologic.rivals.managers;

import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import net.sudologic.rivals.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    public ScoreboardManager(Server server) {
        server.getScheduler().runTaskTimer(Rivals.getPlugin(), () -> {
            for(UUID id : boards.keySet()) {
                updateScoreboard(id);
            }
        }, 0, 20 * 10);
    }

    public void assignScoreboard(Player p) {
        FastBoard board = new FastBoard(p);
        boards.put(p.getUniqueId(), board);
        board.updateTitle(ChatColor.YELLOW + "-= Rivals =-");
        updateScoreboard(p.getUniqueId());
    }

    public void removeScoreboard(Player p) {
        FastBoard b = boards.remove(p.getUniqueId());
        if(b != null) {
            b.delete();
        }
    }

    public void updateScoreboard(UUID id) {
        FastBoard b = boards.get(id);

        Faction f = Rivals.getFactionManager().getFactionByPlayer(id);
        if(f != null) {
            b.updateLines("Faction: " + f.getColor() + f.getName(),
                    "Members: " + ChatColor.WHITE + f.countOnlineMembers() + "/" + f.getMembers().size(),
                    "Power: " + ChatColor.WHITE + Rivals.getRoundedDecimal(f.getPower()),
                    "Influence: " + ChatColor.WHITE + Rivals.getRoundedDecimal(f.getInfluence()),
                    "Warmongering: " + ChatColor.WHITE + Rivals.getRoundedDecimal(f.getWarmongering()) + " + " + Rivals.getRoundedDecimal(Rivals.getEffectManager().getPlayerWarMongering(id)),
                    "In Combat: " + ChatColor.WHITE + combatString(Rivals.getEventManager().combatTimeLeft(id)),
                    "Status: " + ChatColor.WHITE + f.getStatus());
        } else {
            b.updateLines("Faction: " + ChatColor.WHITE + "None",
                    "Join or create a faction", "using /rivals");
        }
    }

    private String combatString(double time) {
        if(time <= 0) {
            return ChatColor.WHITE + "False";
        } else {
            return ChatColor.RED + String.valueOf(Rivals.getRoundedDecimal(time / 1000));
        }
    }
}
