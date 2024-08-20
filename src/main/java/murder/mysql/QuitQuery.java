package murder.mysql;

import GTCore.Mysql.*;
import murder.arena.object.*;
import GTCore.*;
import cn.nukkit.*;
import cn.nukkit.scheduler.*;
import murder.stats.*;
import java.sql.*;

public class QuitQuery extends AsyncQuery
{
    private final PlayerData[] datas;
    
    public QuitQuery(final PlayerData... datas) {
        this.datas = datas;
        if (!MTCore.isShuttingDown) {
            Server.getInstance().getScheduler().scheduleAsyncTask((AsyncTask)this);
        }
        else {
            this.onRun();
        }
    }
    
    public void onRun() {
        for (final PlayerData data : this.datas) {
            this.player = data.getPlayer().getName();
            final Stats stats = data.stats;
            try {
                final PreparedStatement statement = this.getMysqli().prepareStatement("UPDATE murder SET wins = wins + '" + stats.getDelta(Stats.Stat.WIN) + "', karma = karma + '" + stats.getDelta(Stats.Stat.KARMA) + "', innocent = innocent + '" + stats.getDelta(Stats.Stat.INNOCENT_POINTS) + "', detective = detective + '" + stats.getDelta(Stats.Stat.DETECTIVE_POINTS) + "', murder = murder + '" + stats.getDelta(Stats.Stat.MURDER_POINTS) + "' WHERE name = '" + this.player.toLowerCase() + "'");
                statement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
