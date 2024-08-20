package murder.arena;

import murder.language.*;
import GTCore.*;
import murder.*;
import GTCore.Object.*;
import cn.nukkit.level.format.*;
import cn.nukkit.blockentity.*;
import cn.nukkit.nbt.tag.*;
import murder.entity.*;
import cn.nukkit.item.*;
import java.util.*;
import cn.nukkit.entity.data.*;
import cn.nukkit.utils.*;
import GTCore.reflect.*;
import murder.arena.object.*;
import cn.nukkit.math.*;
import cn.nukkit.block.*;
import cn.nukkit.*;
import cn.nukkit.level.*;
import murder.mysql.*;
import cn.nukkit.scheduler.*;
import cn.nukkit.network.protocol.*;
import cn.nukkit.entity.*;

public abstract class ArenaManager
{
    public Arena plugin;
    protected int currentPos;
    protected int titlePos;
    private ArrayList<String> titles;
    
    public ArenaManager() {
        this.currentPos = 0;
        this.titlePos = 0;
        this.titles = new ArrayList<String>() {
            {
                this.add("tut_0");
                this.add("tut_roles");
                this.add("tut_traitors");
                this.add("tut_innocents");
                this.add("tut_tester");
                this.add("tut_rdm");
            }
        };
    }
    
    public boolean inArena(final Player p) {
        return this.inArena(p.getName());
    }
    
    protected boolean inArena(final String p) {
        return this.plugin.players.containsKey(p.toLowerCase());
    }
    
    protected boolean isArenaFull() {
        return this.plugin.players.size() >= this.getMaxPlayers();
    }
    
    public ArenaPlayerData getPlayerData(final Player p) {
        return this.getPlayerData(p.getName());
    }
    
    protected ArenaPlayerData getPlayerData(final String name) {
        return this.plugin.playersData.get(name.toLowerCase());
    }
    
    public int getMaxPlayers() {
        return (this.plugin.gameType == Arena.GameType.TTT) ? 24 : 16;
    }
    
    public int getMinPlayers() {
        return (this.plugin.gameType == Arena.GameType.TTT) ? 10 : 8;
    }
    
    public void messageArenaPlayers(final String message, final boolean translate, final String... parameters) {
        if (!translate) {
            this.plugin.players.values().forEach(p -> p.sendMessage(message));
            return;
        }
        final HashMap<Integer, String> translations = Language.getTranslations(message, parameters);
        for (final ArenaPlayerData data : this.plugin.playersData.values()) {
            data.getPlayerData().getPlayer().sendMessage((String)translations.get(data.getPlayerData().getData().getLanguage()));
        }
    }
    
    public void messageAllPlayers(final String message) {
        this.messageAllPlayers(message, false, new String[0]);
    }
    
    public void messageAllPlayers(final String message, final String... args) {
        this.messageAllPlayers(message, null, null, false, args);
    }
    
    public void messageAllPlayers(final String message, final boolean addPrefix, final String... args) {
        this.messageAllPlayers(message, null, null, addPrefix, args);
    }
    
    public void messageAllPlayers(final String message, final Player player, final ArenaPlayerData data) {
        this.messageAllPlayers(message, player, data, false, new String[0]);
    }
    
    public void messageAllPlayers(final String message, final Player player, final ArenaPlayerData data, final boolean addPrefix, final String... args) {
        this.messageAllPlayers(message, player, data, addPrefix, (byte)1, args);
    }
    
