package murder.arena.task;

import murder.arena.*;
import cn.nukkit.*;
import murder.arena.object.*;
import cn.nukkit.utils.*;
import murder.*;
import murder.language.*;
import cn.nukkit.network.protocol.*;
import cn.nukkit.nbt.tag.*;
import murder.entity.*;
import cn.nukkit.level.format.*;
import cn.nukkit.level.sound.*;
import java.util.*;
import cn.nukkit.math.*;
import cn.nukkit.item.*;

public class ArenaSchedule implements Runnable
{
    private Arena plugin;
    public int gameTime;
    public int testerDelay;
    public int tutorialTime;
    public int startTime;
    private int endTime;
    
    public ArenaSchedule(final Arena plugin) {
        this.gameTime = 0;
        this.testerDelay = 8;
        this.tutorialTime = 23;
        this.startTime = 50;
        this.endTime = 15;
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        if (this.plugin.phase == Arena.GamePhase.LOBBY) {
            if (this.plugin.starting) {
                this.starting();
            }
        }
        else if (this.plugin.phase == Arena.GamePhase.GAME) {
            this.running();
        }
        else if (this.plugin.phase == Arena.GamePhase.ENDING) {
            this.ending();
        }
    }
    
    private void starting() {
        --this.startTime;
        this.plugin.updateSign();
        if (this.startTime >= 0) {
            for (final Player p : this.plugin.players.values()) {
                p.setExperience(0, this.startTime);
            }
        }
        if (this.startTime == 5) {
            this.plugin.selectMap();
        }
        if (this.startTime <= 0 && this.plugin.isLevelLoaded) {
            this.plugin.start();
        }
    }
    
    public int getPrepareTime() {
        return (this.plugin.gameType == Arena.GameType.TTT) ? 30 : 10;
    }
    
    private void running() {
        if (this.plugin.gameType == Arena.GameType.MURDER && this.gameTime % 30 == 0) {
            this.spawnGold();
        }
        if (this.plugin.gameType == Arena.GameType.TTT) {
            if (this.tutorialTime >= 0) {
                --this.tutorialTime;
                if (this.tutorialTime % 4 == 0) {
                    this.plugin.nextPos();
                }
                if (this.tutorialTime < 0) {
                    this.plugin.postStart();
                }
                return;
            }
            ++this.gameTime;
            if (this.gameTime == 30) {
                this.plugin.giveRoles();
                this.plugin.setRolesVisibleToTraitors();
            }
            if (this.plugin.testActivated) {
                if (this.testerDelay <= 0) {
                    this.testerDelay = 8;
                    this.plugin.switchTester();
                    return;
                }
                if (this.testerDelay == 5) {
                    this.plugin.checkTester();
                }
                --this.testerDelay;
            }
        }
        else {
            ++this.gameTime;
            if (this.gameTime == 10) {
                this.plugin.titleAllPlayers("murder_sword", true, new String[0]);
                for (final ArenaPlayerData data : this.plugin.playersData.values()) {
                    if (data.getRole() == Role.MURDERER) {
                        final Player p = data.getPlayer();
                        p.getInventory().setItem(1, (Item)new ItemSwordIron());
                        p.getInventory().setHotbarSlotIndex(1, 1);
                        p.getInventory().setItem(2, (Item)new ItemRedstone());
                        p.getInventory().setHotbarSlotIndex(2, 2);
                        p.getInventory().sendContents(p);
                    }
                }
            }
            if (this.gameTime >= 300) {
                this.plugin.end(Role.INNOCENT);
                return;
            }
            if (300 - this.gameTime == 60) {
                for (final ArenaPlayerData data : this.plugin.playersData.values()) {
                    if (data.getRole() == Role.MURDERER) {
                        final Player p = data.getPlayer();
                        p.getInventory().addItem(new Item[] { new ItemCompass().setCustomName(TextFormat.GOLD + "Players finder") });
                        p.getInventory().sendContents(p);
                        p.sendMessage(Murder.getPrefix() + Language.translate("compass_murder", p, new String[0]));
                    }
                }
                this.plugin.titleAllPlayers("", "minute_left", true, new String[0]);
            }
            if (new Random().nextInt(50) == 0) {
                final LevelSoundEventPacket pk = new LevelSoundEventPacket();
                pk.sound = 13;
                pk.pitch = 68404;
                pk.isGlobal = true;
                for (final Player p : this.plugin.players.values()) {
                    pk.x = (float)p.x;
                    pk.y = (float)p.y + p.getEyeHeight();
                    pk.z = (float)p.z;
                    p.dataPacket((DataPacket)pk);
                }
            }
        }
        if (this.gameTime <= this.getPrepareTime()) {
            final ExperienceOrbSound sound = new ExperienceOrbSound(new Vector3());
            for (final Player p : this.plugin.players.values()) {
                p.setExperience(0, this.getPrepareTime() - this.gameTime);
                sound.setComponents(p.x, p.y + p.getEyeHeight(), p.z);
                p.getLevel().addSound((Sound)sound, p);
            }
            if (this.gameTime == this.getPrepareTime()) {
                final LevelSoundEventPacket pk2 = new LevelSoundEventPacket();
                pk2.sound = 55;
                pk2.extraData = Integer.MIN_VALUE;
                pk2.pitch = 1;
                pk2.isGlobal = true;
                for (final Player p2 : this.plugin.players.values()) {
                    pk2.x = (float)p2.x;
                    pk2.y = (float)p2.y + p2.getEyeHeight();
                    pk2.z = (float)p2.z;
                    p2.dataPacket((DataPacket)pk2);
                }
            }
        }
    }
    
    private void ending() {
        --this.endTime;
        if (this.endTime <= 0) {
            this.plugin.stop();
        }
    }
    
    public void reset() {
        this.endTime = 15;
        this.startTime = 50;
        this.gameTime = 0;
        this.testerDelay = 8;
        this.tutorialTime = 23;
    }
    
    private void spawnParticle() {
        final Vector3 center = null;
        for (final Vector3 v : this.getRandomPosition(center, 7)) {
            final CompoundTag nbt = new CompoundTag().putList(new ListTag("Pos").add((Tag)new DoubleTag("", v.x)).add((Tag)new DoubleTag("", v.y)).add((Tag)new DoubleTag("", v.z))).putList(new ListTag("Motion").add((Tag)new DoubleTag("", 0.0)).add((Tag)new DoubleTag("", 0.0)).add((Tag)new DoubleTag("", 0.0))).putList(new ListTag("Rotation").add((Tag)new FloatTag("", 0.0f)).add((Tag)new FloatTag("", 0.0f)));
            new WinEntity((FullChunk)this.plugin.level.getChunk((int)v.x >> 4, (int)v.z >> 4), nbt);
            this.plugin.level.addSound((Sound)new FizzSound(v));
        }
    }
    
    private Vector3[] getRandomPosition(final Vector3 center, final int count) {
        final ArrayList<Vector3> list = new ArrayList<Vector3>();
        final NukkitRandom random = new NukkitRandom();
        for (int i = 0; i < count; ++i) {
            list.add(new Vector3(center.x + random.nextRange(-12, 12), center.y, center.z + random.nextRange(-12, 12)));
        }
        return list.stream().toArray(Vector3[]::new);
    }
    
    private void spawnGold() {
        for (final int i : this.plugin.goldsToSpawn) {
            this.plugin.level.dropItem(this.plugin.mapData.goldSpawn[i], (Item)new ItemIngotGold(), new Vector3(0.0, 0.2, 0.0));
        }
        this.plugin.goldsToSpawn.clear();
    }
}
