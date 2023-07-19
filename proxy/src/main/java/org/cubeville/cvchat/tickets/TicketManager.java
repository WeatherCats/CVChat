package org.cubeville.cvchat.tickets;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvipc.IPCInterface;

import org.cubeville.cvchat.CVChat;
import org.cubeville.cvchat.Util;

public class TicketManager implements IPCInterface
{
    private CVIPC ipc;
    private CVChat plugin;
    
    private List<Ticket> tickets;
    TicketDao dao;

    private Set<String> openTicketPlayerList;
    
    public TicketManager(CVChat plugin, CVIPC ipc, TicketDao dao) {
        this.ipc = ipc;
        this.plugin = plugin;
        ipc.registerInterface("modreq", this);
        this.dao = dao;
        tickets = dao.loadTickets();
        updateOpenTicketPlayerList();
    }
    
    public void process(String serverName, String channel, String message) {
        StringTokenizer tk = new StringTokenizer(message, "|");
        if(tk.countTokens() != 3) return;
        UUID playerId = UUID.fromString(tk.nextToken());
        String location = tk.nextToken();
        String text = tk.nextToken();
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
        if(player == null) return;

        if(!player.hasPermission("cvchat.ticket.multiple") && playerHasTicket(player.getUniqueId())) {
            player.sendMessage(new TextComponent("§6You already have an open mod request. Please wait for it to be completed."));
            return;
        }

        try {
            StringTokenizer loctk = new StringTokenizer(location, ",");
            Ticket ticket = new Ticket(null, playerId, player.getName(), text, serverName,
                                       loctk.nextToken(), Integer.parseInt(loctk.nextToken()), Integer.parseInt(loctk.nextToken()), Integer.parseInt(loctk.nextToken()),
                                       System.currentTimeMillis());
            tickets.add(ticket);
            int ticketId = dao.createTicket(ticket);
            player.sendMessage(new TextComponent("§6Thank you. Your message has been sent. A moderator should be with you shortly."));
            TextComponent notify1 = new TextComponent("New mod request #" + ticketId + " filed; use ");
            notify1.setColor(ChatColor.GOLD);
            TextComponent notify2 = new TextComponent("/");
            notify2.setColor(ChatColor.AQUA);
            notify2.addExtra(ChatColor.GREEN + "check");
            notify2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/check " + ticketId));
            notify2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Check modreq #" + ticketId)));
            TextComponent notify3 = new TextComponent(" for more.");
            notify3.setColor(ChatColor.GOLD);
            notify1.addExtra(notify2);
            notify1.addExtra(notify3);
            sendNotification(notify1);
            //sendNotification("§6New mod request #" + ticketId + " filed; use /check for more.");
            updateOpenTicketPlayerList();
        }
        catch(RuntimeException e) {
            player.sendMessage(new TextComponent("§cTicket creation failed. Please contact an administrator."));
        }
    }

