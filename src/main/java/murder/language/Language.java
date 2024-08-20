package murder.language;

import cn.nukkit.utils.*;
import cn.nukkit.*;
import GTCore.Object.*;
import java.util.*;
import GTCore.*;

public class Language
{
    private static Map<Integer, Map<String, String>> data;
    
    public static void init(final Map<Integer, Config> data) {
        for (final Map.Entry<Integer, Config> entry : data.entrySet()) {
            Language.data.put(entry.getKey(), entry.getValue().getAll());
        }
    }
    
    public static String translate(final String message, final Player p, final String... args) {
        return translate(message, MTCore.getInstance().getPlayerData(p), args);
    }
    
    public static String translate(final String message, final PlayerData data, final String... args) {
        return translate(message, data.getLanguage(), args);
    }
    
    public static String translate(final String message, final int language, final String... args) {
        String base = Language.data.get(language).get(message).replaceAll("&", "§");
        if (base == null) {
            return message;
        }
        for (int i = 0; i < args.length; ++i) {
            base = base.replaceAll("%" + i, args[i]);
        }
        return base;
    }
    
    public static HashMap<Integer, String> getTranslations(final String msg, final String... args) {
        final HashMap<Integer, String> translations = new HashMap<Integer, String>();
        for (final int i : Lang.getLanguages()) {
            translations.put(i, translate(msg, i, args));
        }
        return translations;
    }
    
    static {
        Language.data = new HashMap<Integer, Map<String, String>>();
    }
}
