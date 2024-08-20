package murder.arena;

import murder.*;
import cn.nukkit.level.*;
import cn.nukkit.math.*;
import murder.stats.*;
import murder.arena.manager.*;
import murder.arena.object.*;
import murder.language.*;
import GTCore.*;
import cn.nukkit.*;
import cn.nukkit.entity.projectile.*;
import cn.nukkit.potion.*;
import cn.nukkit.utils.*;
import cn.nukkit.entity.*;
import cn.nukkit.event.*;
import cn.nukkit.inventory.*;
import cn.nukkit.event.inventory.*;
import murder.entity.*;
import java.util.*;
import cn.nukkit.level.sound.*;
import cn.nukkit.item.*;
import cn.nukkit.block.*;
import GTCore.Event.*;
import GTCore.reflect.*;
import cn.nukkit.entity.data.*;
import cn.nukkit.network.protocol.*;
import murder.event.*;
import cn.nukkit.event.player.*;
import cn.nukkit.event.entity.*;
import murder.arena.task.*;
import cn.nukkit.scheduler.*;

public class Arena extends ArenaManager implements Listener
{
    private String id;
    private String safeId;
    private Murder plugin;
    public Level level;
    public GamePhase phase;
    public boolean starting;
    public final Map<String, Player> players;
    public final Map<String, ArenaPlayerData> playersData;
    public final Map<String, Player> spectators;
    public final ArenaData data;
    public MapData mapData;
    public Player winner;
    public PopupTask popupTask;
    public ArenaSchedule task;
    public GameType gameType;
    public boolean testActivated;
    public Player testedPlayer;
    public List<String> traitors;
    public List<String> detectives;
    public String hero;
    public boolean isLevelLoaded;
    public VotingManager votingManager;
    public String map;
    public Set<Integer> goldsToSpawn;
    
    public Arena(final Murder plugin, final String id, final ArenaData data) {
        this.level = null;
        this.phase = GamePhase.LOBBY;
        this.starting = false;
        this.players = new HashMap<String, Player>();
        this.playersData = new HashMap<String, ArenaPlayerData>();
        this.spectators = new HashMap<String, Player>();
        this.mapData = null;
        this.winner = null;
        this.testActivated = false;
        this.testedPlayer = null;
        this.traitors = new ArrayList<String>();
        this.detectives = new ArrayList<String>();
        this.hero = null;
        this.isLevelLoaded = false;
        this.map = null;
        this.goldsToSpawn = new HashSet<Integer>();
        super.plugin = this;
        this.id = id;
        this.safeId = id.toLowerCase().replaceAll(" ", "");
        this.plugin = plugin;
        this.data = data;
        this.task = new ArenaSchedule(this);
        this.popupTask = new PopupTask(this);
        this.gameType = data.gameType;
        (this.votingManager = new VotingManager(this)).createVoteTable();
        plugin.getServer().getScheduler().scheduleDelayedRepeatingTask((Runnable)this.task, 20, 20);
        plugin.getServer().getScheduler().scheduleDelayedRepeatingTask((Runnable)this.popupTask, 20, 10);
        this.updateSign();
    }
    
    public boolean start() {
        return this.start(false);
    }
    
    public boolean start(final boolean force) {
        if (this.phase != GamePhase.LOBBY || !this.isLevelLoaded) {
            return false;
        }
        if (this.players.size() < this.getMinPlayers() && !force) {
            this.starting = false;
            this.task.startTime = 50;
            return false;
        }
        this.traitors.clear();
        this.level.setTime(5000);
        this.level.setRaining(false);
        this.level.setThundering(false);
        int spawnIndex = 0;
        if (this.gameType == GameType.MURDER) {
            for (final ArenaPlayerData data : new ArrayList<ArenaPlayerData>(this.playersData.values())) {
                final Player p = data.getPlayerData().getPlayer();
                p.removeAllEffects();
                p.getFoodData().setLevel(20);
                p.getInventory().clearAll();
                if (spawnIndex >= this.mapData.spawnPositions.length) {
                    spawnIndex = 0;
                }
                final Vector3 pos = this.mapData.spawnPositions[spawnIndex];
                ++spawnIndex;
                p.teleport(new Position(pos.x, pos.y, pos.z, this.level));
                p.setNameTagVisible(false);
                p.setNameTagAlwaysVisible(false);
                p.setNameTag("");
            }
            this.giveRoles();
            for (final Vector3 v : this.mapData.goldSpawn) {
                this.level.dropItem(v, (Item)new ItemIngotGold(), new Vector3(0.0, 0.2, 0.0));
            }
        }
        else {
            this.startTutorial();
        }
        this.phase = GamePhase.GAME;
        this.updateSign();
        return true;
    }
    
