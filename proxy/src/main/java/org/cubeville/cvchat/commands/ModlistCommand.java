package org.cubeville.cvchat.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvchat.Util;
import org.cubeville.cvchat.sanctions.SanctionManager;
import org.cubeville.cvchat.ranks.RankManager;

public class ModlistCommand extends CommandBase
{
    public ModlistCommand() {
        super("modlist", null);
    }

    public void executeC(CommandSender sender, String[] args) {
        if(args.length > 1) {
            sender.sendMessage("§cToo many arguments.");
            sender.sendMessage("/modlist [filter]");
            return;
        }

        String list = "";
        int cnt = 0;
        for(ProxiedPlayer player: ProxyServer.getInstance().getPlayers()) {
            if(RankManager.getInstance().getPriority(player) < 20) continue;
            if(args.length > 0 && player.getName().toUpperCase().indexOf(args[0].toUpperCase()) == -1) continue;
            if(!Util.getPlayerVisibilityFor(sender, player)) continue;
            if(list.length() > 0) list += "§r, ";
            list += "§" + RankManager.getInstance().getColor(player);
            if(Util.getPlayerUnlistedStatusFor(sender, player)) {
                list += "§m";
            }
            else if(Util.getPlayerInvisibilityStatusFor(sender, player)) {
                list += "§o";
            }
            list += player.getName();
            cnt++;
        }
        sender.sendMessage("§6Cubeville §a(" + cnt + ")§r: " + list);
    }
}
