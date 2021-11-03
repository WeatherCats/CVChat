package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvchat.Util;
import org.cubeville.cvchat.sanctions.SanctionManager;

public class RCommand extends Command
{
    public RCommand() {
        super("r");
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(args.length < 1) {
            sender.sendMessage("§cToo few arguments.");
            sender.sendMessage("§c/r <message...>");
            return;
        }

        UUID recipientId = MsgCommand.getLastMessageReceived(sender.getUniqueId());
        if (recipientId == null) {
            sender.sendMessage("§cNo message to reply to.");
            return;
        }

        ProxiedPlayer recipient = ProxyServer.getInstance().getPlayer(recipientId);
        boolean fakeNotFound = false;
        if(recipient == null || (Util.playerIsUnlisted(recipient) == true && recipient.hasPermission("cvchat.refusepm") == true && sender.hasPermission("cvchat.showvanished") == false && MsgCommand.disabledRefusal(recipient.getUniqueId()) == false)) {
            sender.sendMessage("§cPlayer left.");
            if(recipient == null) return;
            fakeNotFound = true;
        }

        if(SanctionManager.getInstance().isPlayerMuted(sender)) {
            if(!recipient.hasPermission("cvchat.mute.staff")) {
                sender.sendMessage("§cYou are muted. You can only send private messages to staff members.");
                return;
            }
        }

        MsgCommand.sendMessage(sender, recipient, args, 0, fakeNotFound);
    }
}
