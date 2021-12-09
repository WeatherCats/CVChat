package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cubeville.cvchat.Util;
import org.cubeville.cvchat.sanctions.SanctionManager;

public class MuteAllCommand extends CommandBase {

    public MuteAllCommand() {
        super("muteall", "cvchat.muteall");
        setUsage("§c/muteall [broadcast reason]");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(getSanctionManager().isAllChatMuted()) {
            sender.sendMessage("§cAll chat is already muted.");
            return;
        }

        getSanctionManager().muteAllChat();

        String reason = "";
        if(args.length > 0) reason = " Reason: " + Util.joinStrings(args, 0);
        sendMessage(getAllPlayers(), "[§cAnnouncement§f] §cAll chat has been muted." + reason);
    }

    private SanctionManager getSanctionManager() {
        return SanctionManager.getInstance();
    }
}