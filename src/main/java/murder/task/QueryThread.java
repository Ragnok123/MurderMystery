package murder.task;

import cn.nukkit.*;
import java.util.*;

public class QueryThread extends Thread implements InterruptibleThread
{
    private HashMap<String, String> data;
    
    public QueryThread() {
        this.data = new HashMap<String, String>();
    }
    
    @Override
    public void run() {
    }
}
