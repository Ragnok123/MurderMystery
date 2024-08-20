package murder.entity;

import cn.nukkit.entity.*;
import cn.nukkit.level.format.*;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.*;
import murder.event.*;
import cn.nukkit.event.*;
import cn.nukkit.math.*;
import cn.nukkit.event.entity.*;
import cn.nukkit.level.*;
import cn.nukkit.network.protocol.*;
import cn.nukkit.item.*;

public class BowEntity extends Entity
{
    public static final int NETWORK_ID = 36;
    
    public BowEntity(final FullChunk chunk, final CompoundTag nbt) {
        super(chunk, nbt);
    }
    
    protected void initEntity() {
        super.initEntity();
        this.setDataFlag(0, 16);
        this.setDataFlag(0, 5);
        this.setDataFlag(0, 17);
        this.setScale(0.0f);
    }
    
    public int getNetworkId() {
        return 36;
    }
    
    public float getWidth() {
        return 1.0f;
    }
    
    public float getLength() {
        return 1.0f;
    }
    
    public float getHeight() {
        return 3.0f;
    }
    
    public boolean onUpdate(final int currentTick) {
        final AxisAlignedBB bb = this.boundingBox.grow(0.3, 0.3, 0.3);
        for (final Entity entity : this.level.getEntities()) {
            if (entity != this) {
                if (entity.getBoundingBox().intersectsWith(bb)) {
                    if (entity instanceof Player) {
                        final Player p = (Player)entity;
                        final PlayerPickupBowEvent event = new PlayerPickupBowEvent(p);
                        this.getServer().getPluginManager().callEvent((Event)event);
                        if (!event.isCancelled()) {
                            this.close();
                            return false;
                        }
                    }
                }
            }
        }
        this.yaw = (this.yaw + 9.0) % 360.0;
        this.addMovement(this.x, this.y, this.z, this.yaw, this.pitch, this.yaw);
        return true;
    }
    
    public boolean attack(final EntityDamageEvent source) {
        return false;
    }
    
    public void spawnTo(final Player player) {
        if (!this.hasSpawned.containsKey(player.getLoaderId()) && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))) {
            this.hasSpawned.put(player.getLoaderId(), player);
            final AddEntityPacket packet = new AddEntityPacket();
            packet.type = 36;
            packet.entityUniqueId = this.getId();
            packet.entityRuntimeId = this.getId();
            packet.x = (float)this.x;
            packet.y = (float)this.y;
            packet.z = (float)this.z;
            packet.speedX = (float)this.motionX;
            packet.speedY = (float)this.motionY;
            packet.speedZ = (float)this.motionZ;
            packet.yaw = (float)this.yaw;
            packet.pitch = (float)this.pitch;
            packet.metadata = this.dataProperties;
            player.dataPacket((DataPacket)packet);
            final MobEquipmentPacket pk = new MobEquipmentPacket();
            pk.eid = this.getId();
            pk.hotbarSlot = 0;
            pk.inventorySlot = 0;
            pk.item = (Item)new ItemBow();
            player.dataPacket((DataPacket)pk);
        }
    }
}
