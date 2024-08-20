package murder;

import GTCore.minigame.*;
import GTCore.Object.*;
import murder.arena.*;
import GTCore.*;
import cn.nukkit.entity.*;
import murder.entity.*;
import cn.nukkit.plugin.*;
import murder.task.*;
import cn.nukkit.scheduler.*;
import murder.arena.object.*;
import cn.nukkit.math.*;
import cn.nukkit.level.*;
import cn.nukkit.command.*;
import murder.command.*;
import cn.nukkit.utils.*;
import murder.language.*;
import java.util.*;
import GTCore.Event.*;
import cn.nukkit.*;
import murder.mysql.*;
import cn.nukkit.event.*;
import cn.nukkit.block.*;
import cn.nukkit.event.player.*;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.block.*;

public class Murder extends PluginBase implements Listener, Minigame
{
    private static Murder instance;
    private String id;
    public BossBar bossBar;
    private HashMap<String, Arena> arenas;
    public HashMap<Long, PlayerData> players;
    public static boolean DEBUG;
    public final HashMap<String, MapData> tttMaps;
    public final HashMap<String, MapData> murderMaps;
    
    public Murder() {
        this.arenas = new HashMap<String, Arena>();
        this.players = new HashMap<Long, PlayerData>();
        this.tttMaps = new HashMap<String, MapData>();
        this.murderMaps = new HashMap<String, MapData>();
    }
    
    public void onLoad() {
        Murder.instance = this;
        MTCore.getInstance().minigame = (Minigame)this;
        Entity.registerEntity("DeadEntity", (Class)DeadEntity.class, true);
        Entity.registerEntity("BowEntity", (Class)BowEntity.class, true);
        Entity.registerEntity("DeadPlayerEntity", (Class)DeadPlayerEntity.class, true);
    }
    
    public void onEnable() {
        this.saveDefaultConfig();
        this.id = this.getConfig().getString("id");
        this.initLanguage();
        this.registerCommands();
        this.registerMaps();
        this.registerArenas();
        MTCore.getInstance().enableAutoRestart();
        (this.bossBar = new BossBar((Plugin)this)).setHealth(300);
        this.bossBar.setMaxHealth(300);
        this.getServer().getScheduler().scheduleDelayedRepeatingTask((Task)new BossBarTask(this, TextFormat.YELLOW + "Kdo je vrah"), 2, 2);
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
    }
    
    public void onDisable() {
        MTCore.isShuttingDown = true;
        for (final PlayerData data : this.players.values()) {
            new QuitQuery(new PlayerData[] { data });
        }
    }
    
    private void registerArenas() {
        this.registerArena("Kdo je Vrah-1", new ArenaData() {
            {
                this.sign = new Vector3(-54.0, 57.0, -690.0);
                this.lobby = new Vector3(-725.5, 54.0, -689.5);
                this.gameType = Arena.GameType.TTT;
                this.peOnly = true;
            }
        });
        this.registerArena("Kdo je Vrah-2", new ArenaData() {
            {
                this.sign = new Vector3(-54.0, 57.0, -691.0);
                this.lobby = new Vector3(-725.5, 54.0, -689.5);
                this.gameType = Arena.GameType.TTT;
            }
        });
        this.registerArena("Kdo je Vrah-3", new ArenaData() {
            {
                this.sign = new Vector3(-54.0, 57.0, -692.0);
                this.lobby = new Vector3(-725.5, 54.0, -689.5);
                this.gameType = Arena.GameType.TTT;
            }
        });
        this.registerArena("Kdo je Vrah-4", new ArenaData() {
            {
                this.sign = new Vector3(-54.0, 57.0, -694.0);
                this.lobby = new Vector3(-725.5, 54.0, -689.5);
                this.gameType = Arena.GameType.MURDER;
                this.peOnly = true;
            }
        });
        this.registerArena("Kdo je Vrah-5", new ArenaData() {
            {
                this.sign = new Vector3(-54.0, 57.0, -695.0);
                this.lobby = new Vector3(-725.5, 54.0, -689.5);
                this.gameType = Arena.GameType.MURDER;
            }
        });
        this.registerArena("Kdo je Vrah-6", new ArenaData() {
            {
                this.sign = new Vector3(-54.0, 57.0, -696.0);
                this.lobby = new Vector3(-725.5, 54.0, -689.5);
                this.gameType = Arena.GameType.MURDER;
            }
        });
        this.registerArena("Kdo je Vrah-7", new ArenaData() {
            {
                this.sign = new Vector3(-54.0, 56.0, -694.0);
                this.lobby = new Vector3(-725.5, 54.0, -689.5);
                this.gameType = Arena.GameType.MURDER;
                this.peOnly = true;
            }
        });
        this.registerArena("Kdo je Vrah-8", new ArenaData() {
            {
                this.sign = new Vector3(-54.0, 56.0, -695.0);
                this.lobby = new Vector3(-725.5, 54.0, -689.5);
                this.gameType = Arena.GameType.MURDER;
            }
        });
        this.registerArena("Kdo je Vrah-9", new ArenaData() {
            {
                this.sign = new Vector3(-54.0, 56.0, -696.0);
                this.lobby = new Vector3(-725.5, 54.0, -689.5);
                this.gameType = Arena.GameType.MURDER;
            }
        });
    }
    
