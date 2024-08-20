package murder.command;

import murder.*;
import cn.nukkit.command.data.*;
import cn.nukkit.command.*;
import cn.nukkit.*;
import cn.nukkit.utils.*;
import murder.arena.*;

public class VoteCommand extends BaseCommand
{
    public VoteCommand(final Murder plugin) {
        super("vote", plugin);
        this.commandParameters.clear();
        this.commandParameters.put("byName", new CommandParameter[] { new CommandParameter("map", "string", false) });
        this.commandParameters.put("byNumber", new CommandParameter[] { new CommandParameter("map", "int", false) });
    }
    
    public boolean execute(final CommandSender sender, final String s, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player p = (Player)sender;
        final Arena arena = this.getPlugin().getPlayerArena(p);
        if (arena == null) {
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Murder.getPrefix() + TextFormat.GRAY + "use /vote [map]");
            return true;
        }
        arena.votingManager.onVote((Player)sender, args[0]);
        return true;
    }
}
