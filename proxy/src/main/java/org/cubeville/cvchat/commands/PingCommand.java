package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping");
    }

    public void execute(CommandSender sender, String[] args) {
        boolean isPlayer = sender instanceof ProxiedPlayer;
        sender.sendMessage(new TextComponent(ChatColor.AQUA + "PONG! " + (isPlayer ? ((ProxiedPlayer) sender).getPing() + "ms" : "0ms duh, you're console")));
    }
}
