package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvchat.Util;

import org.cubeville.cvchat.sanctions.SanctionManager;

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

        if(!verifyOnline(sender, args[0])) return;
        ProxiedPlayer player = getPlayer(args[0]);

        if(!verify(sender, !player.getUniqueId().equals(sender.getUniqueId()), "§cYou can't mute yourself, silly!")) return;
        
        if(!verifyOutranks(sender, player)) {
            player.sendMessage(sender.getDisplayName() + "§c tried to mute you. Should we instaban that person, your majesty?");
            return;
        }

        if(!verify(sender, !getSanctionManager().isPlayerMuted(player), "§cPlayer is already muted.")) return;

        getSanctionManager().mutePlayer(player);

        String reason = "";
        if(args.length > 1) reason = " Reason: " + Util.joinStrings(args, 1);
        player.sendMessage("§cYou have been muted." + reason);
        String msg = "§a" + player.getDisplayName() + "§a has been muted by " + sender.getDisplayName() + "§a." + reason;
        sendMessage(getAllPlayersWithPermission("cvchat.mute.notify"), msg);
    }

    private SanctionManager getSanctionManager() {
        return SanctionManager.getInstance();
    }
}
