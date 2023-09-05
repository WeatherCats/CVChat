package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.cubeville.cvchat.Util;

public class GTrCommand extends CommandBase
{
    public GTrCommand() {
        super("gtr", "cvchat.gtr");
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length < 2) {
            commandSender.sendMessage(new TextComponent("§c/gtr <permission> <message>"));
            return;
        }
        
        String permission = args[0];
        
        String message = joinStrings(args, 1);
        message = Util.translateAlternateColorCodes(message);
        TextComponent out = new TextComponent();
        while(message.contains("hoverbase[") && message.contains("hovertext[")) {
            out.addExtra(new TextComponent(message.substring(0, message.indexOf("hoverbase["))));
            message = message.substring(message.indexOf("hoverbase["));
            TextComponent hover = new TextComponent(message.substring(message.indexOf("[") + 1, message.indexOf("]")));
            message = message.substring(message.indexOf("]") + 1);
            hover.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(message.substring(message.indexOf("[") + 1, message.indexOf("]")))));
            out.addExtra(hover);
            if(message.length() + 1 > message.indexOf("]")) {
                message = message.substring(message.indexOf("]") + 1);
            } else {
                message = "";
            }
        }
        if(message.length() > 0) out.addExtra(message);
        //sendMessage(getAllPlayersWithPermission(permission), message);
        sendMessage(getAllPlayersWithPermission(permission), out);
    }
}
