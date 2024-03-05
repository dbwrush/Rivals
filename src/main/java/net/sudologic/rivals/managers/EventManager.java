package net.sudologic.rivals.managers;

import com.nisovin.shopkeepers.api.events.*;
import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventManager implements Listener {
    private double combatTeleportDelay, killEntityPower, killMonsterPower, killPlayerPower, deathPowerLoss, tradePower;

    private Map<UUID, Double> combatTime;

    public EventManager(ConfigurationSection settings) {
        /*killEntityPower = 0;
        killMonsterPower = 1;
        killPlayerPower = 3;
        deathPowerLoss = -4;
        tradePower = 1;*/
        killEntityPower = (double) settings.get("killEntityPower");
        killMonsterPower = (double) settings.get("killMonsterPower");
        killPlayerPower = (double) settings.get("killPlayerPower");
        deathPowerLoss = (double) settings.get("deathPowerLoss");
        tradePower = (double) settings.get("tradePower");
        if(settings.contains("combatTeleportDelay")) {
            combatTeleportDelay = (double) settings.get("combatTeleportDelay");
        } else {
            settings.set("combatTeleportDelay", 120.0);
        }
        combatTime = new HashMap<>();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        if(e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            double power = Math.round(killEntityPower * 100.0) / 100.0;
            if(e.getEntity() instanceof Monster) {
                power = Math.round(killMonsterPower * 100.0) / 100.0;
            } else if(e.getEntity() instanceof Player) {
                power = Math.round(killPlayerPower * 100.0) / 100.0;
            }
            Faction killerFaction = manager.getFactionByPlayer(killer.getUniqueId());
            if(killerFaction != null) {
                killerFaction.powerChange(power);
            }
        }
        if(e.getEntity() instanceof Player) {
            Faction playerFaction = manager.getFactionByPlayer(e.getEntity().getUniqueId());
            if(playerFaction != null) {
                playerFaction.powerChange(deathPowerLoss);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        List<Integer> invites = manager.getInvitesForPlayer(e.getPlayer().getUniqueId());
        if(manager.getFactionByPlayer(e.getPlayer().getUniqueId()) == null && invites.size() > 0) {
            String inviteMess = "[Rivals] You're invited to join " + manager.getFactionByID(invites.get(0)).getColor() + manager.getFactionByID(invites.get(0)).getName();
            e.getPlayer().sendMessage(inviteMess);
        } else {
            if(manager.getFactionByPlayer(e.getPlayer().getUniqueId()) == null) {
                e.getPlayer().sendMessage("[Rivals] You haven't joined a faction yet!");
                return;
            }
            e.getPlayer().sendMessage("[Rivals] Faction status:");
            Rivals.getCommand().sendFactionInfo(e.getPlayer(), manager.getFactionByPlayer(e.getPlayer().getUniqueId()), "");
        }
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        manager.removeInvitesOver7Days();
    }

    @EventHandler
    public void onTrade(ShopkeeperTradeEvent e) {
        Faction f = Rivals.getShopManager().getFactionForShopLocation(e.getShopkeeper().getLocation());
        Player p = e.getPlayer();
        Faction pFaction = Rivals.getFactionManager().getFactionByPlayer(p.getUniqueId());
        if(f != pFaction) {
            f.powerChange(tradePower);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if(e.getEntityType() == EntityType.PLAYER) {
            Player victim = (Player) e.getEntity();
            if(combatTime.containsKey(victim.getUniqueId())) {
                combatTime.remove(victim);
            }
            combatTime.put(victim.getUniqueId(), System.currentTimeMillis() + combatTeleportDelay * 1000);
        }
    }

    public boolean getCombat(UUID uuid) {
        double time = combatTime.get(uuid);
        if(time == 0) {
            return false;
        }
        if(System.currentTimeMillis() < time) {
            combatTime.remove(uuid);
            return false;
        }
        return true;
    }
}