    private void registerMaps() {
        MapData data = new MapData();
        data.name = "Woodbury";
        data.spawnPositions = new Vector3[] { new Vector3(-25.0, 78.0, 89.0), new Vector3(-25.0, 78.0, 81.0), new Vector3(-25.0, 78.0, 73.0), new Vector3(-25.0, 78.0, 65.0), new Vector3(-25.0, 78.0, 57.0), new Vector3(-25.0, 78.0, 49.0), new Vector3(-25.0, 78.0, 41.0), new Vector3(-25.0, 78.0, 33.0), new Vector3(-25.0, 78.0, 25.0), new Vector3(-25.0, 78.0, 17.0), new Vector3(-25.0, 78.0, 9.0), new Vector3(-25.0, 78.0, 1.0), new Vector3(-52.0, 78.0, 89.0), new Vector3(-52.0, 78.0, 81.0), new Vector3(-52.0, 78.0, 73.0), new Vector3(-52.0, 78.0, 65.0), new Vector3(-52.0, 78.0, 57.0), new Vector3(-52.0, 78.0, 49.0), new Vector3(-52.0, 78.0, 41.0), new Vector3(-52.0, 78.0, 33.0), new Vector3(-52.0, 78.0, 25.0), new Vector3(-52.0, 78.0, 17.0), new Vector3(-52.0, 78.0, 9.0), new Vector3(-52.0, 78.0, 1.0) };
        data.lamps = new Vector3[] { new Vector3(-61.0, 81.0, -23.0), new Vector3(-61.0, 81.0, -19.0) };
        data.testerButton = new Vector3(-64.0, 80.0, -21.0);
        data.traitorTester = new AxisAlignedBB(-64.0, 78.0, -22.0, -62.0, 82.0, -20.0).expand(0.5, 0.5, 0.5);
        data.traitorTesterGate = new AxisAlignedBB(-61.0, 79.0, -22.0, -61.0, 81.0, -20.0);
        data.tutorial = new Location[] { new Location(-37.0, 91.0, -64.0, 0.0, 10.0), new Location(-12.0, 81.0, 3.0, 270.0, 9.0), new Location(-52.0, 80.5, -20.5, 90.0, 12.0) };
        this.tttMaps.put("Woodbury", data);
        data = new MapData();
        data.name = "Woodburry";
        data.spawnPositions = new Vector3[] { new Vector3(-25.0, 78.0, 89.0), new Vector3(-25.0, 78.0, 81.0), new Vector3(-25.0, 78.0, 73.0), new Vector3(-25.0, 78.0, 65.0), new Vector3(-25.0, 78.0, 57.0), new Vector3(-25.0, 78.0, 49.0), new Vector3(-25.0, 78.0, 41.0), new Vector3(-25.0, 78.0, 33.0), new Vector3(-25.0, 78.0, 25.0), new Vector3(-25.0, 78.0, 17.0), new Vector3(-25.0, 78.0, 9.0), new Vector3(-25.0, 78.0, 1.0), new Vector3(-52.0, 78.0, 89.0), new Vector3(-52.0, 78.0, 81.0), new Vector3(-52.0, 78.0, 73.0), new Vector3(-52.0, 78.0, 65.0), new Vector3(-52.0, 78.0, 57.0), new Vector3(-52.0, 78.0, 49.0), new Vector3(-52.0, 78.0, 41.0), new Vector3(-52.0, 78.0, 33.0), new Vector3(-52.0, 78.0, 25.0), new Vector3(-52.0, 78.0, 17.0), new Vector3(-52.0, 78.0, 9.0), new Vector3(-52.0, 78.0, 1.0) };
        data.goldSpawn = new Vector3[] { new Vector3(-5.0, 79.0, -74.0), new Vector3(-8.0, 79.0, -56.0), new Vector3(3.0, 79.0, -60.0), new Vector3(-9.0, 79.0, -32.0), new Vector3(-9.0, 79.0, -16.0), new Vector3(-8.0, 78.0, 9.0), new Vector3(5.0, 79.0, -3.0), new Vector3(22.0, 79.0, -2.0), new Vector3(-12.0, 79.0, 24.0), new Vector3(-5.0, 78.0, 49.0), new Vector3(-11.0, 79.0, 64.0), new Vector3(-6.0, 79.0, 75.0), new Vector3(-6.0, 79.0, 95.0), new Vector3(-63.0, 79.0, 88.0), new Vector3(-85.0, 79.0, 84.0), new Vector3(-72.0, 79.0, 57.0), new Vector3(-70.0, 79.0, 50.0), new Vector3(-77.0, 79.0, 32.0), new Vector3(-69.0, 79.0, 8.0), new Vector3(-75.0, 79.0, 0.0), new Vector3(-63.0, 79.0, -12.0), new Vector3(-68.0, 79.0, -3.0), new Vector3(-76.0, 79.0, -34.0), new Vector3(-66.0, 79.0, -38.0), new Vector3(-72.0, 79.0, -54.0), new Vector3(-67.0, 79.0, -74.0), new Vector3(-77.0, 79.0, -64.0), new Vector3(-38.0, 78.0, -14.0), new Vector3(-31.0, 82.0, -2.0), new Vector3(-52.0, 78.0, 20.0), new Vector3(-34.0, 78.0, 51.0), new Vector3(-59.0, 79.0, 64.0), new Vector3(-38.0, 78.0, 88.0), new Vector3(-23.0, 79.0, 93.0) };
        for (int i = 0; i < data.goldSpawn.length; ++i) {
            data.goldSpawn[i].add(0.5, 0.5, 0.5);
        }
        data = new MapData();
        data.name = "Forge";
        data.spawnPositions = new Vector3[] { new Vector3(61.0, 74.0, 0.0), new Vector3(56.0, 74.0, 0.0), new Vector3(51.0, 74.0, 0.0), new Vector3(46.0, 74.0, 0.0), new Vector3(41.0, 74.0, 0.0), new Vector3(36.0, 74.0, 0.0), new Vector3(31.0, 74.0, 0.0), new Vector3(26.0, 74.0, 0.0), new Vector3(21.0, 74.0, 0.0), new Vector3(16.0, 74.0, 0.0), new Vector3(11.0, 74.0, 0.0), new Vector3(6.0, 74.0, 0.0), new Vector3(1.0, 74.0, 0.0), new Vector3(-4.0, 74.0, 0.0), new Vector3(-9.0, 74.0, 0.0), new Vector3(-14.0, 74.0, 0.0), new Vector3(-19.0, 74.0, 0.0), new Vector3(-24.0, 74.0, 0.0), new Vector3(-29.0, 74.0, 0.0), new Vector3(-34.0, 74.0, 0.0), new Vector3(-39.0, 74.0, 0.0), new Vector3(-44.0, 74.0, 0.0), new Vector3(-49.0, 74.0, 0.0), new Vector3(-54.0, 74.0, 0.0) };
        data.lamps = new Vector3[] { new Vector3(62.0, 74.0, 14.0), new Vector3(62.0, 74.0, 18.0) };
        data.testerButton = new Vector3(65.0, 73.0, 16.0);
        data.traitorTester = new AxisAlignedBB(63.0, 72.0, 15.0, 65.0, 74.0, 17.0).expand(0.5, 0.5, 0.5);
        data.traitorTesterGate = new AxisAlignedBB(62.0, 72.0, 15.0, 63.0, 73.0, 17.0);
        data.tutorial = new Location[] { new Location(25.5, 47.8, 26.77, 136.0, -2.0), new Location(76.7, 110.4, -20.89, 51.3, 7.76), new Location(51.7, 73.82, 16.42, 266.8, 16.11) };
        this.tttMaps.put("Forge", data);
        data = new MapData();
        data.name = "Mineville";
        data.spawnPositions = new Vector3[] { new Vector3(-33.0, 65.0, 3.0), new Vector3(-23.0, 65.0, 3.0), new Vector3(-13.0, 65.0, 3.0), new Vector3(-3.0, 65.0, 3.0), new Vector3(7.0, 65.0, 3.0), new Vector3(17.0, 65.0, 3.0), new Vector3(27.0, 64.0, 3.0), new Vector3(37.0, 64.0, 3.0), new Vector3(47.0, 64.0, 3.0), new Vector3(57.0, 64.0, 3.0), new Vector3(67.0, 64.0, 3.0), new Vector3(77.0, 64.0, 3.0), new Vector3(76.0, 64.0, -29.0), new Vector3(81.0, 64.0, -29.0), new Vector3(86.0, 64.0, -29.0), new Vector3(91.0, 64.0, -29.0), new Vector3(96.0, 64.0, -29.0), new Vector3(101.0, 64.0, -29.0), new Vector3(106.0, 64.0, -29.0), new Vector3(111.0, 64.0, -29.0), new Vector3(116.0, 64.0, -29.0), new Vector3(121.0, 64.0, -29.0), new Vector3(126.0, 64.0, -29.0), new Vector3(131.0, 64.0, -29.0) };
        data.lamps = new Vector3[] { new Vector3(39.0, 67.0, -70.0), new Vector3(39.0, 67.0, -73.0) };
        data.testerButton = new Vector3(36.0, 65.0, -71.0);
        data.traitorTester = new AxisAlignedBB(36.0, 65.0, -72.0, 37.0, 67.0, -71.0).expand(0.5, 0.5, 0.5);
        data.traitorTesterGate = new AxisAlignedBB(38.0, 65.0, -72.0, 38.0, 66.0, -71.0);
        data.tutorial = new Location[] { new Location(37.5, 83.5, -25.3, 307.0, 23.3), new Location(150.0, 79.3, -15.2, 64.0, 13.0), new Location(52.5, 70.0, -49.37, 148.0, 13.0) };
        this.tttMaps.put("Mineville", data);
        data = new MapData();
        data.name = "Canal";
        data.spawnPositions = new Vector3[] { new Vector3(-8.0, 70.0, -33.0), new Vector3(-8.0, 70.0, -27.0), new Vector3(-8.0, 70.0, -21.0), new Vector3(-8.0, 70.0, -15.0), new Vector3(-8.0, 70.0, -9.0), new Vector3(-8.0, 70.0, -3.0), new Vector3(-8.0, 70.0, 3.0), new Vector3(-8.0, 70.0, 9.0), new Vector3(-8.0, 70.0, 15.0), new Vector3(-8.0, 70.0, 21.0), new Vector3(-8.0, 70.0, 27.0), new Vector3(-8.0, 70.0, 33.0), new Vector3(8.0, 70.0, -33.0), new Vector3(8.0, 70.0, -27.0), new Vector3(8.0, 70.0, -21.0), new Vector3(8.0, 70.0, -15.0), new Vector3(8.0, 70.0, -9.0), new Vector3(8.0, 70.0, -3.0), new Vector3(8.0, 70.0, 3.0), new Vector3(8.0, 70.0, 9.0), new Vector3(8.0, 70.0, 15.0), new Vector3(8.0, 70.0, 21.0), new Vector3(8.0, 70.0, 27.0), new Vector3(8.0, 70.0, 33.0) };
        data.lamps = new Vector3[] { new Vector3(-59.0, 72.0, -3.0), new Vector3(-55.0, 72.0, -3.0) };
        data.testerButton = new Vector3(-57.0, 71.0, -5.0);
        data.traitorTester = new AxisAlignedBB(-58.0, 70.0, -5.0, -56.0, 72.0, -4.0).expand(0.5, 0.5, 0.5);
        data.traitorTesterGate = new AxisAlignedBB(-58.0, 70.0, -3.0, -56.0, 71.0, -3.0);
        data.tutorial = new Location[] { new Location(-10.0, 82.0, -35.0, 322.0, 13.0), new Location(7.0, 69.0, 19.0, 121.0, -11.0), new Location(-58.0, 71.0, 3.0, 221.0, 13.0) };
        this.tttMaps.put("Canal", data);
        data = new MapData();
        data.name = "Archives";
        data.spawnPositions = new Vector3[] { new Vector3(-31.0, 65.0, -20.0), new Vector3(-23.0, 65.0, -35.0), new Vector3(-10.0, 65.0, -35.0), new Vector3(-2.0, 65.0, -29.0), new Vector3(2.0, 65.0, -18.0), new Vector3(6.0, 65.0, -9.0), new Vector3(20.0, 65.0, -3.0), new Vector3(19.0, 65.0, 5.0), new Vector3(7.0, 65.0, 11.0), new Vector3(-10.0, 65.0, 0.0), new Vector3(-37.0, 75.0, 0.0), new Vector3(-62.0, 75.0, 0.0), new Vector3(0.0, 65.0, 21.0), new Vector3(-1.0, 65.0, 36.0), new Vector3(-33.0, 65.0, 19.0), new Vector3(-58.0, 64.0, 1.0) };
        data.goldSpawn = new Vector3[] { new Vector3(-60.0, 64.0, 4.0), new Vector3(-49.0, 64.0, -22.0), new Vector3(-33.0, 65.0, -26.0), new Vector3(2.0, 65.0, -35.0), new Vector3(19.0, 65.0, -4.0), new Vector3(0.0, 65.0, 19.0), new Vector3(-40.0, 75.0, 3.0), new Vector3(-70.0, 81.0, -1.0), new Vector3(-60.0, 75.0, -8.0), new Vector3(-20.0, 65.0, 36.0), new Vector3(-32.0, 65.0, 20.0), new Vector3(-53.0, 64.0, 23.0), new Vector3(-11.0, 65.0, 25.0) };
        for (int i = 0; i < data.goldSpawn.length; ++i) {
            data.goldSpawn[i].add(0.5, 0.5, 0.5);
        }
        this.murderMaps.put("Archives", data);
        data = new MapData();
        data.name = "Mansion";
        data.spawnPositions = new Vector3[] { new Vector3(-38.0, 43.0, -1.0), new Vector3(-49.0, 43.0, -1.0), new Vector3(-45.0, 43.0, -16.0), new Vector3(-41.0, 43.0, -15.0), new Vector3(-31.0, 43.0, -20.0), new Vector3(-31.0, 43.0, -13.0), new Vector3(-38.0, 43.0, -10.0), new Vector3(-39.0, 49.0, 6.0), new Vector3(-39.0, 49.0, 0.0), new Vector3(-33.0, 49.0, -6.0), new Vector3(-48.0, 49.0, -6.0), new Vector3(-49.0, 49.0, -18.0), new Vector3(-43.0, 49.0, -15.0), new Vector3(-36.0, 49.0, -19.0), new Vector3(-29.0, 49.0, -17.0), new Vector3(-50.0, 49.0, 1.0) };
        data.goldSpawn = new Vector3[] { new Vector3(-37.0, 43.0, 17.0), new Vector3(-27.0, 43.0, 11.0), new Vector3(-28.0, 43.0, 0.0), new Vector3(-40.0, 43.0, 1.0), new Vector3(-48.0, 43.0, 1.0), new Vector3(-44.0, 43.0, -9.0), new Vector3(-49.0, 43.0, -20.0), new Vector3(-33.0, 49.0, -11.0), new Vector3(-49.0, 49.0, 2.0), new Vector3(-33.0, 49.0, 0.0), new Vector3(-30.0, 55.0, -15.0), new Vector3(-63.0, 55.0, -3.0), new Vector3(-46.0, 55.0, -14.0), new Vector3(-26.0, 49.0, -17.0) };
        for (int i = 0; i < data.goldSpawn.length; ++i) {
            data.goldSpawn[i].add(0.5, 0.5, 0.5);
        }
        this.murderMaps.put("Mansion", data);
        data = new MapData();
        data.name = "Base";
        data.spawnPositions = new Vector3[] { new Vector3(66.0, 63.0, -148.0), new Vector3(59.0, 63.0, -149.0), new Vector3(55.0, 63.0, -149.0), new Vector3(53.0, 64.0, -153.0), new Vector3(53.0, 64.0, -157.0), new Vector3(53.0, 64.0, -161.0), new Vector3(53.0, 64.0, -167.0), new Vector3(53.0, 64.0, -172.0), new Vector3(53.0, 64.0, -178.0), new Vector3(53.0, 64.0, -184.0), new Vector3(58.0, 64.0, -152.0), new Vector3(63.0, 61.0, -158.0), new Vector3(65.0, 59.0, -162.0), new Vector3(63.0, 57.0, -167.0), new Vector3(63.0, 57.0, -173.0), new Vector3(63.0, 57.0, -178.0) };
        data.goldSpawn = new Vector3[] { new Vector3(45.0, 57.0, -187.0), new Vector3(58.0, 57.0, -171.0), new Vector3(58.0, 57.0, -159.0), new Vector3(48.0, 57.0, -160.0), new Vector3(46.0, 57.0, -167.0), new Vector3(47.0, 58.0, -151.0), new Vector3(56.0, 58.0, -131.0), new Vector3(65.0, 59.0, -144.0), new Vector3(74.0, 57.0, -162.0), new Vector3(72.0, 57.0, -146.0), new Vector3(56.0, 64.0, -183.0), new Vector3(46.0, 63.0, -170.0), new Vector3(49.0, 65.0, -160.0), new Vector3(52.0, 66.0, -143.0), new Vector3(73.0, 63.0, -142.0), new Vector3(73.0, 65.0, -151.0) };
        for (int i = 0; i < data.goldSpawn.length; ++i) {
            data.goldSpawn[i].add(0.5, 0.5, 0.5);
        }
        this.murderMaps.put("Base", data);
        data = new MapData();
        data.name = "Headquarters";
        data.spawnPositions = new Vector3[] { new Vector3(11.0, 66.0, 23.0), new Vector3(15.0, 66.0, 23.0), new Vector3(18.0, 66.0, 23.0), new Vector3(-21.0, 66.0, 20.0), new Vector3(18.0, 66.0, 16.0), new Vector3(18.0, 66.0, 6.0), new Vector3(15.0, 66.0, -2.0), new Vector3(17.0, 66.0, -6.0), new Vector3(15.0, 66.0, -9.0), new Vector3(17.0, 66.0, -13.0), new Vector3(15.0, 66.0, -15.0), new Vector3(13.0, 66.0, -17.0), new Vector3(8.0, 66.0, -15.0), new Vector3(4.0, 66.0, -17.0), new Vector3(-1.0, 66.0, -15.0), new Vector3(-10.0, 66.0, -16.0) };
        data.goldSpawn = new Vector3[] { new Vector3(-9.0, 66.0, -12.0), new Vector3(-13.0, 66.0, -9.0), new Vector3(-14.0, 66.0, 0.0), new Vector3(-25.0, 66.0, 10.0), new Vector3(-14.0, 66.0, 19.0), new Vector3(-5.0, 66.0, 23.0), new Vector3(1.0, 66.0, 16.0), new Vector3(12.0, 66.0, 20.0), new Vector3(14.0, 76.0, -7.0), new Vector3(17.0, 76.0, 9.0), new Vector3(13.0, 76.0, 16.0), new Vector3(-19.0, 76.0, 11.0), new Vector3(-25.0, 76.0, -12.0), new Vector3(29.0, 66.0, 23.0) };
        for (int i = 0; i < data.goldSpawn.length; ++i) {
            data.goldSpawn[i].add(0.5, 0.5, 0.5);
        }
        this.murderMaps.put("Headquarters", data);
    }
    
