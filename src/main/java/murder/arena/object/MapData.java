package murder.arena.object;

import cn.nukkit.math.*;
import cn.nukkit.level.*;

public class MapData
{
    public String name;
    public AxisAlignedBB area;
    public Vector3[] spawnPositions;
    public AxisAlignedBB traitorTester;
    public AxisAlignedBB traitorTesterGate;
    public Vector3 testerButton;
    public Vector3[] lamps;
    public Vector3[] goldSpawn;
    public Location[] tutorial;
    
    public MapData() {
        this.traitorTester = null;
        this.traitorTesterGate = null;
        this.testerButton = null;
        this.lamps = null;
        this.goldSpawn = null;
        this.tutorial = null;
    }
    
    public String getName() {
        return this.name;
    }
}
