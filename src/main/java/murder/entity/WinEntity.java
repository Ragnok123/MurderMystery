package murder.entity;

import cn.nukkit.entity.*;
import cn.nukkit.math.*;
import cn.nukkit.level.format.*;
import cn.nukkit.nbt.tag.*;
import java.util.*;
import cn.nukkit.level.particle.*;
import cn.nukkit.level.sound.*;
import cn.nukkit.*;

public class WinEntity extends Entity
{
    private Vector3 startPos;
    private double speed;
    
    public WinEntity(final FullChunk chunk, final CompoundTag nbt) {
        super(chunk, nbt);
        this.speed = 0.3;
    }
    
    public int getNetworkId() {
        return -10;
    }
    
    protected void initEntity() {
        super.initEntity();
        (this.startPos = new Vector3()).setComponents(this.x, this.y, this.z);
        this.speed += (new Random().nextInt(3) + 1) / 10;
    }
    
    public boolean onUpdate(final int diff) {
        final int tick = this.getServer().getTick();
        final Random rnd = new Random();
        this.level.addParticle((Particle)new DustParticle((Vector3)this, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
        if (this.y > this.startPos.y + 13.0) {
            this.level.addParticle((Particle)new InstantSpellParticle((Vector3)this, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
            this.level.addParticle((Particle)new InstantSpellParticle((Vector3)this, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
            this.level.addSound((Sound)new ExplodeSound((Vector3)this));
            this.close();
            return false;
        }
        this.y += this.speed;
        this.lastUpdate = tick;
        return true;
    }
    
    public void spawnTo(final Player player) {
    }
    
    public void despawnFrom(final Player player) {
    }
}
