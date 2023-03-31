package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.Util;
import org.cubeville.cvchat.sanctions.SanctionManager;

public class UnmuteAllCommand extends CommandBase {

    public UnmuteAllCommand() {
        super("unmuteall", "cvchat.unmuteall");
        setUsage("§c/unmuteall [broadcast reason]");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(!getSanctionManager().isAllChatMuted()) {
            sender.sendMessage(new TextComponent("§cAll chat is not muted."));
            return;
        }

        getSanctionManager().unmuteAllChat();

        String reason = "";
        if(args.length > 0) reason = " Reason: " + Util.joinStrings(args, 0);
        sendMessage(getAllPlayers(), "[§cAnnouncement§f] §cAll chat is no longer muted." + reason);
    }

    private SanctionManager getSanctionManager() {
        return SanctionManager.getInstance();
    }
}