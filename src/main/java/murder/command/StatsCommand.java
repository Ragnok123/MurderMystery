package murder.command;

import murder.*;
import cn.nukkit.command.*;
import cn.nukkit.*;
import murder.stats.*;
import murder.language.*;
import murder.arena.object.*;

public class StatsCommand extends BaseCommand
{
    public StatsCommand(final Murder plugin) {
        super("stats", plugin);
        this.commandParameters.clear();
    }
    
    public boolean execute(final CommandSender commandSender, final String s, final String[] strings) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        final PlayerData data = this.getPlugin().getPlayerData((Player)commandSender);
        final Stats stats = data.stats;
        commandSender.sendMessage(Language.translate("stats", data.getData(), String.valueOf(stats.get(Stats.Stat.WIN)), String.valueOf(stats.get(Stats.Stat.KARMA)), String.valueOf(stats.get(Stats.Stat.INNOCENT_POINTS)), String.valueOf(stats.get(Stats.Stat.DETECTIVE_POINTS)), String.valueOf(stats.get(Stats.Stat.MURDER_POINTS))));
        return true;
    }
}
