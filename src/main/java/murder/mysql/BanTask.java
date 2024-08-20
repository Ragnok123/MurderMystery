package murder.mysql;

import GTCore.Mysql.*;
import cn.nukkit.*;
import java.sql.*;

public class BanTask extends AsyncQuery
{
    private String reason;
    private long duration;
    
    public BanTask(final Player p, final String reason, final long duration) {
        this.player = p.getName();
        this.reason = reason;
        this.duration = duration;
        this.table = null;
    }
    
    public void onRun() {
        try {
            final PreparedStatement statement = this.getMysqli().prepareStatement("UPDATE murder SET ban = '" + this.duration + "', reason = '" + this.reason + "', count = count + 1, karma = 100 WHERE name = '" + this.player.toLowerCase() + "'");
            statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