    public void postStart() {
        int spawnIndex = 0;
        final Item compass = (Item)new ItemCompass();
        compass.setCustomName(TextFormat.YELLOW + "Traitor Tester");
        final Item clock = (Item)new ItemClock();
        clock.setCustomName(TextFormat.AQUA + "Lobby");
        for (final ArenaPlayerData data : new ArrayList<ArenaPlayerData>(this.playersData.values())) {
            final Player p = data.getPlayerData().getPlayer();
            p.setGamemode(0);
            p.removeAllEffects();
            p.getFoodData().setLevel(20);
            p.getInventory().clearAll();
            p.getInventory().setItem(5, compass);
            p.getInventory().setHotbarSlotIndex(5, 5);
            p.getInventory().setItem(6, clock);
            p.getInventory().setHotbarSlotIndex(6, 6);
            p.getInventory().sendContents(p);
            if (spawnIndex >= this.mapData.spawnPositions.length) {
                spawnIndex = 0;
            }
            final Vector3 pos = this.mapData.spawnPositions[spawnIndex];
            ++spawnIndex;
            p.teleport(new Position(pos.x, pos.y, pos.z, this.level));
            p.setNameTag(p.getName());
        }
        this.messageAllPlayers("compass_ttt", true, "");
    }
    
    public void end(Role role) {
        if (role == null) {
            if (this.players.size() > 0) {
                role = ((ArenaPlayerData)this.playersData.values().stream().toArray()[0]).getRole();
            }
            else {
                role = Role.INNOCENT;
            }
        }
        this.phase = GamePhase.ENDING;
        this.updateSign();
        for (final Player p : this.players.values()) {
            p.getInventory().clearAll();
            p.getInventory().sendContents(p);
            p.setExperience(0, 0);
            p.setHealth(20.0f);
            p.getFoodData().setLevel(20, 20.0f);
        }
        for (final ArenaPlayerData data : this.playersData.values()) {
            if (data.getRole().getSide() == role.getSide()) {
                data.addStat(Stats.Stat.WIN);
                data.addRolePoints(10);
            }
        }
        this.titleAllPlayers("end_game_title", true, role.getColor() + role.getName(this.gameType) + "S");
        this.messageAllPlayers("end_game", role.getColor() + role.getName(this.gameType) + "S");
        this.messageAllPlayers("end_reward", role.getColor() + role.getName(this.gameType) + "S");
        this.messageAllPlayers("end_roles_" + ((this.gameType == GameType.TTT) ? "ttt" : "murder"), String.join(", ", this.traitors));
        if (this.gameType == GameType.MURDER) {
            this.messageAllPlayers("end_roles_detective", this.detectives.get(0));
            if (this.hero != null) {
                this.messageAllPlayers("end_roles_hero", this.hero);
            }
        }
    }
    
