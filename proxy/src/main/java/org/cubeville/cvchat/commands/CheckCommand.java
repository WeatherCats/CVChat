package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import org.cubeville.cvchat.tickets.TicketManager;

import java.util.UUID;

public class CheckCommand extends CommandBase
{
    TicketManager ticketManager;
    
    public CheckCommand(TicketManager ticketManager) {
        super("check", "cvchat.ticket");
        this.ticketManager = ticketManager;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        int page = -1;
        boolean held = false;
        boolean closed = false;
        UUID playerId = null;
        UUID modId = null;
        int id = -1;
        
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("t:held")) {
                held = true;
            }
            else if(args[i].equals("t:closed")) {
                closed = true;
            }
            else if(args[i].startsWith("p:")) {
                try {
                    page = Integer.valueOf(args[i].substring(2));
                }
                catch(NumberFormatException e) {
                    commandSender.sendMessage("§6The p: parameter must be numeric.");
                    return;
                }
            } else if(args[i].startsWith("u:")) {
                playerId = getPDM().getPlayerId(args[i].substring(2));
                if (playerId == null) {
                    commandSender.sendMessage("§6The u: parameter must be used with their current name.");
                    return;
                }
            } else if(args[i].startsWith("m:")) {
                if(!commandSender.hasPermission("cvchat.ticket.search.mod")) {
                    commandSender.sendMessage("§cNo permission.");
                    return;
                }
                modId = getPDM().getPlayerId(args[i].substring(2));
                if(modId == null) {
                    commandSender.sendMessage("§6The m: parameter must be used with their current name.");
                    return;
                }
            }
            else {
                try {
                    id = Integer.valueOf(args[i]);
                }
                catch(NumberFormatException e) {
                    commandSender.sendMessage("§6Ticket # must be numeric.");
                    return;
                }
            }
        }

        if(held && closed) {
            commandSender.sendMessage("§6Only one type argument permitted.");
            return;
        }
        
        if(id >= 0 && (page >= 0 || held || closed || playerId != null || modId != null)) {
            commandSender.sendMessage("§6Ticket # can not be combined with other parameters.");
            return;
        }

        if(id >= 0) {
            ticketManager.showTicketDetail(commandSender, id);
            return;
        }

        if(page == -1) page = 1;
        ticketManager.checkTickets(commandSender, held, closed, playerId, modId, page);
    }
}
