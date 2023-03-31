package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class TestCommand extends Command
{
    public TestCommand() {
        super("test", "cvchat.testcommand.supersecret");
    }

    public void execute(CommandSender commandSender, String[] args) {
        
        commandSender.sendMessage(new TextComponent("Sleeping for 10 seconds now."));
        try {Thread.sleep(10000); } catch(Exception ignored) {}
        commandSender.sendMessage(new TextComponent("Done sleeping."));
        
        // if (BungeeTabListPlus.isHidden(BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayer(sender))) {
        //     sender.sendMessage("You are hidden!");
        // }
        // else {
        //     sender.sendMessage("You are visible!");
        // }
    }

}
