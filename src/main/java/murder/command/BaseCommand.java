package murder.command;

import cn.nukkit.command.*;
import murder.*;
import cn.nukkit.*;

public abstract class BaseCommand extends Command
{
    protected Murder plugin;
    protected Server server;
    
    public BaseCommand(final String name, final Murder plugin) {
        super(name);
        this.description = "GT command";
        this.usageMessage = "";
        this.plugin = plugin;
    }
    
    protected Murder getPlugin() {
        return this.plugin;
    }
}
