package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.Util;
import org.cubeville.cvchat.ranks.RankManager;

public class WhoCommand extends CommandBase
{
    public WhoCommand() {
        super("who", null, "list", "playerlist", "online", "players");
    }

    public void executeC(CommandSender sender, String[] args) {
        if(args.length > 1) {
            sender.sendMessage("§cToo many arguments.");
            sender.sendMessage("§c/who [filter]");
            return;
        }

        StringBuilder list = new StringBuilder();
        int cnt = 0;
        for(ProxiedPlayer player: ProxyServer.getInstance().getPlayers()) {
            if(args.length > 0 && !player.getDisplayName().toUpperCase().contains(args[0].toUpperCase())) continue;
            if(!Util.getPlayerVisibilityFor(sender, player)) continue;
            if(list.length() > 0) list.append("§r, ");
            list.append("§").append(RankManager.getInstance().getColor(player));
            if(Util.getPlayerUnlistedStatusFor(sender, player)) {
                list.append("§m");
            }
            else if(Util.getPlayerInvisibilityStatusFor(sender, player)) {
                list.append("§o");
            }
            list.append(player.getDisplayName());
            cnt++;
        }
        sender.sendMessage("§6Cubeville §a(" + cnt + ")§r: " + list);
    }
}
