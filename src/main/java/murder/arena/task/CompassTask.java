package murder.arena.task;

import cn.nukkit.scheduler.*;
import cn.nukkit.math.*;
import cn.nukkit.*;
import cn.nukkit.level.*;
import cn.nukkit.level.particle.*;
import cn.nukkit.network.protocol.*;

public class CompassTask extends Task
{
    private double current;
    private Vector3 vector;
    private Vector3 from;
    private Level level;
    private Player player;
    
    public CompassTask(final Player p, final Vector3 to) {
        this.current = 1.0;
        this.level = p.getLevel();
        this.player = p;
        this.from = (Vector3)p.add(0.0, (double)p.getEyeHeight(), 0.0);
        final double x = to.x - this.from.x;
        final double y = to.y + p.getEyeHeight() - this.from.y;
        final double z = to.z - this.from.z;
        final double diff = Math.abs(x) + Math.abs(z);
        final double yaw = Math.toDegrees(-Math.atan2(x / diff, z / diff));
        final double pitch = (y == 0.0) ? 0.0 : Math.toDegrees(-Math.atan2(y, Math.sqrt(x * x + z * z)));
        this.vector = new Location(this.from.x, this.from.y, this.from.z, yaw, pitch).getDirectionVector();
    }
    
    public void onRun(final int i) {
        if (this.level.getProvider() == null) {
            this.cancel();
            return;
        }
        final Vector3 pos = this.from.add(this.vector.multiply(this.current));
        this.current += 0.5;
        this.level.addParticle((Particle)new DustParticle(pos, 255, 0, 0), this.player);
        if (this.current > 10.0) {
            this.cancel();
        }
    }
    
    private class BoneMealParticle extends Particle
    {
        public BoneMealParticle(final Vector3 pos) {
            super(pos.x, pos.y, pos.z);
        }
        
        public DataPacket[] encode() {
            final LevelEventPacket pk = new LevelEventPacket();
            pk.evid = 2005;
            pk.x = (float)this.x;
            pk.y = (float)this.y;
            pk.z = (float)this.z;
            pk.data = 0;
            return new DataPacket[] { pk };
        }
    }
}
