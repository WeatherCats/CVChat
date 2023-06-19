package org.cubeville.cvchat.commands;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.LoginListener;

import net.md_5.bungee.api.CommandSender;

public class VcbAddCommand extends CommandBase
{
    LoginListener loginListener;
    
    public VcbAddCommand(LoginListener loginListener) {
        super("vcbadd", "cvchat.vcbadd");
        this.loginListener = loginListener;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length != 1) {
            commandSender.sendMessage(new TextComponent(ChatColor.RED + "Syntax is /vcbadd <uuid>"));
            return;
        }
        try {
            loginListener.addVersionCheckBypass(UUID.fromString(args[0]));
        } catch (IllegalArgumentException e) {
            commandSender.sendMessage(new TextComponent(ChatColor.RED + args[0] + " is invalid uuid!"));
            return;
        }
        commandSender.sendMessage(new TextComponent(ChatColor.GREEN + args[0] + " is added to version bypass"));
    }
}
