package net.sudologic.rivals.managers;

import com.nisovin.shopkeepers.api.events.*;
import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventManager implements Listener {
    private Map<UUID, Double> combatTime;

    public EventManager() {
        combatTime = new HashMap<>();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        if(e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            Faction killerFaction = manager.getFactionByPlayer(killer.getUniqueId());
            double power = Math.round((double)Rivals.getSettings().get("killEntityPower") * 100.0) / 100.0;
            if(e.getEntity() instanceof Monster) {
                power = Math.round((double)Rivals.getSettings().get("killMonsterPower") * 100.0) / 100.0;
            } else if(e.getEntity() instanceof Player) {
                //power = Math.round((double)Rivals.getSettings().get("killPlayerPower") * 100.0) / 100.0;
                Faction victimFaction = manager.getFactionByPlayer(e.getEntity().getUniqueId());
                if(victimFaction == null || killerFaction == null) {
                    power = Math.round((double)Rivals.getSettings().get("killNeutralPower") * 100.0) / 100.0;
                } else if(killerFaction.getHostileFactions().contains(victimFaction.getID())) {
                    power = Math.round((double)Rivals.getSettings().get("killEnemyPower") * 100.0) / 100.0;
                } else if(killerFaction.getAllies().contains(victimFaction.getID())) {
                    power = Math.round((double)Rivals.getSettings().get("killAllyPower") * 100.0) / 100.0;
                } else {
                    power = Math.round((double) Rivals.getSettings().get("killNeutralPower") * 100.0) / 100.0;
                }
            }
            if(killerFaction != null) {
                killerFaction.powerChange(power);
            }
        }
        if(e.getEntity() instanceof Player) {
            Faction playerFaction = manager.getFactionByPlayer(e.getEntity().getUniqueId());
            if(playerFaction != null) {
                playerFaction.powerChange((double)Rivals.getSettings().get("deathPowerLoss") * -1);
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
            e.getPlayer().sendMessage("[Rivals] Server status:");//display currently interventioned factions & number of policy proposals
            Rivals.getPoliticsManager().displayPolicy(new String[]{"intervention"}, e.getPlayer());
            Rivals.getPoliticsManager().displayPolicy(new String[]{"custodian"}, e.getPlayer());
            e.getPlayer().sendMessage("[Rivals] Use /policy for more information on politics");
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
            f.powerChange((double)Rivals.getSettings().get("tradePower"));
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if(e.getEntityType() == EntityType.PLAYER) {
            Player victim = (Player) e.getEntity();
            if(combatTime.containsKey(victim.getUniqueId())) {
                combatTime.remove(victim);
            }
            combatTime.put(victim.getUniqueId(), System.currentTimeMillis() + (double)Rivals.getSettings().get("combatTeleportDelay") * 1000);
        }
    }

    @EventHandler
    public void onMilk(PlayerItemConsumeEvent e) {
        if(e.getItem().getType().equals(Material.MILK_BUCKET)) {
            Rivals.getEffectManager().updatePlayer(e.getPlayer(), Rivals.getPoliticsManager());
        }
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        Rivals.getEffectManager().updatePlayer(e.getPlayer(), Rivals.getPoliticsManager());
    }

    public boolean getCombat(UUID uuid) {
        if(!combatTime.containsKey(uuid)) {
            return false;
        }
        Double time = combatTime.get(uuid);
        if(time == null) {
            return false;
        }
        if(System.currentTimeMillis() > time) {
            combatTime.remove(uuid);
            return false;
        }
        return true;
    }

    public double combatTimeLeft(UUID uuid) {
        double time = combatTime.get(uuid);
        if(time == 0) {
            return 0;
        }
        return time - System.currentTimeMillis();
    }
}
