package murder.arena.object;

import murder.arena.*;
import cn.nukkit.*;
import java.util.*;
import murder.stats.*;

public class PlayerData
{
    public Arena arena;
    private Player player;
    private GTCore.Object.PlayerData data;
    public HashSet<String> kits;
    public Stats stats;
    public boolean initialized;
    public long lastNPCClick;
    public long lastNPCId;
    public int banCount;
    
    public PlayerData(final Player p, final GTCore.Object.PlayerData data) {
        this.arena = null;
        this.data = null;
        this.kits = new HashSet<String>();
        this.stats = new Stats();
        this.initialized = false;
        this.lastNPCClick = 0L;
        this.lastNPCId = -1L;
        this.banCount = 0;
        this.player = p;
        this.data = data;
    }
    
    public Arena getArena() {
        return this.arena;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public GTCore.Object.PlayerData getData() {
        return this.data;
    }
    
    public boolean isInitialized() {
        return this.initialized;
    }
}