    public void registerArena(final String name, final ArenaData data) {
        final Arena arena = new Arena(this, name, data);
        this.getServer().getPluginManager().registerEvents((Listener)arena, (Plugin)this);
        this.arenas.put(name, arena);
    }
    
    private void registerCommands() {
        this.getServer().getCommandMap().register("stats", (Command)new StatsCommand(this));
        this.getServer().getCommandMap().register("start", (Command)new StartCommand(this));
        this.getServer().getCommandMap().register("traitor", (Command)new RoleCommand(this));
        this.getServer().getCommandMap().register("vote", (Command)new VoteCommand(this));
    }
    
    private void initLanguage() {
        this.saveResource("English.yml");
        this.saveResource("Czech.yml");
        final Map<Integer, Config> langs = new HashMap<Integer, Config>();
        final Config cze = new Config(this.getDataFolder() + "/Czech.yml", 2);
        final Config eng = new Config(this.getDataFolder() + "/English.yml", 2);
        langs.put(1, cze);
        langs.put(0, eng);
        Language.init(langs);
    }
    
    public PlayerData getPlayerData(final Player p) {
        return this.players.get(p.getId());
    }
    
    @EventHandler
    public void onDataLoad(final PlayerLoadDataEvent e) {
        final Player p = e.getPlayer();
        p.getAdventureSettings().set(AdventureSettings.Type.WORLD_BUILDER, false);
        p.getAdventureSettings().set(AdventureSettings.Type.WORLD_IMMUTABLE, true);
        p.getAdventureSettings().set(AdventureSettings.Type.BUILD_AND_MINE, false);
        p.getAdventureSettings().update();
        new JoinQuery(p);
        this.bossBar.addPlayer(p);
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final Block b = e.getBlock();
        final PlayerInteractEvent.Action action = e.getAction();
        if (b.getLevel().getId() != this.getServer().getDefaultLevel().getId() || action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || !(b instanceof BlockSignPost)) {
            return;
        }
        e.setCancelled();
        final PlayerData data = this.getPlayerData(p);
        if (data.getArena() != null || this.getSpectator(p) != null) {
            return;
        }
        final Arena arena = this.getArenaBySign((Vector3)b);
        if (arena != null) {
            arena.onJoin(p, this.getPlayerData(p));
        }
    }
    
