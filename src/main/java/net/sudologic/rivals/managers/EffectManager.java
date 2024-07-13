package net.sudologic.rivals.managers;

import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EffectManager {
    /*todo
    - When a player kills another player, update their factions' penalties
     - Track faction penalties, with related methods.
    - Hourly update for effect cooldowns
    - If a player tries to clear their status effects, re-apply them
    - If a faction is denounced, sanctioned, or intervened, apply the appropriate effects
     */

    public void applyEffects(Player player, double intensity) {
        List<PotionEffect> effects = new ArrayList<>();
        if(intensity >= 5) {
            effects.add(new PotionEffect(PotionEffectType.HUNGER, 20 * 60 * 60, (int)intensity - 2));
        }
        if(intensity >= 4) {
            effects.add(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 60 * 60, (int)intensity - 3));
        }
        if(intensity >= 3) {
            effects.add(new PotionEffect(PotionEffectType.SLOW, 20 * 60 * 60, (int)intensity - 2));
        }
        if(intensity >= 1) {
            effects.add(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 60 * 60, (int)intensity));
        }
        player.addPotionEffects(effects);
    }

    public void update() {
        PoliticsManager polMan = Rivals.getPoliticsManager();
        for(Faction f : Rivals.getFactionManager().getFactions()) {
            for(UUID playerID : f.getMembers()) {
                Player p = Bukkit.getPlayer(playerID);
                if(p != null) {
                    updatePlayer(p, polMan);
                }
            }
            f.setWarmongering(f.getWarmongering() * .9);
        }
    }

    public void updatePlayer(Player p, PoliticsManager polMan) {
        long time = System.currentTimeMillis();
        Faction f = Rivals.getFactionManager().getFactionByPlayer(p.getUniqueId());
        double penalty = f.getWarmongering();
        if(polMan.getDenouncedFactions().getOrDefault(f.getID(), 0L) > time) {
            penalty += 1;
        }
        if(polMan.getSanctionedFactions().getOrDefault(f.getID(), 0L) > time) {
            penalty += 2;
        }
        if(polMan.getInterventionFactions().getOrDefault(f.getID(), 0L) > time) {
            penalty += 3;
        }
        if(penalty <= 0) {
            return;
        }
        applyEffects(p, penalty);
    }
}