package murder.arena.manager;

import cn.nukkit.*;
import org.apache.commons.io.*;
import java.io.*;

public class WorldManager
{
    public static void addWorld(final String name, final String id) {
        final File from = new File(Server.getInstance().getDataPath() + "/worlds/murder/" + name);
        final File to = new File(Server.getInstance().getDataPath() + "/worlds/" + name + "_" + id);
        try {
            FileUtils.copyDirectory(from, to);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void deleteWorld(final String name, final String id) {
        try {
            final File directory = new File(Server.getInstance().getDataPath() + "/worlds/" + name + "_" + id);
            FileUtils.deleteDirectory(directory);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void resetWorld(final String name, final String id) {
        deleteWorld(name, id);
        addWorld(name, id);
    }
}
