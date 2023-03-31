package org.cubeville.cvchat.commands;

// TODO: This should obviously be integrated with RCommand
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.Util;
import org.cubeville.cvchat.sanctions.SanctionManager;

public class RlCommand extends CommandBase
{
    public RlCommand() {
        super("rl");
        setUsage("§c/rl <message...>");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(!verifyNotLessArguments(sender, args, 1)) return;

        UUID recipientId = MsgCommand.getLastMessageSent(sender.getUniqueId());
        if(!verify(sender, recipientId != null, "§cNo message sent yet.")) return;

        ProxiedPlayer recipient = ProxyServer.getInstance().getPlayer(recipientId);
        boolean fakeNotFound = false;
        if(recipient == null || (Util.playerIsUnlisted(recipient) && recipient.hasPermission("cvchat.refusepm") && !sender.hasPermission("cvchat.showvanished") && !MsgCommand.disabledRefusal(recipient.getUniqueId()))) {
            sender.sendMessage("§cPlayer left.");
            if(recipient == null) return;
            fakeNotFound = true;
        }

        if(SanctionManager.getInstance().isPlayerMuted(sender)) {
            if(!verify(sender, recipient.hasPermission("cvchat.mute.staff"), "§cYou are muted. You can only send messages to staff members.")) return;
        }

        if(SanctionManager.getInstance().isAllChatMuted() && !sender.hasPermission("cvchat.muteallbypass")) {
            if(!recipient.hasPermission("cvchat.mute.staff")) {
                sender.sendMessage("§cAll chat is muted. You can only send messages to staff members.");
                return;
            }
        }
        
        MsgCommand.sendMessage(sender, recipient, args, 0, fakeNotFound);
    }
}
