package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.CVChat;

public class ReloadWhitelistCommand extends CommandBase
{
    CVChat plugin;

    public ReloadWhitelistCommand(CVChat plugin) {
        super("reloadwhitelist", "cvchat.reloadwhitelist");
        this.plugin = plugin;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length > 0) {
            commandSender.sendMessage(new TextComponent("§cCommand has no arguments."));
            return;
        }

        plugin.updateCommandWhitelist();
        commandSender.sendMessage(new TextComponent("§aWhitelist updated."));
    }
}

