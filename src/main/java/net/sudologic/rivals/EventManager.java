package net.sudologic.rivals;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EventManager {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Faction f = Rivals.getFactionManager().getFactionByPlayer(e.getEntity().getUniqueId());
        if(f != null) {
            f.powerChange(-2);
        }
    }
}