    public void messageAllPlayers(final String message, final Player player, final ArenaPlayerData data, final boolean addPrefix, final byte messageType, final String... args) {
        if (player != null) {
            final String msg = TextFormat.GRAY + player.getDisplayName() + TextFormat.GRAY + " > " + data.getPlayerData().getData().getChatColor() + message;
            final List<Player> datas = new ArrayList<Player>(this.plugin.players.values());
            datas.addAll(this.plugin.spectators.values());
            for (final Player p : datas) {
                p.sendMessage(msg);
            }
            return;
        }
        final TextPacket pk = new TextPacket();
        pk.type = messageType;
        if (args.length > 0) {
            final HashMap<Integer, String> translations = Language.getTranslations(message, args);
            final List<Player> datas2 = new ArrayList<Player>(this.plugin.players.values());
            datas2.addAll(this.plugin.spectators.values());
            for (final Player p2 : datas2) {
                final PlayerData playerData = MTCore.getInstance().getPlayerData(p2);
                if (!p2.isOnline()) {
                    continue;
                }
                pk.message = (addPrefix ? (Murder.getPrefix() + translations.get(playerData.getLanguage())) : ("" + translations.get(playerData.getLanguage())));
                p2.dataPacket((DataPacket)pk);
            }
        }
        else {
            final List<Player> datas = new ArrayList<Player>(this.plugin.players.values());
            datas.addAll(this.plugin.spectators.values());
            for (final Player p : datas) {
                pk.message = (addPrefix ? (Murder.getPrefix() + message) : message);
                p.dataPacket((DataPacket)pk);
            }
        }
    }
    
    public void titleAllPlayers(final String message, final boolean translate, final String... args) {
        this.titleAllPlayers(message, "", translate, args);
    }
    
    public void titleAllPlayers(final String message, final String subTitle, final boolean translate, final String... args) {
        final HashMap<Integer, String> translations = message.isEmpty() ? null : Language.getTranslations(message, args);
        final HashMap<Integer, String> translationsSub = subTitle.isEmpty() ? null : Language.getTranslations(subTitle, args);
        for (final ArenaPlayerData pData : this.plugin.playersData.values()) {
            final Player p = pData.getPlayerData().getData().getPlayer();
            if (!p.isOnline()) {
                continue;
            }
            String title = (translations != null) ? translations.get(pData.getPlayerData().getData().getLanguage()) : "";
            String subTitle2 = (translationsSub != null) ? translationsSub.get(pData.getPlayerData().getData().getLanguage()) : "";
            final String[] split = title.split("_");
            if (split.length == 2) {
                subTitle2 = split[1];
                title = split[0];
            }
            p.sendTitle(title, subTitle2);
        }
    }
    
    private String getDisplayPhase(final Arena.GamePhase phase) {
        switch (phase) {
            case LOBBY: {
                if (this.plugin.starting) {
                    return TextFormat.GOLD + "Starting " + TextFormat.GRAY + "(" + this.plugin.task.startTime + ")";
                }
                return TextFormat.GREEN + "Lobby";
            }
            case GAME: {
                return TextFormat.YELLOW + "Running";
            }
            case ENDING: {
                return TextFormat.BLACK + "Restarting...";
            }
            default: {
                return "";
            }
        }
    }
    
    public void updateSign() {
        final Vector3 sign = this.plugin.data.sign;
        final Level level = Server.getInstance().getDefaultLevel();
        final BlockEntity entity = level.getBlockEntity(sign);
        final int id = level.getBlockIdAt(sign.getFloorX(), sign.getFloorY(), sign.getFloorX());
        if (id != 68) {
            level.setBlock(sign, (Block)new BlockWallSign(5), true, false);
        }
        final String line1 = TextFormat.DARK_RED + "> " + this.plugin.getId() + " <";
        final String line2 = TextFormat.YELLOW + this.plugin.gameType.name();
        final String line3 = "" + TextFormat.GRAY + this.plugin.players.size() + "/" + this.getMaxPlayers();
        String line4 = this.getDisplayPhase(this.plugin.phase);
        if (this.plugin.data.peOnly) {
            line4 = line4 + "\n\n" + TextFormat.DARK_RED + TextFormat.BOLD + "PE ONLY";
        }
        if (!(entity instanceof BlockEntitySign)) {
            final CompoundTag nbt = new CompoundTag().putString("id", "Sign").putString("Text1", line1).putString("Text2", line2).putString("Text3", line3).putString("Text4", line4).putInt("x", sign.getFloorX()).putInt("y", sign.getFloorY()).putInt("z", sign.getFloorZ());
            new BlockEntitySign((FullChunk)level.getChunk((int)sign.x >> 4, (int)sign.z >> 4), nbt).spawnToAll();
        }
        else {
            final BlockEntitySign blockEntitySign = (BlockEntitySign)entity;
            blockEntitySign.setText(new String[] { line1, line2, line3, line4 });
        }
    }
    
