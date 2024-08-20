package murder.command;

import murder.*;
import cn.nukkit.command.*;
import cn.nukkit.*;
import murder.arena.*;

public class StartCommand extends BaseCommand
{
    public StartCommand(final Murder plugin) {
        super("start", plugin);
        this.commandParameters.clear();
    }
    
    public boolean execute(final CommandSender sender, final String s, final String[] args) {
        if (!sender.isOp() || !(sender instanceof Player)) {
            return true;
        }
        final Arena arena = this.plugin.getPlayerArena((Player)sender);
        if (arena == null) {
            return true;
        }
        arena.selectMap(true);
        return true;
    }
}