    public void stop() {
        this.task.reset();
        this.phase = GamePhase.LOBBY;
        this.starting = false;
        this.testActivated = false;
        this.isLevelLoaded = false;
        this.currentPos = 0;
        for (final ArenaPlayerData data : new ArrayList<ArenaPlayerData>(this.playersData.values())) {
            final Player p = data.getPlayerData().getPlayer();
            this.onLeave(p, false);
            if (data.getPlayerData().stats.get(Stats.Stat.KARMA) <= -100) {
                this.ban(p, "random killing", data.getPlayerData().banCount);
            }
        }
        for (final Player p2 : new ArrayList<Player>(this.spectators.values())) {
            this.unsetSpectator(p2);
        }
        this.winner = null;
        this.updateSign();
        this.traitors.clear();
        this.detectives.clear();
        this.hero = null;
        this.votingManager.createVoteTable();
        this.level.unload();
        this.goldsToSpawn.clear();
        this.currentPos = 0;
        this.titlePos = 0;
        this.plugin.getServer().getScheduler().scheduleAsyncTask((AsyncTask)new AsyncTask() {
            public void onRun() {
                WorldManager.deleteWorld(Arena.this.map, Arena.this.safeId);
            }
        });
    }
    
    public void onJoin(final Player p, final PlayerData globalData) {
        final GTCore.Object.PlayerData playerData = globalData.getData();
        if (this.data.peOnly && p.getLoginChainData().getDeviceOS() == 7 && !p.hasPermission("gameteam.helper") && !p.hasPermission("gameteam.mcpe")) {
            p.sendMessage(Murder.getPrefix() + Language.translate("pe_only", p, new String[0]));
            return;
        }
        if (this.phase != GamePhase.LOBBY) {
            p.sendMessage(Language.translate("game_running", playerData, new String[0]));
            return;
        }
        if (!p.isOp() && !p.hasPermission("gameteam.vip") && this.isArenaFull()) {
            p.sendMessage(Language.translate("full_arena", playerData, new String[0]));
            return;
        }
        MTCore.getInstance().unsetLobby(p);
        p.sendMessage(Language.translate("join", globalData.getData(), this.getId()));
        this.messageArenaPlayers("join_others", true, p.getName());
        globalData.arena = this;
        final ArenaPlayerData data = new ArenaPlayerData(this, globalData);
        this.playersData.put(p.getName().toLowerCase(), data);
        this.players.put(p.getName().toLowerCase(), p);
        p.teleport(this.data.lobby);
        final Item clock = (Item)new ItemClock();
        clock.setCustomName(TextFormat.AQUA + "Lobby");
        p.getInventory().clearAll();
        p.getInventory().setItem(4, clock);
        p.getInventory().setHotbarSlotIndex(4, 4);
        p.getInventory().sendContents(p);
        this.checkStarting();
        this.updateSign();
    }
    
    public void onLeave(final Player p) {
        this.onLeave(p, true);
    }
    
    public void onLeave(final Player p, final boolean message) {
        final Player player = this.players.remove(p.getName().toLowerCase());
        if (player == null) {
            final Player player2 = this.spectators.remove(p.getName().toLowerCase());
            if (player2 != null) {
                this.unsetSpectator(player2);
            }
            return;
        }
        if (p.isOnline()) {
            p.setGamemode(0);
            p.setNameTagAlwaysVisible(true);
            p.setNameTagVisible(true);
            MTCore.getInstance().setLobby(p);
            p.getAdventureSettings().set(AdventureSettings.Type.WORLD_BUILDER, false);
            p.getAdventureSettings().set(AdventureSettings.Type.WORLD_IMMUTABLE, true);
            p.getAdventureSettings().set(AdventureSettings.Type.BUILD_AND_MINE, false);
            p.getAdventureSettings().update();
            p.getInventory().clearAll();
            p.getInventory().sendContents(p);
        }
        final ArenaPlayerData data = this.playersData.remove(p.getName().toLowerCase());
        p.setDisplayName(TextFormat.GRAY + "[" + TextFormat.YELLOW + data.getPlayerData().stats.get(Stats.Stat.KARMA) + TextFormat.GRAY + "] " + p.getDisplayName());
        data.getPlayerData().arena = null;
        if (message) {
            this.messageArenaPlayers("leave", true, p.getDisplayName(), "" + this.players.size(), "" + this.getMaxPlayers());
        }
        this.updateSign();
        this.checkAlive();
    }
    