    protected boolean checkAlive() {
        if (this.plugin.phase == Arena.GamePhase.GAME && (this.plugin.gameType != Arena.GameType.TTT || this.plugin.task.gameTime > this.plugin.task.getPrepareTime())) {
            if (this.plugin.players.size() <= 1) {
                this.plugin.end(null);
                return false;
            }
            int murderCount = 0;
            int playerCount = 0;
            for (final ArenaPlayerData data : this.plugin.playersData.values()) {
                if (data.getRole() == Role.MURDERER) {
                    ++murderCount;
                }
                else {
                    ++playerCount;
                }
            }
            if (murderCount == 0) {
                this.plugin.end(Role.INNOCENT);
                return false;
            }
            if (playerCount == 0) {
                this.plugin.end(Role.MURDERER);
                return false;
            }
        }
        return true;
    }
    
    protected void createDeadNPC(final Player p, final Role role, final Arena.GameType gameType) {
        if (gameType == Arena.GameType.TTT) {
            final CompoundTag nbt = new CompoundTag().putList(new ListTag("Pos").add((Tag)new DoubleTag("", p.x)).add((Tag)new DoubleTag("", p.y)).add((Tag)new DoubleTag("", p.z))).putList(new ListTag("Motion").add((Tag)new DoubleTag("", 0.0)).add((Tag)new DoubleTag("", 0.0)).add((Tag)new DoubleTag("", 0.0))).putList(new ListTag("Rotation").add((Tag)new FloatTag("", (float)p.yaw)).add((Tag)new FloatTag("", (float)p.pitch))).putString("Role", role.name()).putString("RealName", role.getColor() + p.getName());
            final DeadEntity deadEntity = new DeadEntity(p.chunk, nbt);
            deadEntity.setNameTag("Unknown");
            deadEntity.setNameTagVisible(true);
            deadEntity.spawnToAll();
        }
        else {
            final CompoundTag nbt = new CompoundTag().putList(new ListTag("Pos").add((Tag)new DoubleTag("", p.x)).add((Tag)new DoubleTag("", p.y + 0.3)).add((Tag)new DoubleTag("", p.z))).putList(new ListTag("Motion").add((Tag)new DoubleTag("", 0.0)).add((Tag)new DoubleTag("", 0.0)).add((Tag)new DoubleTag("", 0.0))).putList(new ListTag("Rotation").add((Tag)new FloatTag("", (float)p.yaw)).add((Tag)new FloatTag("", 0.0f))).putCompound("Skin", new CompoundTag().putByteArray("Data", p.getSkin().getData()).putString("ModelId", p.getSkin().getModel())).putLong("spawnId", p.getId());
            final DeadPlayerEntity deadEntity2 = new DeadPlayerEntity(p.chunk, nbt);
            deadEntity2.spawnToAll();
        }
    }
    
    protected void spawnBow(final Player p) {
        final CompoundTag nbt = new CompoundTag().putList(new ListTag("Pos").add((Tag)new DoubleTag("", p.x)).add((Tag)new DoubleTag("", p.y + 1.0)).add((Tag)new DoubleTag("", p.z))).putList(new ListTag("Motion").add((Tag)new DoubleTag("", 0.0)).add((Tag)new DoubleTag("", 0.0)).add((Tag)new DoubleTag("", 0.0))).putList(new ListTag("Rotation").add((Tag)new FloatTag("", (float)p.yaw)).add((Tag)new FloatTag("", (float)p.pitch)));
        final BowEntity bow = new BowEntity(p.chunk, nbt);
        bow.spawnToAll();
    }
    
