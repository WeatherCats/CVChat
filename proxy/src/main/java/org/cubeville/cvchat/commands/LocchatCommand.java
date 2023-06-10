package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.ChatListener;

public class LocchatCommand extends CommandBase
{
    public ChatListener chatListener;

    public LocchatCommand(ChatListener chatListener) {
        super("locchat", "cvchat.locchat");
        this.chatListener = chatListener;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length > 0) {
            commandSender.sendMessage(new TextComponent(ChatColor.RED + "Incorrect usage! Use /locchat to toggle locchat on and off"));
            return;
        }
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if(chatListener.isLocChatEnabled(player.getUniqueId())) {
            chatListener.removeLocChatEnabled(player.getUniqueId());
            player.sendMessage(new TextComponent(ChatColor.GOLD + player.getName() + "'s local chat will operate as normal"));
        } else {
            chatListener.addLocChatEnabled(player.getUniqueId());
            player.sendMessage(new TextComponent(ChatColor.GOLD + player.getName() + "'s local chat will be sent to the server"));
        }
    }
}
