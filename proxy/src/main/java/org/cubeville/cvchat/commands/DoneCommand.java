package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.Util;
import org.cubeville.cvchat.tickets.TicketManager;

public class DoneCommand extends CommandBase
{
    TicketManager ticketManager;
    
    public DoneCommand(TicketManager ticketManager) {
        super("done", "cvchat.ticket");
        this.ticketManager = ticketManager;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length != 0) {
            try {
                int id = Integer.parseInt(args[0]);
                StringBuilder text = new StringBuilder();
                for(int i = 1; i < args.length; i++) {
                    if(i > 1) text.append(" ");
                    text.append(args[i]);
                }
                if(commandSender.hasPermission("cvchat.ticket.color")) {
                    text = new StringBuilder(Util.translateAlternateColorCodes(text.toString()));
                }
                ticketManager.closeTicket(commandSender, id, text.toString());
            }
            catch (NumberFormatException e) {
                commandSender.sendMessage(new TextComponent("§cInvalid ticket id."));
            }
        }
        else {
            commandSender.sendMessage(new TextComponent("§cWrong number of arguments."));
            commandSender.sendMessage(new TextComponent("§c/done <modreq-id> [mod-comment]"));
        }
    }
}
