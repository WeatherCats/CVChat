package org.cubeville.cvchat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvipc.IPCInterface;

import org.cubeville.cvchat.channels.Channel;
import org.cubeville.cvchat.log.Logger;
import org.cubeville.cvchat.playerdata.PlayerDataManager;
import org.cubeville.cvchat.textcommands.TextCommandManager;
import org.cubeville.cvchat.tickets.TicketManager;

public class ChatListener implements Listener, IPCInterface {

    private Channel localChannel;
    //private Set<String> commandWhitelist;
    //private Set<String> commandWhitelistTutorial;
    private Map<String, Set<String>> commandWhitelist;
    
    private TextCommandManager textCommandManager;
    private Set<UUID> tutorialChatUnlocked;
    private List<Alias> aliases;

    private TicketManager ticketManager;
    private CVIPC cvipc;

    private Set<String> commandLoggingBlacklist;
    
    public ChatListener(Channel localChannel, Map<String, Set<String>> commandWhitelist, TextCommandManager textCommandManager, TicketManager ticketManager, CVIPC ipc, Set<String> commandLoggingBlacklist) {
        this.localChannel = localChannel;
        this.commandWhitelist = commandWhitelist;
        this.textCommandManager = textCommandManager;
        this.ticketManager = ticketManager;
        tutorialChatUnlocked = new HashSet<>();
        this.cvipc = ipc;
        ipc.registerInterface("unlocktutorialchat", this);
        ipc.registerInterface("finishtutorial", this);
        aliases = new ArrayList<>();
        this.commandLoggingBlacklist = commandLoggingBlacklist;
    }

    public void unlockTutorialChat(UUID playerId) {
        tutorialChatUnlocked.add(playerId);
        System.out.println("Unlocking tutorial chat for player " + playerId);
    }

    public void process(String serverName, String channel, String message) {
        if(channel.equals("unlocktutorialchat")) {
            unlockTutorialChat(UUID.fromString(message));
        }
        else if(channel.equals("finishtutorial")) {
            System.out.println("Finishing tutorial for player " + message);
            PlayerDataManager.getInstance().setFinishedTutorial(UUID.fromString(message));
            String tpCmd = "xwportal|" + message + "|portal:Tutorial_SpawnNewPlayer|cv7survival";
            cvipc.sendMessage("cv7survival", tpCmd);            
        }
    }

    @EventHandler
    public void onChat(final ChatEvent event) {
        String name;
        if(event.getSender() instanceof ProxiedPlayer) name = ((ProxiedPlayer) event.getSender()).getDisplayName();
        else name = "Console";
        Logger.getInstance().logWithHeader(name + ": " + event.getMessage());

        if (event.isCancelled()) return;
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer)event.getSender();

        if(event.getMessage().contains("/")) {
            String[] commandSplit = event.getMessage().split(" ");
            if(commandSplit[0].contains("/") && !commandLoggingBlacklist.contains(commandSplit[0].substring(1)) && !commandSplit[0].substring(1, 2).equalsIgnoreCase("y")) {
                PlayerDataManager.getInstance().addPlayerCommand(player.getUniqueId(), event.getMessage());
            }
        }

        event.setMessage(event.getMessage().replace("§", ""));
                         
        String serverName = player.getServer().getInfo().getName();
        cvipc.sendMessage(serverName, "afktrigger|" + player.getUniqueId());
        
        boolean finishedTutorial = PlayerDataManager.getInstance().finishedTutorial(player.getUniqueId());
        if(finishedTutorial == false && tutorialChatUnlocked.contains(player.getUniqueId()) == false && player.hasPermission("cvchat.bypasstutorial") == false) {
            player.sendMessage("§cNo permission. Please proceed first.");
            event.setCancelled(true);
            System.out.println("Cancelling command due to tutorial intro for player " + player.getName());
            return;
        }

