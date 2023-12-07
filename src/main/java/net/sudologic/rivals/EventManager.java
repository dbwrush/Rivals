package net.sudologic.rivals;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

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
}
