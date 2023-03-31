package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

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
                String text = "";
                for(int i = 1; i < args.length; i++) {
                    if(i > 1) text += " ";
                    text += args[i];
                }
                if(commandSender.hasPermission("cvchat.ticket.color")) {
                    text = Util.translateAlternateColorCodes(text);
                }
                ticketManager.closeTicket(commandSender, id, text);
            }
            catch (NumberFormatException e) {
                commandSender.sendMessage("§cInvalid ticket id.");                
            }
        }
        else {
            commandSender.sendMessage("§cWrong number of arguments.");
            commandSender.sendMessage("§c/done <modreq-id> [mod-comment]");
        }
    }
}