        if (event.isCommand()) {
            for(Alias alias: aliases) {
		for(String command: alias.getCommands()) {
		    boolean complete = command.endsWith("$");
		    if((complete == false && event.getMessage().toLowerCase().startsWith(command)) ||
		       (complete == true && event.getMessage().toLowerCase().equals(command))) {
			if(alias.getServer() == null || alias.getServer().equals(player.getServer().getInfo().getName())) {
			    String perm = alias.getPermission();
			    boolean perminv = false;
			    if(perm != null && perm.startsWith("!")) {
				perminv = true;
				perm = perm.substring(1);
			    }
			    if(perm != null) perm = "cvchat.alias." + perm;
			    if(perm == null || (perminv == false && player.hasPermission(perm) == true) || (perminv == true && player.hasPermission(perm) == false)) {
				List<String> translations = alias.getTranslate();
				event.setMessage(translations.get(0) + event.getMessage().substring(command.length()));
				for(int i = 1; i < translations.size(); i++) {
				    ProxyServer.getInstance().getPluginManager().dispatchCommand(player, translations.get(i));
				}
				break;
			    }
			}
		    }
		}
            }

            if(textCommandManager.executeTextCommand(player, event.getMessage())) {
                event.setCancelled(true);
                return;
            }

            if(player.hasPermission("cvchat.nowhitelist")) return;

            String cmd = event.getMessage();

            int idx = cmd.indexOf(" ");
            if(idx != -1) cmd = cmd.substring(0, idx);
            cmd = cmd.substring(1);
            cmd = cmd.toLowerCase();

            if(commandWhitelist == null || commandWhitelist.get("tutorial") == null) {
                player.sendMessage("§cCommand verification problems, please tell a server administrator.");
                event.setCancelled(true);
                return;
            }
            
            if(finishedTutorial == false) {
                if(commandWhitelist.get("tutorial").contains(cmd)) return;
                player.sendMessage("§cYou have limited permissions, please finish the tutorial first.");
                event.setCancelled(true);
                System.out.println("Cancelling command for player, not contained in tutorial whitelist: " + player.getName());
                return;
            }

            for(String whitelist: commandWhitelist.keySet()) {
                if(commandWhitelist.get(whitelist).contains(cmd) && player.hasPermission("cvchat.whitelist." + whitelist)) return;
            }

            player.sendMessage("§cNo permission.");
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);

        String message = Util.removeSectionSigns(event.getMessage());
        if(Util.playerIsHidden(player)) {
            if(message.endsWith("/")) {
                message = message.substring(0, message.length() - 1);
                if(message.length() == 0) return;
            }
            else {
                player.sendMessage("§cAdd a / to speak in local chat when you're in /v.");
                return;
            }
        }

        localChannel.sendMessage(player, message.trim());
    }

    @EventHandler
    public void OnTabCompleteResponseEvent(final TabCompleteResponseEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onTabCompleteEvent(final TabCompleteEvent event) {
        if(!(event.getSender() instanceof ProxiedPlayer)) {
            event.setCancelled(true);
            return;
        }
        
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        String c = event.getCursor();
        while(c.startsWith(" ")) c = c.substring(1);
        
        int lastSpace = c.lastIndexOf(' ');
        if(lastSpace != -1) {
            String lastWord = c.substring(lastSpace + 1);
            Set<String> players;
            if(player.hasPermission("cvchat.tabcompletion.modreq")) {
                players = ticketManager.getOpenTicketPlayerList();
            }
            else {
                players = new HashSet<>();
            }
            for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
                if(Util.playerIsUnlisted(p) == false || player.hasPermission("cvchat.tabcompletion.seehidden")) {
                    players.add(Util.removeColorCodes(p.getDisplayName()));
                }
            }
            for(String pname: players) {
                if(pname.toLowerCase().startsWith(lastWord.toLowerCase())) {
                    event.getSuggestions().add(pname);
                }
            }
        }
        else {
            if(c.startsWith("/")) {
                c = c.substring(1);
                for(String whitelist: commandWhitelist.keySet()) {
                    if(player.hasPermission("cvchat.whitelist." + whitelist)) {
                        for(String cmd: commandWhitelist.get(whitelist)) {
                            if(cmd.startsWith(c)) {
                                event.getSuggestions().add("/" + cmd + " ");
                            }
                        }
                    }
                }
            }
        }
        if(event.getSuggestions().size() == 0) {
            event.setCancelled(true);
        }
    }

    public void addAlias(List<String> commands, List<String> translate, String server, String permission) {
	aliases.add(new Alias(commands, translate, server, permission));
    }
    
    public class Alias {
	public Alias(List<String> commands, List<String> translate, String server, String permission) {
	    this.commands = commands;
	    this.translate = translate;
	    this.server = server;
	    this.permission = permission;
	}
	
	public String getServer() {
	    return server;
	}
	
	public List<String> getCommands() {
	    return commands;
	}

	public List<String> getTranslate() {
	    return translate;
	}

	public String getPermission() {
	    return permission;
	}
	
	private List<String> commands;
	private List<String> translate;
	//private String translate;
	private String server;
	private String permission;
    }
}
