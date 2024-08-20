package murder.arena.object;

import murder.arena.*;
import murder.arena.task.*;
import cn.nukkit.item.*;
import java.util.*;
import murder.stats.*;
import cn.nukkit.*;

public class ArenaPlayerData
{
    private final PlayerData playerData;
    private final Arena arena;
    private Role role;
    public long lastSword;
    public BowRechargeTask bowTask;
    public final ArrayList<Item> items;
    
    public ArenaPlayerData(final Arena arena, final PlayerData playerData) {
        this.lastSword = 0L;
        this.bowTask = null;
        this.items = new ArrayList<Item>(Arrays.asList(new ItemSwordWood(), new ItemSwordStone(), new ItemBow(), new ItemArrow(0, 32), new ItemArrow(0, 32)));
        this.arena = arena;
        this.playerData = playerData;
        Collections.shuffle(this.items, new Random());
    }
    
    public void addStat(final Stats.Stat stat) {
        this.addStat(stat, 1);
    }
    
    public void addRolePoints(final int points) {
        switch (this.role) {
            case INNOCENT: {
                this.addStat(Stats.Stat.INNOCENT_POINTS, points);
                break;
            }
            case MURDERER: {
                this.addStat(Stats.Stat.MURDER_POINTS, points);
                break;
            }
            case DETECTIVE: {
                this.addStat(Stats.Stat.DETECTIVE_POINTS, points);
                break;
            }
        }
    }
    
    public void addStat(final Stats.Stat stat, final int amount) {
        this.playerData.stats.add(stat, amount);
        this.playerData.getData().addMoney(stat.getTokens());
        this.playerData.getData().addExp(stat.getXp());
    }
    
    public Player getPlayer() {
        return this.getPlayerData().getPlayer();
    }
    
    public PlayerData getPlayerData() {
        return this.playerData;
    }
    
    public Arena getArena() {
        return this.arena;
    }
    
    public Role getRole() {
        return this.role;
    }
    
    public void setRole(final Role role) {
        this.role = role;
    }
}
