package murder.arena.task;

import cn.nukkit.scheduler.*;
import murder.arena.*;
import murder.arena.manager.*;
import cn.nukkit.*;

public class WorldPrepareTask extends AsyncTask
{
    private Arena arena;
    private String map;
    private String id;
    private boolean force;
    
    public WorldPrepareTask(final Arena arena, final String map, final String id, final boolean force) {
        this.map = map;
        this.arena = arena;
        this.id = id;
        this.force = force;
    }
    
    public void onRun() {
        WorldManager.resetWorld(this.map, this.id);
    }
    
    public void onCompletion(final Server server) {
        if (server.loadLevel(this.map + "_" + this.id)) {
            this.arena.isLevelLoaded = true;
            this.arena.level = server.getLevelByName(this.map + "_" + this.id);
        }
        if (this.force) {
            this.arena.start(this.force);
        }
    }
}
