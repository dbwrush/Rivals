package net.sudologic.rivals;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.List;

public class EventManager implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        if(e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            double power = 1;
            if(e.getEntity() instanceof Monster) {
                power = 2;
            } else if(e.getEntity() instanceof Player) {
                power = 4;
            }
            manager.getFactionByPlayer(killer.getUniqueId()).powerChange(power);
        }
        if(e.getEntity() instanceof Player) {
            manager.getFactionByPlayer(e.getEntity().getUniqueId()).powerChange(-4);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        List<Integer> invites = manager.getInvitesForPlayer(e.getPlayer().getUniqueId());
        if(manager.getFactionByPlayer(e.getPlayer().getUniqueId()) == null) {
            String inviteMess = "[Rivals] You're invited to join " + manager.getFactionByID(invites.get(0)).getColor() + manager.getFactionByID(invites.get(0)).getName();
            e.getPlayer().sendMessage(inviteMess);
        } else {
            e.getPlayer().sendMessage("[Rivals] Faction status:");
            Rivals.getCommand().sendFactionInfo(e.getPlayer(), manager.getFactionByPlayer(e.getPlayer().getUniqueId()), "");
        }
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        manager.removeInvitesOver7Days();
    }
}