    protected void throwSword(final Player p) {
        final CompoundTag nbt = new CompoundTag().putList(new ListTag("Pos").add((Tag)new DoubleTag("", p.x)).add((Tag)new DoubleTag("", p.y + p.getEyeHeight())).add((Tag)new DoubleTag("", p.z))).putList(new ListTag("Motion").add((Tag)new DoubleTag("", -Math.sin(p.yaw / 180.0 * 3.141592653589793) * Math.cos(p.pitch / 180.0 * 3.141592653589793))).add((Tag)new DoubleTag("", -Math.sin(p.pitch / 180.0 * 3.141592653589793))).add((Tag)new DoubleTag("", Math.cos(p.yaw / 180.0 * 3.141592653589793) * Math.cos(p.pitch / 180.0 * 3.141592653589793)))).putList(new ListTag("Rotation").add((Tag)new FloatTag("", (float)p.yaw)).add((Tag)new FloatTag("", (float)p.pitch)));
        final ThrowedSwordEntity bow = new ThrowedSwordEntity(p.chunk, nbt);
        bow.shootingEntity = (Entity)p;
        bow.spawnToAll();
    }
    
    public void giveRoles() {
        final int playersCount = this.plugin.players.size();
        int murderCount = (this.plugin.gameType == Arena.GameType.TTT) ? ((playersCount <= 12) ? 2 : ((playersCount <= 16) ? 3 : ((playersCount <= 20) ? 4 : 5))) : 1;
        int detectiveCount = (this.plugin.gameType == Arena.GameType.TTT) ? ((playersCount <= 12) ? 1 : ((playersCount <= 18) ? 2 : 3)) : 1;
        final ArrayList<String> detectives = new ArrayList<String>();
        final HashMap<String, Player> traitors = new HashMap<String, Player>();
        final List<ArenaPlayerData> datas = new ArrayList<ArenaPlayerData>(this.plugin.playersData.values());
        Collections.shuffle(datas);
        for (final ArenaPlayerData data : datas) {
            if (data.getRole() != null) {
                if (data.getRole() == Role.MURDERER) {
                    --murderCount;
                }
                else {
                    if (data.getRole() != Role.DETECTIVE) {
                        continue;
                    }
                    --detectiveCount;
                }
            }
        }
        for (final ArenaPlayerData data : datas) {
            final Player p = data.getPlayer();
            if (data.getRole() != null) {
                if (data.getRole() == Role.MURDERER) {
                    this.plugin.traitors.add(p.getName());
                    traitors.put(p.getName(), p);
                }
                else {
                    if (data.getRole() != Role.DETECTIVE) {
                        continue;
                    }
                    detectives.add(p.getName());
                    p.setNameTag(data.getRole().getColor() + p.getName());
                    if (this.plugin.gameType == Arena.GameType.TTT) {
                        p.getInventory().addItem(new Item[] { new ItemStick() });
                        p.getInventory().sendContents(p);
                    }
                    else {
                        final Item bow = (Item)new ItemBow();
                        bow.setLore(new String[] { "Detective Bow" });
                        p.getInventory().setItem(1, bow);
                        p.getInventory().setItem(16, (Item)new ItemArrow());
                        p.getInventory().sendContents(p);
                    }
                }
            }
            else {
                Role role = Role.INNOCENT;
                if (murderCount > 0) {
                    role = Role.MURDERER;
                    --murderCount;
                    this.plugin.traitors.add(p.getName());
                    traitors.put(p.getName(), p);
                }
                else if (detectiveCount > 0) {
                    role = Role.DETECTIVE;
                    detectives.add(p.getName());
                    --detectiveCount;
                    p.setNameTag(role.getColor() + p.getName());
                    if (this.plugin.gameType == Arena.GameType.TTT) {
                        p.getInventory().addItem(new Item[] { new ItemStick() });
                        p.getInventory().sendContents(p);
                    }
                    else {
                        final Item bow2 = (Item)new ItemBow();
                        bow2.setLore(new String[] { "Detective Bow" });
                        p.getInventory().setItem(1, bow2);
                        p.getInventory().setItem(16, (Item)new ItemArrow());
                        p.getInventory().sendContents(p);
                    }
                }
                data.setRole(role);
                p.sendTitle(Language.translate("role_select", p, role.getColor() + role.getName(this.plugin.gameType)));
                final String t = "role_" + role.getName(this.plugin.gameType).toLowerCase().trim();
                p.sendMessage(Murder.getPrefix() + Language.translate(t, p, new String[0]));
            }
        }
        if (this.plugin.gameType == Arena.GameType.TTT) {
            this.messageAllPlayers("detectives", true, String.join(", ", detectives));
            final String msg = String.join(", ", traitors.keySet());
            for (final Player player : traitors.values()) {
                player.sendMessage(Murder.getPrefix() + Language.translate("murders", player, msg));
            }
        }
        this.plugin.detectives = detectives;
    }
    
