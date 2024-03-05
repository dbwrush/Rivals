package net.sudologic.rivals.managers;

import net.sudologic.rivals.Rivals;
import net.sudologic.rivals.util.Policy;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PoliticsManager implements ConfigurationSerializable {
    private int custodian;
    private float custodianBudget;
    private String custodianMandate;
    private HashMap<Integer, Long> sanctionedFactions;//this faction has reduced power
    private HashMap<Integer, Long> denouncedFactions;//this faction is given a warning
    private HashMap<Integer, Long> interventionFactions;//all factions are at war with this faction
    private long custodianEnd;

    private Map<Integer, Policy> proposed;

    public PoliticsManager() {
        sanctionedFactions = new HashMap<>();
        denouncedFactions = new HashMap<>();
        interventionFactions = new HashMap<>();
    }

    public PoliticsManager(Map<String, Object> serialized) {
        proposed = new HashMap<>();
        for(Object o : (ArrayList<Object>) serialized.get("proposed")) {
            Policy p = new Policy((Map<String, Object>) o);
            proposed.put(p.getId(), p);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        ArrayList<Object> pObjects = new ArrayList<>();
        for(Policy p : proposed.values()) {
            pObjects.add(p.serialize());
        }
        serialized.put("proposed", pObjects);

        return serialized;
    }

    public void implement(Policy p) {
        switch(p.getType()) {
            case budget -> custodianBudget = p.getBudget();
            case custodian -> {
                custodian = p.getTarget();
                custodianEnd = p.getTime();
            }
            case mandate -> custodianMandate = p.getMandate();
            case setting -> Rivals.changeSetting(p.getSettingName(), p.getSettingValue());
            case unsanction -> sanctionedFactions.remove(p.getTarget());
            case denounce -> denouncedFactions.put(p.getTarget(), p.getTime());
            case sanction -> sanctionedFactions.put(p.getTarget(), p.getTime());
            case intervention -> interventionFactions.put(p.getTarget(), p.getTime());
        }
    }

    public void update() {
        long time = System.currentTimeMillis();
        if(custodianEnd < time) {
            custodian = -1;
        }
        for(int f : sanctionedFactions.keySet()) {
            if(sanctionedFactions.get(f) < time) {
                sanctionedFactions.remove(f);
            }
        }
        for(int f : denouncedFactions.keySet()) {
            if(denouncedFactions.get(f) < time) {
                denouncedFactions.remove(f);
            }
        }
        for(int f : interventionFactions.keySet()) {
            if(interventionFactions.get(f) < time) {
                interventionFactions.remove(f);
            }
        }
        long a = System.currentTimeMillis() - ((int)Rivals.getSettings().get("votePassTime") * 3600000);
        Collection<Policy> props = proposed.values();
        for(Policy p : props) {
            if(p.getProposedTime() < a) {
                if(p.support() > (float)Rivals.getSettings().get("votePassRatio")) {
                    implement(p);
                }
                proposed.remove(p.getId());
            }
        }
    }

    public Map<Integer, Policy> getProposed() {
        return proposed;
    }

    public int propose(Policy policy) {
        if(proposed.size() < 2048) {
            policy.setId((int) (Math.random() * 2048));
            if(proposed.keySet().contains(policy.getId())) {
                for(int i = 0; i < 2048; i++) {
                    if (!proposed.keySet().contains(i)) {
                        policy.setId(i);
                        break;
                    }
                }
            }
            proposed.put(policy.getId(), policy);
            return policy.getId();
        }
        return -1;
    }

    public int getCustodian() {
        return custodian;
    }

    public float getCustodianBudget() {
        return custodianBudget;
    }

    public String getCustodianMandate() {
        return custodianMandate;
    }

    public HashMap<Integer, Long> getSanctionedFactions() {
        return sanctionedFactions;
    }

    public HashMap<Integer, Long> getDenouncedFactions() {
        return denouncedFactions;
    }

    public HashMap<Integer, Long> getInterventionFactions() {
        return interventionFactions;
    }

    public long getCustodianEnd() {
        return custodianEnd;
    }
}
