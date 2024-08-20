package murder.arena.object;

import cn.nukkit.math.*;
import murder.arena.*;

public class ArenaData
{
    public Vector3 sign;
    public Vector3 lobby;
    public Arena.GameType gameType;
    public boolean peOnly;
    
    protected ArenaData() {
        this.sign = null;
        this.lobby = null;
        this.peOnly = false;
    }
    
    public ArenaData(final Vector3 sign, final Vector3 lobby) {
        this.sign = null;
        this.lobby = null;
        this.peOnly = false;
        this.sign = sign;
        this.lobby = lobby;
    }
}
