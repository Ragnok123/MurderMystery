package murder.command;

import murder.*;
import cn.nukkit.command.*;
import cn.nukkit.*;
import murder.language.*;
import murder.arena.*;
import cn.nukkit.utils.*;
import murder.arena.object.*;
import GTCore.*;
import cn.nukkit.math.*;
import java.util.*;
import GTCore.Object.*;

public class RoleCommand extends BaseCommand
{
    public RoleCommand(final Murder plugin) {
        super("traitor", plugin);
        this.setAliases(new String[] { "vrah", "murder", "murderer", "detective", "detektiv", "nevinny", "innocent" });
        this.commandParameters.clear();
    }
    
    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player p = (Player)sender;
        if (args.length != 0) {
            return true;
        }
        final Arena arena = this.getPlugin().getPlayerArena(p);
        if (arena == null) {
            p.sendMessage(Murder.getPrefix() + Language.translate("not_ingame", p, new String[0]));
            return true;
        }
        if ((arena.gameType == Arena.GameType.MURDER && arena.phase == Arena.GamePhase.GAME) || (arena.gameType == Arena.GameType.TTT && arena.task.gameTime >= 30)) {
            p.sendMessage(Murder.getPrefix() + Language.translate("already_started", p, new String[0]));
            return true;
        }
        final ArenaPlayerData data = arena.getPlayerData(p);
        Role role = null;
        final String lowerCase = label.toLowerCase();
        switch (lowerCase) {
            case "traitor":
            case "murder":
            case "murderer":
            case "vrah": {
                role = Role.MURDERER;
                break;
            }
            case "innocent":
            case "nevinny": {
                role = Role.INNOCENT;
                break;
            }
            case "detective":
            case "detektiv": {
                role = Role.DETECTIVE;
                break;
            }
        }
        if (role == null) {
            p.sendMessage(TextFormat.RED + "Tato role neexistuje");
            return true;
        }
        if (!p.isOp()) {
            final int playersCount = arena.players.size();
            final int murderCount = (arena.gameType == Arena.GameType.TTT) ? ((playersCount <= 12) ? 2 : ((playersCount <= 16) ? 3 : ((playersCount <= 20) ? 4 : 5))) : 1;
            final int detectiveCount = (arena.gameType == Arena.GameType.TTT) ? ((playersCount <= 12) ? 1 : ((playersCount <= 18) ? 2 : 3)) : 1;
            final int innocentCount = playersCount - murderCount - detectiveCount;
            int realCount = 0;
            for (final ArenaPlayerData playerData : arena.playersData.values()) {
                if (playerData.getRole() == role) {
                    ++realCount;
                }
            }
            boolean canSelect = true;
            switch (role) {
                case INNOCENT: {
                    if (realCount >= innocentCount) {
                        canSelect = false;
                        break;
                    }
                    break;
                }
                case MURDERER: {
                    if (realCount >= murderCount) {
                        canSelect = false;
                        break;
                    }
                    break;
                }
                case DETECTIVE: {
                    if (realCount >= detectiveCount) {
                        canSelect = false;
                        break;
                    }
                    break;
                }
            }
            if (!canSelect) {
                p.sendMessage(Language.translate("role_full", p, new String[0]));
                return true;
            }
            if (!p.isOp() && !p.hasPermission("gameteam.mcpe")) {
                final PlayerData data2 = MTCore.getInstance().getPlayerData(p);
                final int cost = p.hasPermission("gameteam.vip") ? NukkitMath.floorDouble(role.getCost() * 0.5) : role.getCost();
                if (data2.getMoney() < cost) {
                    p.sendMessage(Language.translate("role_money", p, "" + cost));
                    return true;
                }
                data2.addMoney(-cost);
            }
        }
        data.setRole(role);
        p.sendMessage(Language.translate("role_select_command", p, role.getColor() + role.getName(arena.gameType)));
        return true;
    }
}
