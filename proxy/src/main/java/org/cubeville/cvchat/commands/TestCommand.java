package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.cubeville.cvchat.CVChat;
import org.cubeville.cvipc.CVIPC;

import java.util.concurrent.TimeUnit;

public class TestCommand extends Command
{

    CVChat plugin;
    CVIPC cvipc;

    public TestCommand(CVChat plugin, CVIPC cvipc) {
        super("test", "cvchat.testcommand.supersecret");
        this.plugin = plugin;
        this.cvipc = cvipc;
    }

    public void execute(CommandSender commandSender, String[] args) {
        
        /*commandSender.sendMessage(new TextComponent("Sleeping for 10 seconds now."));
        try {Thread.sleep(10000); } catch(Exception ignored) {}
        commandSender.sendMessage(new TextComponent("Done sleeping."));*/
        
        // if (BungeeTabListPlus.isHidden(BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayer(sender))) {
        //     sender.sendMessage("You are hidden!");
        // }
        // else {
        //     sender.sendMessage("You are visible!");
        // }
        cvipc.sendMessage("oldserver", "cmd|console|menu createtemp Modreqs player:" + commandSender.getName() + " size:9");
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage("oldserver", "cmd|console|menu setitemtemp Modreqs player:" + commandSender.getName() + " slot:0" + " item:feather" + " itemname:testName" + " itemenchantment:durability");
        }, 1L, TimeUnit.MILLISECONDS);
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            cvipc.sendMessage("oldserver", "cmd|console|menu displaytemp Modreqs player:" + commandSender.getName());
        }, 2L, TimeUnit.MILLISECONDS);

    }
}