    public void playerLogin(ProxiedPlayer player) {
        UUID playerId = player.getUniqueId();
        for(Ticket ticket: tickets) {
            if(ticket.isClosed() && !ticket.playerNotified() && ticket.getPlayer().equals(playerId)) {
                ticket.setPlayerNotified(true);
                updateTicketAsync(ticket);
                final String moderatorName = ticket.getModeratorName();
                final String ticketText = ticket.getText();
                final String moderatorText = ticket.getModeratorText();
                final long ticketId = ticket.getId();
                ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                    sendPlayerNotification(playerId, "§6" + moderatorName + "§6 has completed your request while you were offline:");
                    sendPlayerNotification(playerId, "§6Request - §7" + ticketText);
                    sendPlayerNotification(playerId, "§6Mod comment - §7" + moderatorText);
                }, 5, TimeUnit.SECONDS );
            }
        }
    }

    private boolean playerHasTicket(UUID playerId) {
        for(Ticket ticket: tickets) {
            if(!ticket.isClosed() && !ticket.isHeld() && ticket.getPlayer().equals(playerId)) {
                return true;
            }
        }
        return false;
        
    }
    
    private void sendNotification(String text) {
        for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
            if(p.hasPermission("cvchat.ticket.notify")) {
                p.sendMessage(TextComponent.fromLegacyText(text));
            }
        }
    }

    private void sendNotification(TextComponent text) {
        for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if(p.hasPermission("cvchat.ticket.notify")) {
                p.sendMessage(text);
            }
        }
    }

    private boolean sendPlayerNotification(UUID playerId, String text) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
        if(player != null) {
            player.sendMessage(TextComponent.fromLegacyText(text));
            return true;
        }
        return false;
    }

    public String getDateStr(long timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("MMM.dd@HH:mm:ss");
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(timestamp);
        return df.format(c.getTime());
    }

    public int getNumberOfOpenTickets() {
        int cnt = 0;
        for(Ticket ticket: tickets) {
            if(!ticket.isClosed() && !ticket.isHeld()) {
                cnt++;
            }
        }
        return cnt;
    }

    private boolean showPlayerAsOnline(UUID playerId) {
	if(ProxyServer.getInstance().getPlayer(playerId) == null) return false;
	return !Util.playerIsUnlisted(playerId);
    }

    public List<Ticket> getTicketsByPlayer(UUID pUUID) {
        List<Ticket> playerTickets = new ArrayList<>();
        for(Ticket ticket : tickets) {
            if(ticket.getPlayer().equals(pUUID)) playerTickets.add(ticket);
        }
        return playerTickets;
    }
    
    public void checkTickets(CommandSender sender, boolean held, boolean closed, UUID playerId, UUID modId, int page) {
        // TODO: Need to async this?
        int cnt = -1;
        int pageSize = 5;
        List<TextComponent> out = new ArrayList<>();
        for(Ticket ticket: tickets) {
            if(ticket.isClosed() == closed && ticket.isHeld() == held && (playerId == null || ticket.getPlayer().equals(playerId)) && (modId == null || ((ticket.isClaimed() || ticket.isClosed()) && ticket.getModerator().equals(modId)))) {
                cnt++;
                if(cnt / pageSize + 1 == page) {
                    String text;
                    if(ticket.isClaimed()) {
                        text = "§dClaimed by " + ticket.getModeratorName();
                    }
                    else {
                        text = "§7" + ticket.getText();
                    }
                    if(text.length() > 22) { text = text.substring(0, 22) + "§7..."; }
 		    boolean playerOnline = showPlayerAsOnline(ticket.getPlayer());
                    int id = ticket.getId();
                    TextComponent t = new TextComponent("§6#" + id + ". " + getDateStr(ticket.getCreationTimestamp()) + " by §" + (playerOnline ? "a" : "c") + ticket.getPlayerName() + " §7- " + text);
                    t.addExtra("  ");
                    TextComponent c = new TextComponent("§b[§aCheck§b]");
                    c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/check " + id));
                    c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Check modreq #" + id)));
                    t.addExtra(c);
                    out.add(t);
                }
            }
        }
        if(cnt == -1) {
            String msg = "No open open modreqs. Also there are no closed open and no open closed modreqs.";
            if(held) msg = "No held modreqs.";
            if(closed) msg = "No closed modreqs.";
            sender.sendMessage(new TextComponent(msg));
            return;
        }
        int from = page * 5 - 4;
        int to = Math.min(page * 5, cnt + 1);
        sender.sendMessage(new TextComponent("--------- " + ++cnt + " " + (held ? "held" : (closed ? "closed" : "open")) + " modreqs, showing " + from + "-" + to + " ---------"));
        for(TextComponent s: out) {
            sender.sendMessage(s);
        }
    }

    public void showTicketDetail(CommandSender sender, int id) {
        Ticket ticket = getTicketById(id);
        if(ticket == null) {
            sender.sendMessage(new TextComponent("§cInvalid ticket id."));
            return;
        }

        {
            String hl = "§b--------- Mod Request #" + id + "- ";
            if(ticket.isClosed()) hl += "§aClosed";
            else if(ticket.isClaimed()) hl += "§cClaimed";
            else if(ticket.isHeld()) hl += "§dOn Hold";
            else hl += "§eOpen";
            hl += "§b ---------";
            sender.sendMessage(new TextComponent(hl));
        }

        String p = ticket.getPlayerName();
	boolean playerOnline = showPlayerAsOnline(ticket.getPlayer());
        TextComponent t = new TextComponent("§eFiled by " + (playerOnline ? "§a" : "§c") + p + " §eat " + getDateStr(ticket.getCreationTimestamp()) + " at " + ticket.getServer() + "," + ticket.getWorld() + "," + ticket.getX() + "," + ticket.getY() + "," + ticket.getZ());
        t.addExtra("  ");
        TextComponent name = new TextComponent("§b[§aProfile§b]");
        name.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile " + p));
        name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Profile " + p)));
        t.addExtra(name);
        t.addExtra("  ");
        TextComponent tp = new TextComponent("§b[§aTeleport§b]");
        tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpid " + id));
        tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Teleport to modreq #" + id)));
        t.addExtra(tp);
        t.addExtra("  ");
        TextComponent dibs = new TextComponent("§b[§aDibs§b]");
        dibs.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dibs " + id));
        dibs.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Dibs modreq #" + id)));
        t.addExtra(dibs);
        t.addExtra("  ");
        TextComponent done = new TextComponent("§b[§aDone§b]");
        done.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/done " + id + " "));
        done.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Finish modreq #" + id)));
        t.addExtra(done);
        sender.sendMessage(t);
        if(!ticket.isClosed() && ticket.isClaimed()) {
            sender.sendMessage(new TextComponent("§eClaimed by §d" + ticket.getModeratorName() + "§e at §d" + getDateStr(ticket.getModeratorTimestamp())));
            sender.sendMessage(new TextComponent("§7" + ticket.getText()));
        }
        else if(ticket.isClosed()) {
            sender.sendMessage(new TextComponent("§eHandled by §d" + ticket.getModeratorName() + "§e at §d" + getDateStr(ticket.getModeratorTimestamp())));
            sender.sendMessage(new TextComponent("§7" + ticket.getText()));
            sender.sendMessage(new TextComponent("§6Mod comment - §7" + ticket.getModeratorText()));
        }
        else {
            sender.sendMessage(new TextComponent("§7" + ticket.getText()));
        }
    }

    private Ticket getTicketById(int id) {
        for(Ticket ticket: tickets) {
            if(id == ticket.getId()) return ticket;
        }
        return null;
    }

    public void claimTicket(CommandSender sender, int ticketId) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Ticket ticket = getTicketById(ticketId);
        if(ticket == null) {
            player.sendMessage(new TextComponent("§cInvalid ticket id."));
            return;
        }

        if(ticket.isClosed()) {
            player.sendMessage(new TextComponent("§cCan't claim a closed ticket."));
            return;
        }

        if(ticket.isClaimed()) {
            player.sendMessage(new TextComponent("§cTicket is already claimed."));
            return;
        }

        ticket.setClaimed(true);
        ticket.setModerator(player.getUniqueId());
        ticket.setModeratorName(player.getName());
        ticket.setModeratorTimestamp(System.currentTimeMillis());
        updateTicketAsync(ticket);

        sendNotification("§6" + player.getName() + "§6 calls dibs on request #" + ticket.getId() + ".");
        sendPlayerNotification(ticket.getPlayer(), "§6" + player.getName() + "§6 is now handling your request.");
    }

    public void closeTicket(CommandSender sender, int ticketId, String text) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Ticket ticket = getTicketById(ticketId);
        if(ticket == null) {
            player.sendMessage(new TextComponent("§cInvalid ticket id."));
            return;
        }

        if(ticket.isClosed()) {
            player.sendMessage(new TextComponent("§cTicket is already closed."));
            return;
        }

        ticket.setClosed(true);
        ticket.setModeratorText(text);
        ticket.setModerator(player.getUniqueId());
        ticket.setModeratorName(player.getName()); // TODO: Get current name from playerdata module (not just here, everywhere)
        ticket.setModeratorTimestamp(System.currentTimeMillis());

        sendNotification("§6Request #" + ticket.getId() + " has been completed.");
        sendNotification("§6Mod comment - §7" + text);
        sendPlayerNotification(ticket.getPlayer(), "§6" + player.getName() + "§6 has completed your request.");
        if(sendPlayerNotification(ticket.getPlayer(), "§6Mod comment - §7" + text)) {
            ticket.setPlayerNotified(true);
        }

        updateOpenTicketPlayerList();        
        updateTicketAsync(ticket);
    }

    public void reopenTicket(CommandSender sender, int ticketId) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Ticket ticket = getTicketById(ticketId);
        if(ticket == null) {
            player.sendMessage(new TextComponent("§cInvalid ticket id."));
            return;
        }

        if(!ticket.isClosed() && !ticket.isClaimed()) {
            player.sendMessage(new TextComponent("§cTicket is not closed."));
            return;
        }

        ticket.setClosed(false);
        ticket.setClaimed(false);
        ticket.setHeld(false);
        ticket.setModerator(player.getUniqueId());
        ticket.setModeratorName(player.getName());
        ticket.setModeratorTimestamp(System.currentTimeMillis());
        ticket.setPlayerNotified(false);

        updateOpenTicketPlayerList();
        updateTicketAsync(ticket);

        sendNotification("§6Request #" + ticket.getId() + " has been reopened.");
    }
    
    public void unclaimTicket(CommandSender sender, int ticketId) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Ticket ticket = getTicketById(ticketId);
        if(ticket == null) {
            player.sendMessage(new TextComponent("§cInvalid ticket id."));
            return;
        }

        if(ticket.isClosed()) {
            player.sendMessage(new TextComponent("§cTicket is closed."));
            return;
        }

        if(!ticket.isClaimed()) {
            player.sendMessage(new TextComponent("§cTicket is not claimed."));
            return;
        }

        ticket.setClaimed(false);
        ticket.setModerator(player.getUniqueId());
        ticket.setModeratorName(player.getName());
        ticket.setModeratorTimestamp(System.currentTimeMillis());
        updateTicketAsync(ticket);

        sendNotification("§6Request #" + ticket.getId() + " is no longer assigned.");
        sendPlayerNotification(ticket.getPlayer(), "§6" + player.getName() + "§6 is no longer handling your request. Please wait for another mod.");
    }

    public void holdTicket(CommandSender sender, int ticketId) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Ticket ticket = getTicketById(ticketId);
        if(ticket == null) {
            player.sendMessage(new TextComponent("§cInvalid ticket id."));
            return;
        }

        if(ticket.isClosed()) {
            player.sendMessage(new TextComponent("§cClosed tickets can't be held."));
            return;
        }

        if(ticket.isHeld()) {
            player.sendMessage(new TextComponent("§cTicket is already held."));
            return;
        }

        ticket.setHeld(true);
        ticket.setClaimed(false);
        ticket.setModerator(player.getUniqueId());
        ticket.setModeratorName(player.getName());
        ticket.setModeratorTimestamp(System.currentTimeMillis());
        updateOpenTicketPlayerList();        
        updateTicketAsync(ticket);

        sendNotification("§6Request #" + ticket.getId() + " is now on hold.");
    }
    
    public void unholdTicket(CommandSender sender, int ticketId) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;
        
        Ticket ticket = getTicketById(ticketId);
        if(ticket == null) {
            player.sendMessage(new TextComponent("§cInvalid ticket id."));
            return;
        }
        
        if(ticket.isClosed()) {
          player.sendMessage(new TextComponent("§cTicket is closed, cannot hold"));
          return;
        }
        
        if(!(ticket.isHeld())) {
          player.sendMessage(new TextComponent("§cTicket is not held."));
          return;
        }
        
        ticket.setHeld(false);
        ticket.setClaimed(false);
        ticket.setModerator(player.getUniqueId());
        ticket.setModeratorName(player.getName());
        ticket.setModeratorTimestamp(System.currentTimeMillis());
        updateOpenTicketPlayerList();
        updateTicketAsync(ticket);
        
        sendNotification("§6Request #" + ticket.getId() + " removed from the hold list.");
    }
    
    public void tpid(CommandSender sender, int ticketId) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Ticket ticket = getTicketById(ticketId);
        if(ticket == null) {
            player.sendMessage(new TextComponent("§cInvalid ticket id."));
            return;
        }

        String sourceServer = player.getServer().getInfo().getName();
        String targetServer = ticket.getServer();

        if(sourceServer.equals(targetServer)) {
            ipc.sendMessage(targetServer, "tplocal|" + player.getUniqueId() + "|coord:" + ticket.getWorld() + "," + ticket.getX() + "," + ticket.getY() + "," + ticket.getZ());
        }
        else {
            ipc.sendMessage(targetServer, "xwportal|" + player.getUniqueId() + "|coord:" + ticket.getWorld() + "," + ticket.getX() + "," + ticket.getY() + "," + ticket.getZ() + "|" + targetServer);
        }
    }

    public void updateTicketAsync(Ticket ticket) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> dao.updateTicket(ticket));
    }

    private void updateOpenTicketPlayerList() {
        openTicketPlayerList = new HashSet<>();
        for(Ticket ticket: tickets) {
            if(!ticket.isClosed() && !ticket.isHeld()) {
                openTicketPlayerList.add(ticket.getPlayerName());
            }
        }
    }

    public Set<String> getOpenTicketPlayerList() {
        return new HashSet<>(openTicketPlayerList);
    }
}
