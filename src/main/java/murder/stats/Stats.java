package murder.stats;

import java.util.*;

public class Stats
{
    private EnumMap<Stat, Integer> stats;
    private EnumMap<Stat, Integer> statsOriginal;
    
    public Stats() {
        this.stats = new EnumMap<Stat, Integer>(Stat.class);
        this.statsOriginal = new EnumMap<Stat, Integer>(Stat.class);
    }
    
    public void init(final Map<String, Object> data) {
        this.statsOriginal.put(Stat.WIN, Integer.valueOf(data.get("wins")));
        this.statsOriginal.put(Stat.KARMA, Integer.valueOf(data.get("karma")));
        this.statsOriginal.put(Stat.DETECTIVE_POINTS, Integer.valueOf(data.get("detective")));
        this.statsOriginal.put(Stat.MURDER_POINTS, Integer.valueOf(data.get("murder")));
        this.statsOriginal.put(Stat.INNOCENT_POINTS, Integer.valueOf(data.get("innocent")));
        this.stats.put(Stat.WIN, Integer.valueOf(0));
        this.stats.put(Stat.KARMA, Integer.valueOf(0));
        this.stats.put(Stat.DETECTIVE_POINTS, Integer.valueOf(0));
        this.stats.put(Stat.MURDER_POINTS, Integer.valueOf(0));
        this.stats.put(Stat.INNOCENT_POINTS, Integer.valueOf(0));
    }
    
    public void add(final Stat stat) {
        this.add(stat, 1);
    }
    
    public void add(final Stat stat, final int value) {
        if (this.stats.containsKey(stat)) {
            this.stats.put(stat, Integer.valueOf(this.stats.get(stat) + value));
        }
        if (stat.getKarma() != 0) {
            this.add(Stat.KARMA, stat.getKarma());
        }
    }
    
    public int get(final Stat stat) {
        return this.stats.get(stat) + this.statsOriginal.get(stat);
    }
    
    public int getDelta(final Stat stat) {
        return this.stats.get(stat);
    }
    
    public enum Stat
    {
        WIN(1000, 50, 0), 
        RIGHTKILL(50, 10, 20), 
        WRONGKILL(0, 0, -20), 
        KARMA(0, 0, 0), 
        DETECTIVE_POINTS(0, 0, 0), 
        MURDER_POINTS(0, 0, 0), 
        INNOCENT_POINTS(0, 0, 0);
        
        private final int xp;
        private final int tokens;
        private final int karma;
        
        private Stat(final int xp, final int tokens, final int karma) {
            this.xp = xp;
            this.tokens = tokens;
            this.karma = karma;
        }
        
        public int getXp() {
            return this.xp;
        }
        
        public int getTokens() {
            return this.tokens;
        }
        
        public int getKarma() {
            return this.karma;
        }
    }
}
