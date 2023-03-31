package org.cubeville.cvchat.commands;

import java.util.UUID;

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
        loginListener.addVersionCheckBypass(UUID.fromString(args[0]));
        commandSender.sendMessage(new TextComponent("Added."));
    }
}
