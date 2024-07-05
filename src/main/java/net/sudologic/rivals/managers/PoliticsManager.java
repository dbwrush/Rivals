package net.sudologic.rivals.managers;

import net.sudologic.rivals.Faction;
import net.sudologic.rivals.Rivals;
import net.sudologic.rivals.util.Policy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PoliticsManager implements ConfigurationSerializable {
    private int custodian = -1;
    private float custodianBudget = 0;
    private String custodianMandate;
    private HashMap<Integer, Long> sanctionedFactions;//this faction has reduced power
    private HashMap<Integer, Long> denouncedFactions;//this faction is given a warning
    private HashMap<Integer, Long> interventionFactions;//all factions are at war with this faction
    private long custodianEnd = 0;
    private Map<Integer, Policy> proposed;

    public PoliticsManager() {
        sanctionedFactions = new HashMap<>();
        denouncedFactions = new HashMap<>();
        interventionFactions = new HashMap<>();
        proposed = new HashMap<>();
    }
    public PoliticsManager(Map<String, Object> serialized) {
        proposed = new HashMap<>();
        for(Object o : (ArrayList<Object>) serialized.get("proposed")) {
            Policy p = new Policy((Map<String, Object>) o);
            proposed.put(p.getId(), p);
        }
        custodian = (int) serialized.get("custodian");
        custodianBudget = (float) (double)serialized.get("custodianBudget");
        custodianMandate = (String) serialized.get("custodianMandate");
        sanctionedFactions = (HashMap<Integer, Long>) serialized.get("sanctionedFactions");
        denouncedFactions = (HashMap<Integer, Long>) serialized.get("denouncedFactions");
        interventionFactions = (HashMap<Integer, Long>) serialized.get("interventionFactions");
    }
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        ArrayList<Object> pObjects = new ArrayList<>();
        for(Policy p : proposed.values()) {
            pObjects.add(p.serialize());
        }
        serialized.put("proposed", pObjects);
        serialized.put("custodian", custodian);
        serialized.put("custodianBudget", custodianBudget);
        serialized.put("custodianMandate", custodianMandate);
        serialized.put("sanctionedFactions", sanctionedFactions);
        serialized.put("denouncedFactions", denouncedFactions);
        serialized.put("interventionFactions", interventionFactions);

        return serialized;
    }

    public void implement(Policy p) {
        String announce = "";
        switch(p.getType()) {
            case budget -> {
                custodianBudget = p.getBudget();
                announce = "[Rivals] Influence will be taxed at " + (custodianBudget) * 100 + "% to the Custodian";
            }
            case custodian -> {
                custodian = p.getTarget();
                custodianEnd = p.getTime();
                announce = "Rivals] " + Rivals.getFactionManager().getFactionByID(custodian).getName() + " is now the Custodian";
            }
            case mandate -> {
                custodianMandate = p.getMandate();
                announce = "[Rivals] The Custodian's mandate has been set to " + custodianMandate;
            }
            case setting -> {
                Rivals.changeSetting(p.getSettingName(), p.getSettingValue());
                announce = "[Rivals] The setting " + p.getSettingName() + " has been set to " + p.getSettingValue();
            }
            case unsanction -> {
                sanctionedFactions.remove(p.getTarget());
                announce = "[Rivals] " + Rivals.getFactionManager().getFactionByID(p.getTarget()).getName() + " has been unsanctioned";
            }
            case denounce -> {
                denouncedFactions.put(p.getTarget(), p.getTime());
                announce = "[Rivals] " + Rivals.getFactionManager().getFactionByID(p.getTarget()).getName() + " has been denounced";
            }
            case sanction -> {
                sanctionedFactions.put(p.getTarget(), p.getTime());
                announce = "[Rivals] " + Rivals.getFactionManager().getFactionByID(p.getTarget()).getName() + " has been sanctioned";
            }
            case intervention -> {
                interventionFactions.put(p.getTarget(), p.getTime());
                announce = "[Rivals] " + Rivals.getFactionManager().getFactionByID(p.getTarget()).getName() + " is now under intervention";
            }
            case unintervention -> {
                interventionFactions.remove(p.getTarget());
                announce = "[Rivals] " + Rivals.getFactionManager().getFactionByID(p.getTarget()).getName() + " is no longer under intervention";
            }
        }
        for(Player pl : Bukkit.getOnlinePlayers()) {
            pl.sendMessage(announce);
        }
    }

    public void displayPolicy(String[] sel, Player player) {
        String reply = "";
        switch (sel[0]) {
            case "budget" -> reply = "[Rivals] The Custodian's budget is currently " + String.valueOf(custodianBudget);
            case "custodian" -> {
                if(custodian != -1) {
                    reply = "[Rivals] " + Rivals.getFactionManager().getFactionByID(custodian).getName() + " is the current Custodian";
                } else {
                    reply = "[Rivals] There is no current Custodian";
                }
            }
            case "mandate" -> {
                if(custodianMandate != "") {
                    reply = "[Rivals] " + custodianMandate + " is the current mandate";
                } else {
                    reply = "[Rivals] There is no current mandate";
                }
            }
            case "setting" -> {
                if(Rivals.getSettings().contains(sel[1])) {
                    reply = "[Rivals] The current " + sel[1] + " is: " + Rivals.getSettings().get(sel[1]).toString();
                } else {
                    reply = "[Rivals] No setting with that name";
                }
            }
            case "sanctioned" -> {
                reply = "[Rivals] The current sanctioned factions are: ";
                for(int f : sanctionedFactions.keySet()) {
                    reply += Rivals.getFactionManager().getFactionByID(f).getName() + " ";
                }
            }
            case "intervention" -> {
                reply = "[Rivals] The current factions under intervention are: ";
                for(int f : interventionFactions.keySet()) {
                    reply += Rivals.getFactionManager().getFactionByID(f).getName() + " ";
                }
            }
        }
        if(reply.equals("")) {
            reply = "[Rivals] Invalid request.";
        }
        player.sendMessage(reply);
    }

    public boolean stopProposal(int id) {
        if(proposed.containsKey(id)) {
            proposed.remove(id);
            return true;
        }
        return false;
    }

    public void update() {
        long time = System.currentTimeMillis();
        if(custodianEnd < time) {
            custodian = -1;
        }
        int taxRev = 0;
        if(custodian != -1) {
            for (Faction f : Rivals.getFactionManager().getFactions()) {
                f.payInfluence();
                int am = f.taxInfluence(custodianBudget);
                taxRev += am;
                f.sendMessageToOnlineMembers("[Rivals] You have paid " + am + " influence to the custodian");
            }
            Rivals.getFactionManager().getFactionByID(custodian).addInfluence(taxRev);
            Rivals.getFactionManager().getFactionByID(custodian).sendMessageToOnlineMembers("[Rivals] You have received " + taxRev + " influence from taxes");
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
        long a = (System.currentTimeMillis() - ((int)Rivals.getSettings().get("votePassTime") * 3600000l));//time in hours for vote to pass
        Collection<Policy> props = proposed.values();
        for(Policy p : props) {
            if(p.getProposedTime() < a) {
                if(p.support() > (float)Rivals.getSettings().get("votePassRatio") && p.getNumYays() > (float)Rivals.getSettings().get("minVotes")) {
                    implement(p);
                }
                Rivals.getFactionManager().getFactionByID(p.getProposedBy()).sendMessageToOnlineMembers("Your proposal, ID: " + p.getId() + " has been rejected.");
                proposed.remove(p.getId());
            }
        }
    }

    public long getVotePassTime() {
        return (int)Rivals.getSettings().get("votePassTime") * 3600000l;
    }

    public Map<Integer, Policy> getProposed() {
        return proposed;
    }

    public Policy propose(Policy policy) {
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
            return policy;
        }
        return null;
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
