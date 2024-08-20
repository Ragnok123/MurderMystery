package murder.entity;

import cn.nukkit.entity.*;
import murder.arena.object.*;
import cn.nukkit.level.format.*;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.event.entity.*;
import cn.nukkit.*;
import cn.nukkit.level.*;
import cn.nukkit.network.protocol.*;

public class DeadEntity extends Entity
{
    public static final int NETWORK_ID = 32;
    private Role role;
    
    public DeadEntity(final FullChunk chunk, final CompoundTag nbt) {
        super(chunk, nbt);
        this.role = null;
    }
    
    protected void initEntity() {
        super.initEntity();
        this.setDataFlag(0, 16);
    }
    
    public int getNetworkId() {
        return 32;
    }
    
    public Role getRole() {
        if (this.role == null) {
            final String role = this.namedTag.getString("Role");
            this.role = Role.valueOf(role);
        }
        return this.role;
    }
    
    public void setRealName() {
        this.setNameTag(this.namedTag.getString("RealName"));
    }
    
    public boolean attack(final EntityDamageEvent source) {
        return false;
    }
    
    public void spawnTo(final Player player) {
        if (!this.hasSpawned.containsKey(player.getLoaderId()) && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))) {
            this.hasSpawned.put(player.getLoaderId(), player);
            final AddEntityPacket packet = new AddEntityPacket();
            packet.type = 32;
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
        }
    }
}
