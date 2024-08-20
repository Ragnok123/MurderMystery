package murder.arena.manager;

import murder.arena.*;
import java.util.*;
import cn.nukkit.*;
import murder.*;
import murder.language.*;
import org.apache.commons.lang.math.*;

public class VotingManager
{
    public Arena plugin;
    public HashMap<String, String> players;
    public String[] currentTable;
    public HashMap<String, Integer> stats;
    
    public VotingManager(final Arena plugin) {
        this.players = new HashMap<String, String>();
        this.plugin = plugin;
    }
    
    public void createVoteTable() {
        final ArrayList<String> all = new ArrayList<String>((this.plugin.gameType == Arena.GameType.TTT) ? this.plugin.getPlugin().tttMaps.keySet() : this.plugin.getPlugin().murderMaps.keySet());
        Collections.shuffle(all);
        int i = 0;
        final ArrayList<String> table = new ArrayList<String>();
        while (i < 4 && i < all.size()) {
            table.add(all.get(i));
            ++i;
        }
        this.currentTable = table.stream().toArray(String[]::new);
        this.stats = new HashMap<String, Integer>();
        for (int l = 0; l < this.currentTable.length; ++l) {
            this.stats.put(this.currentTable[l], 0);
        }
        this.players.clear();
    }
    
    public void onVote(final Player p, String vote) {
        if (this.plugin.phase != Arena.GamePhase.LOBBY || !this.plugin.inArena(p)) {
            p.sendMessage(Murder.getPrefix() + Language.translate("can_not_vote", p, new String[0]));
            return;
        }
        if (NumberUtils.isNumber(vote)) {
            final int intValue = Integer.valueOf(vote);
            if (intValue > this.currentTable.length || intValue <= 0) {
                p.sendMessage(Murder.getPrefix() + Language.translate("use_vote", p, new String[0]));
                return;
            }
            if (this.players.containsKey(p.getName().toLowerCase())) {
                this.stats.put(this.players.get(p.getName().toLowerCase()), this.stats.get(this.players.get(p.getName().toLowerCase())) - 1);
            }
            this.stats.put(this.currentTable[intValue - 1], this.stats.get(this.currentTable[intValue - 1]) + 1);
            this.players.put(p.getName().toLowerCase(), this.currentTable[intValue - 1]);
            p.sendMessage(Murder.getPrefix() + Language.translate("vote", p, this.currentTable[intValue - 1]));
        }
        else {
            vote = vote.toLowerCase();
            boolean found = false;
            for (final String s : this.currentTable) {
                if (vote.equals(s.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                p.sendMessage(Murder.getPrefix() + Language.translate("use_vote", p, new String[0]));
                return;
            }
            if (this.players.containsKey(p.getName().toLowerCase())) {
                this.stats.put(this.players.get(p.getName().toLowerCase()), this.stats.get(this.players.get(p.getName().toLowerCase())) - 1);
            }
            final String finall = Character.toUpperCase(vote.charAt(0)) + vote.substring(1);
            this.stats.put(finall, this.stats.get(finall) + 1);
            this.players.put(p.getName().toLowerCase(), finall);
            p.sendMessage(Murder.getPrefix() + Language.translate("vote", p, vote));
        }
    }
}