    public void setRolesVisibleToTraitors() {
        final ArrayList<SetEntityDataPacket> datas = new ArrayList<SetEntityDataPacket>();
        final ArrayList<Player> traitors = new ArrayList<Player>();
        try {
            for (final ArenaPlayerData data : this.plugin.playersData.values()) {
                final Player p = data.getPlayer();
                if (data.getRole() == Role.MURDERER) {
                    traitors.add(p);
                }
                final EntityMetadata metadata = p.getDataProperties();
                final EntityMetadata newMetadata = new EntityMetadata();
                Reflect.on((Object)newMetadata).set("map", (Object)new HashMap((Map<?, ?>)Reflect.on((Object)metadata).get("map")));
                newMetadata.put((EntityData)new StringEntityData(4, data.getRole().getColor() + p.getName()));
                final SetEntityDataPacket packet = new SetEntityDataPacket();
                packet.eid = p.getId();
                packet.metadata = newMetadata;
                datas.add(packet);
            }
        }
        catch (ReflectException e) {
            MainLogger.getLogger().logException((Exception)e);
            return;
        }
        for (final DataPacket pk : datas) {
            Server.broadcastPacket((Collection)traitors, pk);
        }
    }
    
    public void switchTester() {
        this.plugin.testActivated = !this.plugin.testActivated;
        final MapData data = this.plugin.mapData;
        final AxisAlignedBB bb = data.traitorTesterGate;
        final Vector3 temporalVector = new Vector3();
        if (this.plugin.testActivated) {
            for (int x = (int)bb.minX; x <= bb.maxX; ++x) {
                for (int y = (int)bb.minY; y <= bb.maxY; ++y) {
                    for (int z = (int)bb.minZ; z <= bb.maxZ; ++z) {
                        this.plugin.level.setBlock(temporalVector.setComponents((double)x, (double)y, (double)z), (Block)new BlockGlass(), true, false);
                    }
                }
            }
            this.plugin.task.testerDelay = 8;
        }
        else {
            for (int x = (int)bb.minX; x <= bb.maxX; ++x) {
                for (int y = (int)bb.minY; y <= bb.maxY; ++y) {
                    for (int z = (int)bb.minZ; z <= bb.maxZ; ++z) {
                        this.plugin.level.setBlock(temporalVector.setComponents((double)x, (double)y, (double)z), (Block)new BlockAir(), true, false);
                    }
                }
            }
            for (final Vector3 pos : data.lamps) {
                this.plugin.level.setBlock(pos, (Block)new BlockRedstoneLamp(), true, false);
            }
        }
    }
    
    public void checkTester() {
        if (this.plugin.testedPlayer == null) {
            return;
        }
        final ArenaPlayerData data = this.getPlayerData(this.plugin.testedPlayer);
        if (data == null) {
            return;
        }
        if (data.getRole() == Role.MURDERER) {
            for (final Vector3 pos : this.plugin.mapData.lamps) {
                this.plugin.level.setBlock(pos, (Block)new BlockRedstoneLampLit(), true, false);
            }
        }
    }
    
    protected void checkStarting() {
        if (this.plugin.players.size() >= this.getMinPlayers()) {
            this.plugin.starting = true;
        }
    }
    
    protected void printTraceToNearestPlayer(final ArenaPlayerData data) {
        final Player p = data.getPlayer();
        if (this.plugin.players.size() < 2) {
            return;
        }
        Player target = null;
        double distance = 2.147483647E9;
        for (final ArenaPlayerData data2 : this.plugin.playersData.values()) {
            final double dis = data2.getPlayer().distanceSquared((Vector3)p);
            if (data2.getRole() != Role.MURDERER && dis < distance) {
                target = data2.getPlayer();
                distance = dis;
            }
        }
        if (target == null) {
            return;
        }
        this.pointCompass(p, (Vector3)target);
    }
    
