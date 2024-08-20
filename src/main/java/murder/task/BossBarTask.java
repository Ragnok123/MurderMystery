package murder.task;

import cn.nukkit.scheduler.*;
import murder.*;
import cn.nukkit.utils.*;

public class BossBarTask extends Task
{
    private String word;
    private String wordClean;
    private int currentIndex;
    private Murder plugin;
    
    public BossBarTask(final Murder plugin, final String word) {
        this.currentIndex = 0;
        this.plugin = plugin;
        this.word = word;
        this.wordClean = TextFormat.clean(this.word);
    }
    
    public void onRun(final int i) {
        if (this.currentIndex < this.wordClean.length()) {
            while (this.wordClean.charAt(this.currentIndex) == ' ' && this.currentIndex + 1 < this.wordClean.length()) {
                ++this.currentIndex;
            }
            final String s = TextFormat.YELLOW + this.wordClean.substring(0, this.currentIndex) + TextFormat.BOLD + TextFormat.GOLD + this.wordClean.substring(this.currentIndex, this.currentIndex + 1) + TextFormat.RESET + TextFormat.YELLOW + this.wordClean.substring(this.currentIndex + 1, this.wordClean.length());
            this.plugin.bossBar.updateText(s);
            this.plugin.bossBar.updateInfo();
            this.plugin.bossBar.update();
        }
        else if (this.currentIndex == this.wordClean.length()) {
            this.plugin.bossBar.updateText(this.word);
            this.plugin.bossBar.updateInfo();
        }
        if (this.currentIndex - this.wordClean.length() >= 50) {
            this.currentIndex = 0;
        }
        else {
            ++this.currentIndex;
        }
    }
}
