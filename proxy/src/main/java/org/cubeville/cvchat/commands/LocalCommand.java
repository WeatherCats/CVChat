package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.CommandSender;

import org.cubeville.cvchat.channels.LocalChannel;

public class LocalCommand extends CommandBase
{
    LocalChannel localChannel;
    
    public LocalCommand(LocalChannel localChannel) {
        super("local", "cvchat.monitor.local");
        setUsage("§c/local <on|off>");
        this.localChannel = localChannel;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(args.length != 1) {
            sender.sendMessage(new TextComponent("§c/local <on|off>"));
            return;
        }

        if(args[0].equals("on")) {
            localChannel.setLocalMonitorMute(sender.getUniqueId(), false);            
            sender.sendMessage(new TextComponent("§aLocal chat monitor activated."));
        }
        else if(args[0].equals("off")) {
            localChannel.setLocalMonitorMute(sender.getUniqueId(), true);
            sender.sendMessage(new TextComponent("§aLocal chat monitor deactivated."));
        }
        else {
            sender.sendMessage(new TextComponent("§c/local <on|off>"));
        }
    }

}
