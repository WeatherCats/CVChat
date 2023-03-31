package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.tickets.TicketManager;

public class UnholdCommand extends CommandBase
{
    TicketManager ticketManager;
    
    public UnholdCommand(TicketManager ticketManager) {
      super("unhold", "cvchat.ticket");
      this.ticketManager = ticketManager;
    }
    
    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length != 1) {
            commandSender.sendMessage(new TextComponent("§cWrong number of arguments."));
            commandSender.sendMessage(new TextComponent("§c/unhold <modreq-id>"));
            return;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            ticketManager.unholdTicket(commandSender, id);
        }
        catch (NumberFormatException e) {
          commandSender.sendMessage(new TextComponent("§cInvalid ticket id."));
        }
    }
}
