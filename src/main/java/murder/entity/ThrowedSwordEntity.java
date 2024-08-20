package murder.entity;

import cn.nukkit.entity.projectile.*;
import cn.nukkit.level.format.*;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.entity.data.*;
import cn.nukkit.*;
import cn.nukkit.entity.*;
import cn.nukkit.event.entity.*;
import cn.nukkit.math.*;
import java.util.*;
import cn.nukkit.level.*;
import cn.nukkit.network.protocol.*;
import cn.nukkit.item.*;

public class ThrowedSwordEntity extends EntityProjectile
{
    public static final int NETWORK_ID = -1;
    
    public ThrowedSwordEntity(final FullChunk chunk, final CompoundTag nbt) {
        super(chunk, nbt);
    }
    
    protected void initEntity() {
        super.initEntity();
        this.setDataProperty((EntityData)new IntEntityData(2, 267));
        this.setDataFlag(0, 16, true);
    }
    
    public int getNetworkId() {
        return -1;
    }
    
    public float getWidth() {
        return 0.5f;
    }
    
    public float getLength() {
        return 0.5f;
    }
    
    public float getHeight() {
        return 0.5f;
    }
    
    public boolean onUpdate(final int currentTick) {
        if (this.closed) {
            return false;
        }
        final int tickDiff = currentTick - this.lastUpdate;
        if (tickDiff <= 0 && !this.justCreated) {
            return true;
        }
        this.lastUpdate = currentTick;
        this.timing.startTiming();
        boolean hasUpdate = this.entityBaseTick(tickDiff);
        if (this.isAlive()) {
            if (!this.onGround) {
                final AxisAlignedBB bb = this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(0.3, 0.3, 0.3);
                for (final Player entity : this.level.getPlayers().values()) {
                    if (entity.getBoundingBox().intersectsWith(bb)) {
                        if (entity == this.shootingEntity) {
                            continue;
                        }
                        if (entity.isSpectator()) {
                            continue;
                        }
                        if (entity.isCreative()) {
                            continue;
                        }
                        if (this.shootingEntity != null) {
                            entity.attack((EntityDamageEvent)new EntityDamageByChildEntityEvent(this.shootingEntity, (Entity)this, (Entity)entity, EntityDamageEvent.DamageCause.PROJECTILE, (float)this.getDamage()));
                        }
                        else {
                            entity.attack((EntityDamageEvent)new EntityDamageByEntityEvent((Entity)this, (Entity)entity, EntityDamageEvent.DamageCause.PROJECTILE, (float)this.getDamage()));
                        }
                        this.isCollided = true;
                        this.close();
                        return false;
                    }
                }
            }
            this.move(this.motionX, this.motionY, this.motionZ);
            if (this.isCollided) {
                this.motionX = 0.0;
                this.motionY = 0.0;
                this.motionZ = 0.0;
                this.onGround = true;
                this.close();
                return false;
            }
            if (!this.onGround || Math.abs(this.motionX) > 1.0E-5 || Math.abs(this.motionY) > 1.0E-5 || Math.abs(this.motionZ) > 1.0E-5) {
                hasUpdate = true;
            }
            this.updateMovement();
        }
        if (this.age > 60) {
            this.kill();
            hasUpdate = true;
        }
        this.timing.stopTiming();
        return hasUpdate;
    }
    
    public void spawnTo(final Player player) {
        if (!this.hasSpawned.containsKey(player.getLoaderId()) && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))) {
            this.hasSpawned.put(player.getLoaderId(), player);
            final AddItemEntityPacket packet = new AddItemEntityPacket();
            packet.item = (Item)new ItemSwordIron();
            packet.entityUniqueId = this.getId();
            packet.entityRuntimeId = this.getId();
            packet.x = (float)this.x;
            packet.y = (float)this.y;
            packet.z = (float)this.z;
            packet.speedX = (float)this.motionX;
            packet.speedY = (float)this.motionY;
            packet.speedZ = (float)this.motionZ;
            player.dataPacket((DataPacket)packet);
        }
    }
}
