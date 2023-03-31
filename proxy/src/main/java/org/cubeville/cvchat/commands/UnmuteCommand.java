package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.Util;
import org.cubeville.cvchat.sanctions.SanctionManager;

import java.util.UUID;

public class UnmuteCommand extends CommandBase
{
    public UnmuteCommand() {
        super("unmute");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(args.length != 1) {
            sender.sendMessage(new TextComponent("§cToo " + (args.length < 1 ? "few" : "many") + " arguments."));
            sender.sendMessage(new TextComponent("§c/unmute <player>"));
            return;
        }

        UUID pUUID = getPDM().getPlayerId(args[0]);
        if(pUUID == null) {
            sender.sendMessage(new TextComponent("§cNo player found!"));
            return;
        }

        if(!verify(sender, !pUUID.equals(sender.getUniqueId()), "§cYou can't unmute yourself.")) return;

        if(!verifyOutranks(sender, pUUID)) {
            sender.sendMessage(new TextComponent("§cNo permission."));
            return;
        }
        
        if(!getSanctionManager().isPlayerMuted(pUUID)) {
            sender.sendMessage(new TextComponent("§cPlayer is not muted."));
            return;
        }

        getSanctionManager().unmutePlayer(pUUID);

        if(ProxyServer.getInstance().getPlayer(pUUID) != null) ProxyServer.getInstance().getPlayer(pUUID).sendMessage(new TextComponent("§aYou have been unmuted."));

        for(ProxiedPlayer n: Util.getPlayersWithPermission("cvchat.mute.notify")) {
            n.sendMessage(new TextComponent("§a" + getPDM().getPlayerName(pUUID) + "§a has been unmuted by " + sender.getDisplayName() + "§a."));
        }

        sender.sendMessage(new TextComponent("§cPlease consider making a /note if you think this mute would justify one."));
    }

    private SanctionManager getSanctionManager() {
        return SanctionManager.getInstance();
    }

}
