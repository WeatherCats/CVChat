package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;


public class SetAliasCommand extends CommandBase
{
    public SetAliasCommand() {
        super("setalias", "cvchat.setalias");
    }

    public void executeC(CommandSender sender, String[] args) {
        if(args.length != 2) {
            sender.sendMessage("§c/setalias <player> [<alias>|none]");
            return;
        }

        String playerName = args[0];
        String alias = args[1];
        if(alias.equals("none")) alias = null;
        
        UUID playerId = getPDM().getPlayerId(playerName);
        if(playerId == null) {
            sender.sendMessage("§cPlayer not found!");
            return;
        }

        getPDM().setPlayerDisplayName(playerId, alias);

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
        if(player != null) {
            player.setDisplayName(alias == null ? player.getName() : alias);
        }
        
        sender.sendMessage("§aAlias changed.");
    }
}
