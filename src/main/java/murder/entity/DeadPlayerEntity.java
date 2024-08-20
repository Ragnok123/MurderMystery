package murder.entity;

import cn.nukkit.entity.*;
import java.util.*;
import cn.nukkit.level.format.*;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.math.*;
import cn.nukkit.entity.data.*;
import java.nio.charset.*;
import cn.nukkit.utils.*;
import cn.nukkit.event.entity.*;
import cn.nukkit.*;
import cn.nukkit.level.*;
import cn.nukkit.item.*;
import cn.nukkit.network.protocol.*;

public class DeadPlayerEntity extends Entity
{
    public static final int NETWORK_ID = 32;
    protected UUID uuid;
    protected byte[] rawUUID;
    protected Skin skin;
    public long spawnId;
    
    public DeadPlayerEntity(final FullChunk chunk, final CompoundTag nbt) {
        super(chunk, nbt);
        this.spawnId = -1L;
    }
    
    protected void initEntity() {
        super.initEntity();
        this.setDataFlag(27, 1);
        this.setDataProperty((EntityData)new ByteEntityData(27, 2));
        this.setDataProperty((EntityData)new IntPositionEntityData(29, new Vector3((double)this.getFloorX(), (double)this.getFloorY(), (double)this.getFloorZ())));
        this.setDataProperty((EntityData)new FloatEntityData(54, 0.2f));
        this.setDataProperty((EntityData)new FloatEntityData(55, 0.2f));
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.setNameTag("");
        if (this.namedTag.contains("Skin") && this.namedTag.get("Skin") instanceof CompoundTag) {
            if (!this.namedTag.getCompound("Skin").contains("Transparent")) {
                this.namedTag.getCompound("Skin").putBoolean("Transparent", false);
            }
            this.skin = new Skin(this.namedTag.getCompound("Skin").getByteArray("Data"), this.namedTag.getCompound("Skin").getString("ModelId"));
        }
        this.uuid = Utils.dataToUUID(new byte[][] { String.valueOf(this.getId()).getBytes(StandardCharsets.UTF_8), this.skin.getData(), this.getNameTag().getBytes(StandardCharsets.UTF_8) });
    }
    
    public int getNetworkId() {
        return 32;
    }
    
    public boolean attack(final EntityDamageEvent source) {
        return false;
    }
    
    public void spawnTo(final Player player) {
        if (!this.hasSpawned.containsKey(player.getLoaderId()) && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))) {
            this.hasSpawned.put(player.getLoaderId(), player);
            final AddPlayerPacket packet = new AddPlayerPacket();
            packet.username = "" + this.getId();
            packet.uuid = this.uuid;
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
            packet.item = Item.get(0);
            player.dataPacket((DataPacket)packet);
        }
    }
    
    private long getSpawnId() {
        if (this.spawnId == -1L) {
            this.spawnId = this.namedTag.getLong("spawnId");
        }
        return this.spawnId;
    }
}
