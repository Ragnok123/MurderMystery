package murder.arena.task;

import murder.arena.*;
import murder.arena.object.*;
import cn.nukkit.utils.*;
import cn.nukkit.*;
import java.util.*;
import murder.language.*;
import murder.arena.manager.*;

public class PopupTask implements Runnable
{
    private final Arena plugin;
    private Map<Integer, String> translations;
    private String space0;
    private String space2;
    
    public PopupTask(final Arena plugin) {
        this.translations = null;
        this.space0 = "                                                     ";
        this.space2 = "                                              ";
        this.plugin = plugin;
        this.translations = this.getTranslations(this.space0);
    }
    
    @Override
    public void run() {
        if (this.plugin.phase == Arena.GamePhase.GAME) {
            final Map<Integer, String> translations = this.translate();
            for (final ArenaPlayerData data : this.plugin.playersData.values()) {
                final Player p = data.getPlayer();
                String role = TextFormat.GRAY + "---";
                if (data.getRole() != null) {
                    role = data.getRole().getColor() + data.getRole().getName(this.plugin.gameType);
                }
                p.sendTip(translations.get(data.getPlayerData().getData().getLanguage()).replace("%role", role));
            }
        }
        else if (this.plugin.phase == Arena.GamePhase.LOBBY) {
            this.sendVotes();
        }
    }
    
    private Map<Integer, String> getTranslations(final String space) {
        final HashMap<Integer, String> map = new HashMap<Integer, String>();
        for (final Map.Entry<Integer, String> entry : Language.getTranslations("scoreboard", "%time", "%role", "%size").entrySet()) {
            final String[] lines = entry.getValue().split("\n");
            for (int i = 0; i < lines.length; ++i) {
                lines[i] = space + lines[i];
            }
            map.put(entry.getKey(), String.join("\n", (CharSequence[])lines));
        }
        return map;
    }
    
    private Map<Integer, String> translate() {
        final HashMap<Integer, String> translation = new HashMap<Integer, String>();
        final String timeString = this.getTimeString((this.plugin.gameType == Arena.GameType.TTT) ? this.plugin.task.gameTime : (300 - this.plugin.task.gameTime));
        for (final Map.Entry<Integer, String> entry : this.translations.entrySet()) {
            translation.put(entry.getKey(), entry.getValue().replace("%time", timeString).replace("%size", "" + this.plugin.players.size()).replace("%3", this.plugin.spectators.size() + ""));
        }
        return translation;
    }
    
    private String getTimeString(final int time) {
        final int hours = time / 3600;
        final int minutes = (time - hours * 3600) / 60;
        final int seconds = time - hours * 3600 - minutes * 60;
        return String.format(TextFormat.WHITE + "%02d" + TextFormat.GRAY + ":" + TextFormat.WHITE + "%02d", minutes, seconds).replace("-", "");
    }
    
    private void sendVotes() {
        final VotingManager vm = this.plugin.votingManager;
        final String[] votes = vm.currentTable;
        String tip = "                                                                                          §8Voting §f| §6/vote <map>";
        for (int i = 0; i < votes.length; ++i) {
            tip = tip + "\n                                                                                      §b[" + (i + 1) + "] §8" + votes[i] + " §c» §a" + vm.stats.get(votes[i]) + " Hlasu";
        }
        this.plugin.messageAllPlayers(tip, null, null, false, (byte)4, new String[0]);
    }
}
