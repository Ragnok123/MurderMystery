package murder.arena.object;

import cn.nukkit.utils.*;
import murder.arena.*;

public enum Role
{
    MURDERER(TextFormat.DARK_RED, "TRAITOR", 1, 300), 
    INNOCENT(TextFormat.GREEN, 0, 5), 
    DETECTIVE(TextFormat.DARK_BLUE, 0, 100);
    
    private String name;
    private TextFormat color;
    private int side;
    private int cost;
    
    private Role(final TextFormat color, final String name, final int side, final int cost) {
        this(color, side, cost);
        this.name = name;
    }
    
    private Role(final TextFormat color, final int side, final int cost) {
        this.name = null;
        this.color = color;
        this.side = side;
        this.cost = cost;
    }
    
    public String getName(final Arena.GameType gameType) {
        if (this.name != null && gameType == Arena.GameType.TTT) {
            return this.name;
        }
        return this.name();
    }
    
    public TextFormat getColor() {
        return this.color;
    }
    
    public int getSide() {
        return this.side;
    }
    
    public int getCost() {
        return this.cost;
    }
}