    private Arena getArenaBySign(final Vector3 pos) {
        for (final Arena arena : this.arenas.values()) {
            if (arena.data.sign.equals((Object)pos)) {
                return arena;
            }
        }
        return null;
    }
    
    public Arena getPlayerArena(final Player p) {
        return this.getPlayerData(p).getArena();
    }
    
    public Arena getSpectator(final Player p) {
        for (final Arena arena : this.arenas.values()) {
            if (arena.spectators.containsKey(p.getName().toLowerCase())) {
                return arena;
            }
        }
        return null;
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        final PlayerData data = this.players.remove(p.getId());
        if (data != null) {
            new QuitQuery(new PlayerData[] { data });
        }
        this.bossBar.removePlayer(p);
    }
    
    public static String getPrefix() {
        return TextFormat.GRAY + "[" + TextFormat.DARK_RED + "Kdo je vrah" + TextFormat.GRAY + "] " + TextFormat.WHITE;
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        this.players.put(p.getId(), new PlayerData(p, MTCore.getInstance().getPlayerData(p)));
    }
    
    public String getInfoAbout() {
        return null;
    }
    
    public String getMinigameName() {
        return "Murder";
    }
    
    @EventHandler
    public void onAchievement(final PlayerAchievementAwardedEvent e) {
        e.setCancelled();
    }
    
    @EventHandler
    public void onHit(final EntityDamageEvent e) {
        final Entity entity = e.getEntity();
        if (e.getCause() == EntityDamageEvent.DamageCause.VOID && entity instanceof Player && this.getPlayerArena((Player)entity) == null && this.getSpectator((Player)entity) == null) {
            entity.teleport(MTCore.getInstance().lobby);
        }
    }
    
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        e.setCancelled();
    }
    
    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        e.setCancelled();
    }
    
    public String getShortName() {
        return "mr";
    }
    
    public static Murder getInstance() {
        return Murder.instance;
    }
    
    public String getId() {
        return this.id;
    }
    
    public HashMap<String, Arena> getArenas() {
        return this.arenas;
    }
    
    static {
        Murder.DEBUG = false;
    }
}
