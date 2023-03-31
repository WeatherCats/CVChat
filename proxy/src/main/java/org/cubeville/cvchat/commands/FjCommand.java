package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class FjCommand extends CommandBase
{
    public FjCommand() {
        super("fj", "cvchat.fakejoinquit");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        String playerName = "Console";
        if(commandSender instanceof ProxiedPlayer) playerName = ((ProxiedPlayer) commandSender).getDisplayName();

        sendMessage(getAllPlayers(), "§e" + playerName + "§e joined the game.");
    }
}