    public void joinSpectator(final Player p, final boolean respawn) {
        this.spectators.put(p.getName().toLowerCase(), p);
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
        final InventoryContentPacket containerSetContentPacket = new InventoryContentPacket();
        containerSetContentPacket.inventoryId = 121;
        p.dataPacket((DataPacket)containerSetContentPacket);
        p.getInventory().clearAll();
        p.getInventory().sendContents(p);
        if (!respawn) {
            final Vector3 v = this.mapData.spawnPositions[0];
            p.teleport(new Position(v.x, v.y, v.z, this.level));
        }
    }
    
    public void unsetSpectator(final Player p) {
        this.spectators.remove(p.getName().toLowerCase());
        if (p.isOnline()) {
            p.setGamemode(0);
            p.setNameTagAlwaysVisible(true);
            p.setNameTagVisible(true);
            MTCore.getInstance().setLobby(p);
            p.getAdventureSettings().set(AdventureSettings.Type.WORLD_BUILDER, false);
            p.getAdventureSettings().set(AdventureSettings.Type.WORLD_IMMUTABLE, true);
            p.getAdventureSettings().set(AdventureSettings.Type.BUILD_AND_MINE, false);
            p.getAdventureSettings().update();
            final PlayerData data = this.plugin.getPlayerData(p);
            p.setDisplayName(TextFormat.GRAY + "[" + TextFormat.YELLOW + data.stats.get(Stats.Stat.KARMA) + TextFormat.GRAY + "] " + p.getDisplayName());
        }
    }
    
