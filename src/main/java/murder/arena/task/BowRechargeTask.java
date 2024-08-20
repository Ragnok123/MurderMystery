package murder.arena.task;

import cn.nukkit.scheduler.*;
import cn.nukkit.*;
import cn.nukkit.item.*;
import java.util.*;

public class BowRechargeTask extends Task
{
    private int durability;
    private final Player player;
    private final int rechargeTime;
    private final int countStep;
    
    public BowRechargeTask(final Player p, final int time) {
        this.durability = 385;
        this.player = p;
        this.rechargeTime = time * 20;
        this.countStep = 385 / this.rechargeTime;
    }
    
    public void onRun(final int currentTick) {
        boolean found = false;
        for (final Map.Entry<Integer, Item> entry : new HashSet<Map.Entry<Integer, Item>>(this.player.getInventory().getContents().entrySet())) {
            final Integer slot = entry.getKey();
            final Item item = entry.getValue();
            if (slot != null && slot >= 0) {
                if (item.getId() != 261) {
                    continue;
                }
                found = true;
                this.durability -= this.countStep;
                if (this.durability < 0) {
                    this.durability = 0;
                }
                item.setDamage(this.durability);
                this.player.getInventory().setItem((int)slot, item);
                if (this.durability > 0) {
                    continue;
                }
                this.cancel();
            }
        }
        if (!found) {
            this.cancel();
        }
    }
}
