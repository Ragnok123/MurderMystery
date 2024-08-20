package murder.event;

import cn.nukkit.event.player.*;
import cn.nukkit.event.*;
import cn.nukkit.*;

public class PlayerPickupBowEvent extends PlayerEvent implements Cancellable
{
    private static final HandlerList handlers;
    
    public static HandlerList getHandlers() {
        return PlayerPickupBowEvent.handlers;
    }
    
    public PlayerPickupBowEvent(final Player player) {
        this.player = player;
    }
    
    static {
        handlers = new HandlerList();
    }
}
