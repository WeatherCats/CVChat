package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.Util;

import org.cubeville.cvchat.sanctions.SanctionManager;

import java.util.UUID;

public class MuteCommand extends CommandBase
{
    public MuteCommand() {
        super("mute", "cvchat.mute");
        setUsage("§c/mute <player> [reason]");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(!verifyNotLessArguments(sender, args, 1)) return;

        UUID pUUID = getPDM().getPlayerId(args[0]);
        if(pUUID == null) {
            sender.sendMessage("§cNo player found!");
            return;
        }

        if(!verify(sender, !pUUID.equals(sender.getUniqueId()), "§cYou can't mute yourself, silly!")) return;

        if(!verifyOutranks(sender, pUUID)) {
            if(ProxyServer.getInstance().getPlayer(pUUID) != null) ProxyServer.getInstance().getPlayer(pUUID).sendMessage(sender.getDisplayName() + "§c tried to mute you. Should we instaban that person, your majesty?");
            return;
        }

        if(!verify(sender, !getSanctionManager().isPlayerMuted(pUUID), "§cPlayer is already muted.")) return;

        getSanctionManager().mutePlayer(pUUID);

        String reason = "";
        if(args.length > 1) reason = " Reason: " + Util.joinStrings(args, 1);
        if(ProxyServer.getInstance().getPlayer(pUUID) != null) ProxyServer.getInstance().getPlayer(pUUID).sendMessage("§cYou have been muted." + reason);
        String msg = "§a" + getPDM().getPlayerName(pUUID) + "§a has been muted by " + sender.getDisplayName() + "§a." + reason;
        sendMessage(getAllPlayersWithPermission("cvchat.mute.notify"), msg);
    }

    private SanctionManager getSanctionManager() {
        return SanctionManager.getInstance();
    }
}
