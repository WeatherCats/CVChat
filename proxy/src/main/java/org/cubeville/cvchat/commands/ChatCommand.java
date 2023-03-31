package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvchat.Util;

import org.cubeville.cvchat.channels.Channel;

public class ChatCommand extends Command
{
    Channel channel;
    
    public ChatCommand(String command, Channel channel) {
        super(command);
        this.channel = channel;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(args.length == 0) return;
        String message = Util.removeSectionSigns(Util.joinStrings(args, 0));
        message = message.trim();
        if(message.length() > 0) {
            channel.sendMessage(commandSender, message.trim());
        }
    }
}