    @EventHandler
    public void onHit(final EntityDamageEvent e) {
        try {
            final Entity entity = e.getEntity();
            if (entity instanceof Player) {
                final Player p = (Player)entity;
                final ArenaPlayerData data = this.getPlayerData(p);
                if (data == null) {
                    return;
                }
                if (this.phase == GamePhase.LOBBY || this.phase == GamePhase.ENDING) {
                    e.setCancelled();
                    if (e.getCause() == EntityDamageEvent.DamageCause.VOID && this.phase == GamePhase.LOBBY) {
                        entity.teleport(this.data.lobby);
                    }
                }
                else if (!e.isCancelled()) {
                    final Entity damager = (e instanceof EntityDamageByEntityEvent) ? ((EntityDamageByEntityEvent)e).getDamager() : null;
                    if (this.gameType == GameType.MURDER) {
                        e.setCancelled();
                        if (damager instanceof Player) {
                            final Player attacker = (Player)damager;
                            final ArenaPlayerData kData = this.getPlayerData(attacker);
                            if (kData == null) {
                                return;
                            }
                            if (kData.getRole() == Role.MURDERER && attacker.getInventory().getItemInHand().isSword()) {
                                this.processKill(p, attacker);
                                return;
                            }
                            if (e instanceof EntityDamageByChildEntityEvent && ((EntityDamageByChildEntityEvent)e).getChild() instanceof EntityArrow) {
                                this.processKill(p, attacker);
                                if (kData.getRole().getSide() == data.getRole().getSide()) {
                                    this.processKill(attacker, null);
                                }
                            }
                        }
                        else if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                            this.processKill(p, null);
                        }
                        return;
                    }
                    if (this.gameType == GameType.TTT && this.task.gameTime < 30) {
                        e.setCancelled();
                        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                            p.teleport(this.mapData.spawnPositions[0]);
                        }
                        return;
                    }
                    if (damager instanceof Player) {
                        final Player attacker = (Player)damager;
                        final ArenaPlayerData kData = this.getPlayerData(attacker);
                        if (kData.getRole() == Role.MURDERER) {
                            if (data.getRole() == Role.MURDERER) {
                                attacker.addEffect(Effect.getEffect(15).setDuration(60).setVisible(false).setAmplifier(1));
                            }
                        }
                        else if (data.getRole() == Role.DETECTIVE) {
                            attacker.addEffect(Effect.getEffect(15).setDuration(60).setVisible(false).setAmplifier(1));
                        }
                    }
                    if (p.getHealth() - e.getFinalDamage() < 1.0f) {
                        e.setCancelled();
                        this.processKill(p, (damager instanceof Player) ? damager : null);
                    }
                }
            }
            else {
                e.setCancelled();
            }
        }
        catch (Exception ex) {
            MainLogger.getLogger().logException(ex);
        }
    }
    
    private void processKill(final Player player, final Player damager) {
        final ArenaPlayerData data = this.getPlayerData(player);
        this.players.remove(player.getName().toLowerCase());
        this.playersData.remove(player.getName().toLowerCase());
        if (this.gameType == GameType.TTT) {
            for (final Item item : player.getInventory().getContents().values()) {
                this.level.dropItem((Vector3)player, item);
            }
        }
        if (damager != null) {
            final ArenaPlayerData damagerData = this.getPlayerData(damager);
            if (damagerData != null) {
                String kill;
                int karma;
                if (data.getRole().getSide() != damagerData.getRole().getSide()) {
                    damagerData.addStat(Stats.Stat.RIGHTKILL);
                    damagerData.addRolePoints(5);
                    kill = "award";
                    karma = ((damagerData.getRole() == Role.MURDERER) ? 10 : 20);
                }
                else {
                    damagerData.addStat(Stats.Stat.WRONGKILL);
                    kill = "lose";
                    karma = -20;
                }
                if (this.gameType == GameType.TTT) {
                    damager.sendMessage(Language.translate("karma_" + kill, damager, "" + Math.abs(karma), data.getRole().getName(this.gameType).toLowerCase(), player.getName()));
                }
                if (this.gameType == GameType.MURDER) {
                    player.sendTitle(Language.translate("kill", player, new String[0]), Language.translate("kill_sub", player, damagerData.getRole().getColor() + damagerData.getRole().getName(this.gameType)));
                }
            }
        }
        else if (this.gameType == GameType.MURDER) {
            player.sendTitle(Language.translate("kill", player, new String[0]));
        }
        final PlayerInventory inv = player.getInventory();
        for (final Item item2 : inv.getContents().values()) {
            if (item2.getId() == 261) {
                final String[] lore = item2.getLore();
                if (lore.length == 0) {
                    continue;
                }
                if (lore[0].equals("Detective Bow")) {
                    this.spawnBow(player);
                    this.titleAllPlayers("bow_drop", true, new String[0]);
                    break;
                }
                continue;
            }
        }
        player.addEffect(Effect.getEffect(15).setDuration(30));
        if (this.gameType == GameType.MURDER) {
            final LevelSoundEventPacket pk = new LevelSoundEventPacket();
            pk.sound = 17;
            pk.isGlobal = true;
            for (final Player pl : this.players.values()) {
                pk.x = (float)pl.x;
                pk.y = (float)pl.y;
                pk.z = (float)pl.z;
                pl.dataPacket((DataPacket)pk);
            }
        }
        final LevelSoundEventPacket pk = new LevelSoundEventPacket();
        pk.sound = 18;
        pk.pitch = 2869;
        pk.x = (float)player.x;
        pk.x = (float)player.y + player.getEyeHeight();
        pk.x = (float)player.z;
        pk.isGlobal = true;
        player.dataPacket((DataPacket)pk);
        data.getPlayerData().arena = null;
        this.joinSpectator(player, true);
        if (player.y > 0.0) {
            this.createDeadNPC(player, data.getRole(), this.gameType);
        }
        if (damager != null && data.getRole() == Role.MURDERER) {
            this.hero = damager.getName();
        }
        this.checkAlive();
        this.updateSign();
        this.messageAllPlayers("remain", true, "" + this.players.size());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(final PlayerQuitEvent e) {
        this.onLeave(e.getPlayer());
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onChat(final PlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        e.setRecipients((Set)new HashSet());
        final Player p = e.getPlayer();
        final ArenaPlayerData data = this.getPlayerData(p);
        if (data == null) {
            return;
        }
        e.setCancelled();
        this.messageAllPlayers(e.getMessage(), p, data);
    }
    
    @EventHandler
    public void onItemPickup(final InventoryPickupItemEvent e) {
        if (this.gameType != GameType.MURDER || this.phase != GamePhase.GAME) {
            return;
        }
        final Item item = e.getItem().getItem();
        final Inventory inv = e.getInventory();
        if (item.getId() == 266 && inv.getHolder() instanceof Player) {
            final Player p = (Player)inv.getHolder();
            final Vector3 itemPos = (Vector3)e.getItem().floor();
            for (int i = 0; i < this.mapData.goldSpawn.length; ++i) {
                if (this.mapData.goldSpawn[i].equals((Object)itemPos)) {
                    this.goldsToSpawn.add(i);
                    break;
                }
            }
            final ArenaPlayerData data = this.getPlayerData(p);
            if (data == null || data.getRole() == Role.MURDERER) {
                return;
            }
            final Item gold = (Item)new ItemIngotGold(0, Math.max(0, 10 - item.getCount()));
            if (inv.contains(gold)) {
                inv.remove(gold);
                e.setCancelled();
                e.getItem().close();
                boolean found = false;
                for (final Item it : inv.getContents().values()) {
                    if (it.getId() == 261) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    inv.addItem(new Item[] { new ItemBow() });
                }
                inv.addItem(new Item[] { new ItemArrow() });
                inv.sendContents(p);
            }
        }
    }
    
    @EventHandler
    public void onArrowPickup(final InventoryPickupArrowEvent e) {
        if (this.gameType == GameType.MURDER) {
            e.setCancelled();
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onHunger(final PlayerFoodLevelChangeEvent e) {
        e.setCancelled();
    }
    
    @EventHandler
    public void onHeldItem(final PlayerItemHeldEvent e) {
        if (this.phase != GamePhase.GAME || this.gameType != GameType.MURDER) {
            return;
        }
        final Player p = e.getPlayer();
        final Item item = e.getItem();
        if (item.getId() != 331 && item.getId() != 345) {
            return;
        }
        final ArenaPlayerData data = this.getPlayerData(p);
        if (data == null) {
            return;
        }
        if (data.getRole() == Role.MURDERER && item.getId() == 331) {
            final long time = System.currentTimeMillis();
            if (data.lastSword >= time) {
                p.sendMessage(Murder.getPrefix() + Language.translate("sword_delay", p, "" + (data.lastSword - time) / 1000L));
                return;
            }
            data.lastSword = time + 10000L;
            this.throwSword(p);
        }
    }
    
    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent e) {
        if ((this.phase != GamePhase.GAME || this.gameType != GameType.TTT) && !e.getPlayer().isOp()) {
            e.setCancelled();
        }
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEntityEvent e) {
        if (this.gameType != GameType.TTT || this.phase != GamePhase.GAME) {
            return;
        }
        final Player p = e.getPlayer();
        final ArenaPlayerData data = this.getPlayerData(p);
        if (data == null) {
            return;
        }
        final Entity entity = e.getEntity();
        if (entity instanceof DeadEntity && entity.getNameTag().equals("Unknown")) {
            final DeadEntity deadEntity = (DeadEntity)entity;
            deadEntity.setRealName();
            final Role role = deadEntity.getRole();
            this.messageAllPlayers("discover", true, deadEntity.getNameTag());
            this.messageAllPlayers("discover_result", true, deadEntity.getNameTag(), role.getColor() + role.getName(this.gameType));
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final Item item = e.getItem();
        final ArenaPlayerData data = this.getPlayerData(p);
        if (data == null) {
            return;
        }
        if (e.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && item.getId() == 347 && item.getCustomName().equals(TextFormat.AQUA + "Lobby")) {
            this.onLeave(p, true);
            e.setCancelled();
            return;
        }
        if (this.phase != GamePhase.GAME) {
            return;
        }
        final Block b = e.getBlock();
        if (this.gameType == GameType.TTT && e.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (b.getId() == 54) {
                if (data.items.isEmpty()) {
                    p.sendMessage(Murder.getPrefix() + Language.translate("chest_limit", p, new String[0]));
                    e.setCancelled();
                    return;
                }
                final Item itemm = data.items.remove(0);
                p.getInventory().addItem(new Item[] { itemm });
                if (itemm.getId() == 261) {
                    p.getInventory().addItem(new Item[] { new ItemArrow(0, 32) });
                }
                e.setCancelled();
                b.getLevel().setBlock((Vector3)b, (Block)new BlockAir(), true, false);
                this.level.addSound((Sound)new ExperienceOrbSound((Vector3)p.add(0.0, (double)p.getEyeHeight(), 0.0)), p);
            }
            else if (b.getId() == 130) {
                if (this.task.gameTime <= 30) {
                    e.setCancelled();
                    return;
                }
                if (new Random().nextInt(5) == 0) {
                    this.level.addSound((Sound)new ExplodeSound((Vector3)b));
                    return;
                }
                p.getInventory().addItem(new Item[] { new ItemSwordIron() });
                p.getInventory().sendContents(p);
                e.setCancelled();
                b.getLevel().setBlock((Vector3)b, (Block)new BlockAir(), true, false);
                this.level.addSound((Sound)new ExperienceOrbSound((Vector3)p.add(0.0, (double)p.getEyeHeight(), 0.0)), p);
            }
            else if (this.task.gameTime > 30 && !this.testActivated && b instanceof BlockButton && b.equals((Object)this.mapData.testerButton) && this.mapData.traitorTester.isVectorInside((Vector3)p.floor())) {
                this.switchTester();
                this.testedPlayer = p;
                this.messageAllPlayers("tester_enter", true, p.getName());
                this.messageAllPlayers("tester_result", true, "");
            }
        }
        else if (this.gameType == GameType.MURDER && e.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && data.getRole() == Role.MURDERER && (item.getId() == 267 || item.getId() == 345)) {
            if (item.getId() == 267) {
                final long time = System.currentTimeMillis();
                if (data.lastSword >= time) {
                    p.sendMessage(Murder.getPrefix() + Language.translate("sword_delay", p, "" + (data.lastSword - time) / 1000L));
                    return;
                }
                data.lastSword = time + 10000L;
                this.throwSword(p);
            }
            else {
                this.printTraceToNearestPlayer(data);
            }
        }
        else if (this.gameType == GameType.TTT && e.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && item.getCustomName().toLowerCase().contains("traitor tester")) {
            this.pointCompass(p, this.mapData.testerButton.add(0.0, -1.5));
            p.sendActionBar(TextFormat.GREEN + "Pointing to " + TextFormat.RED + "Traitor Tester", 5, 40, 5);
        }
    }
    
    @EventHandler
    public void onSpawn(final PlayerSpawnEvent e) {
        try {
            if (this.gameType != GameType.TTT || this.phase != GamePhase.GAME) {
                return;
            }
            final Player target = e.getTarget();
            final ArenaPlayerData targetData = this.getPlayerData(target);
            if (targetData == null || targetData.getRole() != Role.MURDERER) {
                return;
            }
            final Player p = e.getPlayer();
            final ArenaPlayerData data = this.getPlayerData(p);
            if (data == null) {
                return;
            }
            final EntityMetadata metadata = p.getDataProperties();
            final EntityMetadata newMetadata = new EntityMetadata();
            Reflect.on((Object)newMetadata).set("map", (Object)new HashMap((Map<?, ?>)Reflect.on((Object)metadata).get("map")));
            newMetadata.put((EntityData)new StringEntityData(4, data.getRole().getColor() + p.getName()));
            final AddPlayerPacket pk = (AddPlayerPacket)e.getPacket();
            pk.metadata = newMetadata;
            e.setPacket((DataPacket)pk);
        }
        catch (Exception ex) {
            MainLogger.getLogger().logException(ex);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBowPickup(final PlayerPickupBowEvent e) {
        final Player p = e.getPlayer();
        if (p.gamemode != 0) {
            e.setCancelled();
            return;
        }
        if (this.gameType != GameType.MURDER) {
            return;
        }
        final ArenaPlayerData data = this.getPlayerData(p);
        if (data == null) {
            return;
        }
        if (data.getRole() == Role.MURDERER) {
            e.setCancelled();
            return;
        }
        final Item bow = (Item)new ItemBow();
        bow.setLore(new String[] { "Detective Bow" });
        boolean found = false;
        for (final Map.Entry<Integer, Item> entry : p.getInventory().getContents().entrySet()) {
            final Item item = entry.getValue();
            if (item.getId() == 261) {
                p.getInventory().setItem((int)entry.getKey(), bow);
                found = true;
                break;
            }
        }
        if (!found) {
            p.getInventory().addItem(new Item[] { bow });
        }
        p.getInventory().addItem(new Item[] { new ItemArrow() });
        p.getInventory().sendContents(p);
        this.titleAllPlayers("bow_pick", true, new String[0]);
    }
    
    public void selectMap() {
        this.selectMap(false);
    }
    
    public void selectMap(final boolean force) {
        if (this.players.size() <= 1 && !force) {
            this.messageAllPlayers("min_players", true, this.getMinPlayers() + "");
            this.starting = false;
            this.task.startTime = 50;
            return;
        }
        String map = "";
        int points = -10;
        for (final Map.Entry<String, Integer> entry : this.votingManager.stats.entrySet()) {
            if (points < entry.getValue()) {
                map = entry.getKey();
                points = entry.getValue();
            }
        }
        if (this.plugin.getServer().isLevelLoaded(map)) {
            this.plugin.getServer().unloadLevel(this.plugin.getServer().getLevelByName(map));
        }
        this.plugin.getServer().getScheduler().scheduleAsyncTask((AsyncTask)new WorldPrepareTask(this, map, this.safeId, force));
        this.map = map;
        this.mapData = ((this.gameType == GameType.TTT) ? this.plugin.tttMaps.get(map) : this.plugin.murderMaps.get(map));
        this.messageAllPlayers("select_map", true, this.map);
    }
    
    public void onMove(final PlayerMoveEvent e) {
        final Player p = e.getPlayer();
        if (p.gamemode != 3 || this.task.tutorialTime < 0) {
            return;
        }
        if (this.inArenaFast(p)) {
            e.setCancelled();
        }
    }
    
    @EventHandler
    public void onBowShot(final EntityShootBowEvent e) {
        if (this.gameType != GameType.MURDER || this.phase != GamePhase.GAME) {
            return;
        }
        final Entity entity = (Entity)e.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player p = (Player)entity;
        final ArenaPlayerData data = this.getPlayerData(p);
        if (data == null || data.getRole() != Role.DETECTIVE) {
            return;
        }
        if (data.bowTask != null && !data.bowTask.getHandler().isCancelled()) {
            e.setCancelled();
        }
        data.bowTask = new BowRechargeTask(p, 5);
        this.plugin.getServer().getScheduler().scheduleRepeatingTask((Task)data.bowTask, 1);
    }
    
    public String getId() {
        return this.id;
    }
    
    public Murder getPlugin() {
        return this.plugin;
    }
    
    public enum GameType
    {
        MURDER(TextFormat.DARK_RED + "Kdo je vrah"), 
        TTT(TextFormat.GOLD + "Kdo je vrah " + TextFormat.BLUE + "II");
        
        private String signName;
        
        private GameType(final String signName) {
            this.signName = signName;
        }
    }
    
    public enum GamePhase
    {
        LOBBY, 
        GAME, 
        ENDING;
    }
}
