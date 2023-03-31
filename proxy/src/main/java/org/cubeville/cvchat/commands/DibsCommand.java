package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import org.cubeville.cvchat.tickets.TicketManager;

public class DibsCommand extends CommandBase
{
    TicketManager ticketManager;
    
    public DibsCommand(TicketManager ticketManager) {
        super("dibs", "cvchat.ticket");
        this.ticketManager = ticketManager;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length == 1) {
            try {
                int id = Integer.parseInt(args[0]);
                ticketManager.claimTicket(commandSender, id);
            }
            catch (NumberFormatException e) {
                commandSender.sendMessage("§cInvalid ticket id.");                
            }
        }
        else {
            commandSender.sendMessage("§cWrong number of arguments.");
            commandSender.sendMessage("§c/dibs <modreq-id>");
        }
    }
}
