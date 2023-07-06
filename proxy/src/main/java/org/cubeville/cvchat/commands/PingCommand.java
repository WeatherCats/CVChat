package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.cubeville.cvplayerdata.playerdata.PlayerDataManager;

import java.util.UUID;

public class PingCommand extends Command {

    public PlayerDataManager pdm;

    public PingCommand(PlayerDataManager pdm) {
        super("ping");
        this.pdm = pdm;
    }

    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            boolean isPlayer = sender instanceof ProxiedPlayer;
            sender.sendMessage(new TextComponent(ChatColor.AQUA + "PONG! " + (isPlayer ? ((ProxiedPlayer) sender).getPing() + "ms" : "0ms duh, you're console")));
        } else if(args.length == 1 && sender.hasPermission("cvchat.ping.other")) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pdm.getPlayerByVisibleName(args[0]));
            if(player != null) {
                sender.sendMessage(new TextComponent(ChatColor.AQUA + args[0] + "'s ping is " + player.getPing() + "ms"));
            } else {
                sender.sendMessage(new TextComponent(ChatColor.RED + args[0] + " is not online!"));
            }
        } else {
            if(sender.hasPermission("cvchat.ping.other")) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid usage! Try /ping [player]"));
            } else {
                sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid usage! Try /ping"));
            }
        }
    }
}
