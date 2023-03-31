package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvvanish.CVVanish;


public class SetAliasCommand extends CommandBase
{
    public SetAliasCommand() {
        super("setalias", "cvchat.setalias");
    }

    public void executeC(CommandSender sender, String[] args) {
        if(args.length > 3 || args.length < 2 || (args.length == 3 && !args[2].equalsIgnoreCase("-notab"))) {
            sender.sendMessage("§c/setalias <player> [<alias>|none] [-notab]");
            return;
        }

        String playerName = args[0];
        String alias = args[1];
        boolean reloadTab = args.length != 3 || !args[2].equalsIgnoreCase("-notab");
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
            if(reloadTab) {
                CVVanish.getInstance().getTeamHandler().refreshEntireTab();
            }
        }
        
        sender.sendMessage("§aAlias changed.");
    }
}
