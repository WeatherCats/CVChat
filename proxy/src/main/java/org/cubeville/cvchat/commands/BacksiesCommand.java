package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import org.cubeville.cvchat.tickets.TicketManager;

public class BacksiesCommand extends CommandBase
{
    TicketManager ticketManager;
    
    public BacksiesCommand(TicketManager ticketManager) {
        super("backsies", "cvchat.ticket");
        this.ticketManager = ticketManager;
        // TODO: What about some permissions?
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length == 1) {
            try {
                int id = Integer.parseInt(args[0]);
                ticketManager.unclaimTicket(commandSender, id);
            }
            catch (NumberFormatException e) {
                commandSender.sendMessage("§cInvalid ticket id.");                
            }
        }
        else {
            commandSender.sendMessage("§cWrong number of arguments.");
            commandSender.sendMessage("§c/backsies <modreq-id>");
        }
    }
}
