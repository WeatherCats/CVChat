package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
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
            sender.sendMessage(new TextComponent("§cToo few arguments."));
            sender.sendMessage(new TextComponent("§c/r <message...>"));
            return;
        }

        UUID recipientId = MsgCommand.getLastMessageReceived(sender.getUniqueId());
        if (recipientId == null) {
            sender.sendMessage(new TextComponent("§cNo message to reply to."));
            return;
        }

        ProxiedPlayer recipient = ProxyServer.getInstance().getPlayer(recipientId);
        boolean fakeNotFound = false;
        if(recipient == null || (Util.playerIsUnlisted(recipient) && recipient.hasPermission("cvchat.refusepm") && !sender.hasPermission("cvchat.showvanished") && !MsgCommand.disabledRefusal(recipient.getUniqueId()))) {
            sender.sendMessage(new TextComponent("§cPlayer left."));
            if(recipient == null) return;
            fakeNotFound = true;
        }

        if(SanctionManager.getInstance().isPlayerMuted(sender)) {
            if(!recipient.hasPermission("cvchat.mute.staff")) {
                sender.sendMessage(new TextComponent("§cYou are muted. You can only send private messages to staff members."));
                return;
            }
        }

        if(SanctionManager.getInstance().isAllChatMuted() && !sender.hasPermission("cvchat.muteallbypass")) {
            if(!recipient.hasPermission("cvchat.mute.staff")) {
                sender.sendMessage(new TextComponent("§cAll chat is muted. You can only send messages to staff members."));
                return;
            }
        }

        MsgCommand.sendMessage(sender, recipient, args, 0, fakeNotFound);
    }
}
