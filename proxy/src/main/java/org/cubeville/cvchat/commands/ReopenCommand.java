package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.tickets.TicketManager;

public class ReopenCommand extends CommandBase
{
    TicketManager ticketManager;
    
    public ReopenCommand(TicketManager ticketManager) {
        super("reopen", "cvchat.ticket");
        this.ticketManager = ticketManager;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length == 1) {
            try {
                int id = Integer.parseInt(args[0]);
                ticketManager.reopenTicket(commandSender, id);
            }
            catch (NumberFormatException e) {
                commandSender.sendMessage(new TextComponent("§cInvalid ticket id."));
            }
        }
        else {
            commandSender.sendMessage(new TextComponent("§cWrong number of arguments."));
            commandSender.sendMessage(new TextComponent("§c/reopen <modreq-id>"));
        }
    }
}
