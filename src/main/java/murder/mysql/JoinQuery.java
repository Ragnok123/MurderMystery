package murder.mysql;

import GTCore.Mysql.*;
import cn.nukkit.*;
import cn.nukkit.scheduler.*;
import java.util.concurrent.*;
import murder.*;
import murder.language.*;
import GTCore.*;
import java.util.*;
import cn.nukkit.utils.*;
import murder.stats.*;
import murder.arena.object.*;
import java.sql.*;

public class JoinQuery extends AsyncQuery
{
    private HashMap<String, Object> data;
    public String banReason;
    long days;
    long hours;
    long minutes;
    long seconds;
    
    public JoinQuery(final Player p) {
        this.data = new HashMap<String, Object>();
        this.banReason = null;
        this.player = p.getName();
        this.table = "murder";
        Server.getInstance().getScheduler().scheduleAsyncTask((AsyncTask)this);
    }
    
    public void onQuery(final HashMap<String, Object> data) {
        if (data == null || data.isEmpty()) {
            this.data = this.registerPlayer(this.player);
        }
        else {
            this.data = data;
        }
        if (this.data.containsKey("ban")) {
            final long time = this.data.get("ban");
            final String reason = this.data.get("reason");
            final long current = System.currentTimeMillis();
            final long diff = time - current;
            if (diff > 0L) {
                this.days = TimeUnit.MILLISECONDS.toDays(diff);
                this.hours = TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(this.days);
                this.minutes = TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.DAYS.toMinutes(this.days);
                this.seconds = TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.DAYS.toSeconds(this.days);
                this.banReason = reason;
            }
        }
    }
    
    public void onCompletion(final Server server) {
        final Player p = server.getPlayerExact(this.player);
        if (p == null || !p.isOnline()) {
            return;
        }
        final Murder plugin = Murder.getInstance();
        if (this.banReason != null) {
            p.sendMessage(Language.translate("ban", p, "" + this.banReason, "" + this.days, "" + this.hours, "" + this.minutes, "" + this.seconds));
            MTCore.getInstance().transferToLobby(p);
            return;
        }
        final HashMap<String, Object> taskData = this.data;
        final PlayerData data = plugin.getPlayerData(p);
        data.stats.init(taskData);
        data.initialized = true;
        p.setDisplayName(TextFormat.GRAY + "[" + TextFormat.YELLOW + data.stats.get(Stats.Stat.KARMA) + TextFormat.GRAY + "] " + p.getDisplayName());
        p.setGamemode(0);
    }
    
    private HashMap<String, Object> registerPlayer(final String player) {
        final String name = player.toLowerCase().trim();
        final HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("name", name);
        data.put("karma", 100);
        data.put("wins", 0);
        data.put("detective", 0);
        data.put("murder", 0);
        data.put("innocent", 0);
        try {
            final PreparedStatement e = this.getMysqli().prepareStatement("INSERT INTO murder (name) VALUES ('" + name + "')");
            e.executeUpdate();
        }
        catch (SQLException e2) {
            e2.printStackTrace();
        }
        return data;
    }
}
