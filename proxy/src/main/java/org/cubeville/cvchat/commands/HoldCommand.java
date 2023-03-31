package org.cubeville.cvchat.commands;

import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.chat.TextComponent;
import org.cubeville.cvchat.tickets.TicketManager;

public class HoldCommand extends CommandBase
{
    TicketManager ticketManager;

    public HoldCommand(TicketManager ticketManager) {
        super("hold", "cvchat.ticket");
        this.ticketManager = ticketManager;
    }

    public void executeC(CommandSender commandSender, String[] args) {
        if(args.length != 1) {
            commandSender.sendMessage(new TextComponent("§cWrong number of arguments."));
            commandSender.sendMessage(new TextComponent("§c/hold <modreq-id>"));
            return;
        }

        try {
            int id = Integer.parseInt(args[0]);
            ticketManager.holdTicket(commandSender, id);
        }
        catch (NumberFormatException e) {
            commandSender.sendMessage(new TextComponent("§cInvalid ticket id."));
        }
    }
}