    public void startTutorial() {
        final Location pos = this.plugin.mapData.tutorial[0];
        pos.setLevel(this.plugin.level);
        final MovePlayerPacket pk = new MovePlayerPacket();
        pk.x = (float)pos.x;
        pk.y = (float)pos.y;
        pk.z = (float)pos.z;
        pk.yaw = (float)pos.yaw;
        pk.pitch = (float)pos.pitch;
        pk.mode = 2;
        for (final Player p : this.plugin.players.values()) {
            p.gamemode = 3;
            p.getAdventureSettings().set(AdventureSettings.Type.WORLD_BUILDER, false);
            p.getAdventureSettings().set(AdventureSettings.Type.WORLD_IMMUTABLE, true);
            p.getAdventureSettings().set(AdventureSettings.Type.BUILD_AND_MINE, false);
            p.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, true);
            p.getAdventureSettings().set(AdventureSettings.Type.FLYING, true);
            p.getAdventureSettings().set(AdventureSettings.Type.NO_CLIP, true);
            p.getAdventureSettings().set(AdventureSettings.Type.ATTACK_PLAYERS, false);
            p.getAdventureSettings().update();
            p.despawnFromAll();
            p.getInventory().clearAll();
            p.getInventory().sendContents(p);
            p.teleport(pos);
            pk.eid = p.getId();
            p.dataPacket((DataPacket)pk);
        }
        this.titleAllPlayers(this.titles.get(this.titlePos), true, new String[0]);
        ++this.titlePos;
        ++this.currentPos;
    }
    
    public void nextPos() {
        int index = this.currentPos;
        switch (this.currentPos) {
            case 1: {
                index = 0;
                break;
            }
            case 2:
            case 3: {
                index = 1;
                break;
            }
            case 4: {
                index = 2;
                break;
            }
        }
        ++this.currentPos;
        Location pos;
        if (index < this.plugin.mapData.tutorial.length) {
            pos = this.plugin.mapData.tutorial[index];
        }
        else {
            pos = Location.fromObject(this.plugin.mapData.spawnPositions[0]);
        }
        final MovePlayerPacket pk = new MovePlayerPacket();
        pk.x = (float)pos.x;
        pk.y = (float)pos.y;
        pk.z = (float)pos.z;
        pk.yaw = (float)pos.yaw;
        pk.pitch = (float)pos.pitch;
        pk.mode = 2;
        for (final Player p : this.plugin.players.values()) {
            p.teleport(pos);
            pk.eid = p.getId();
            p.dataPacket((DataPacket)pk);
        }
        if (this.titlePos < this.titles.size()) {
            this.titleAllPlayers(this.titles.get(this.titlePos), true, new String[0]);
            ++this.titlePos;
        }
    }
    
    public void ban(final Player p, final String reason, final int count) {
        long doba = 4L;
        switch (count) {
            case 0: {
                break;
            }
            case 1: {
                doba = 24L;
                break;
            }
            case 2: {
                doba = 72L;
                break;
            }
            case 3: {
                doba = 240L;
                break;
            }
            default: {
                doba = 720L;
                break;
            }
        }
        doba *= 216000000L;
        this.ban(p, reason, doba);
    }
    
    public void ban(final Player p, final String reason, final long duration) {
        Server.getInstance().getScheduler().scheduleAsyncTask((AsyncTask)new BanTask(p, reason, duration));
    }
    
    public boolean inArenaFast(final Player p) {
        for (final Player pl : this.plugin.players.values()) {
            if (pl.getId() == p.getId()) {
                return true;
            }
        }
        return false;
    }
    
    public void pointCompass(final Player p, final Vector3 pos) {
        final SetSpawnPositionPacket pk = new SetSpawnPositionPacket();
        pk.spawnType = 1;
        pk.x = pos.getFloorX();
        pk.y = pos.getFloorY();
        pk.z = pos.getFloorZ();
        if (pos instanceof Player) {
            p.sendActionBar(TextFormat.GREEN + "Ponting to " + TextFormat.YELLOW + ((Player)pos).getName(), 5, 40, 5);
        }
    }
}
