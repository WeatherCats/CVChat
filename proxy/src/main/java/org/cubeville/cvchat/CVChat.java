package org.cubeville.cvchat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import org.cubeville.cvipc.CVIPC;

import org.cubeville.cvchat.channels.Channel;
import org.cubeville.cvchat.channels.ChannelManager;
import org.cubeville.cvchat.channels.GroupChannel;
import org.cubeville.cvchat.channels.LocalChannel;

import org.cubeville.cvchat.commands.BacksiesCommand;
import org.cubeville.cvchat.commands.BanCommand;
import org.cubeville.cvchat.commands.BlockNewPlayersCommand;
import org.cubeville.cvchat.commands.ChannelCommand;
import org.cubeville.cvchat.commands.ChatCommand;
import org.cubeville.cvchat.commands.CheckCommand;
import org.cubeville.cvchat.commands.CheckbanCommand;
import org.cubeville.cvchat.commands.ClearchatCommand;
import org.cubeville.cvchat.commands.CommandCheckCommand;
import org.cubeville.cvchat.commands.DibsCommand;
import org.cubeville.cvchat.commands.DoneCommand;
import org.cubeville.cvchat.commands.FinishCommand;
import org.cubeville.cvchat.commands.ForceTutorialFinishCommand;
import org.cubeville.cvchat.commands.FjCommand;
import org.cubeville.cvchat.commands.ForwardCommand;
import org.cubeville.cvchat.commands.FqCommand;
import org.cubeville.cvchat.commands.GTrCommand;
import org.cubeville.cvchat.commands.GroupCommand;
import org.cubeville.cvchat.commands.HoldCommand;
import org.cubeville.cvchat.commands.KickCommand;
import org.cubeville.cvchat.commands.LocalCommand;
import org.cubeville.cvchat.commands.LocchatCommand;
import org.cubeville.cvchat.commands.ModlistCommand;
import org.cubeville.cvchat.commands.MsgCommand;
import org.cubeville.cvchat.commands.MuteAllCommand;
import org.cubeville.cvchat.commands.MuteCommand;
import org.cubeville.cvchat.commands.NewCommand;
import org.cubeville.cvchat.commands.NoteCommand;
import org.cubeville.cvchat.commands.PTrCommand;
import org.cubeville.cvchat.commands.PrefixCommand;
import org.cubeville.cvchat.commands.ProfileCommand;
import org.cubeville.cvchat.commands.RCommand;
import org.cubeville.cvchat.commands.ReopenCommand;
import org.cubeville.cvchat.commands.RlCommand;
import org.cubeville.cvchat.commands.SuCommand;
import org.cubeville.cvchat.commands.SwearCheckCommand;
import org.cubeville.cvchat.commands.TempbanCommand;
import org.cubeville.cvchat.commands.TestCommand;
import org.cubeville.cvchat.commands.TpidCommand;
import org.cubeville.cvchat.commands.TrCommand;
import org.cubeville.cvchat.commands.UnbanCommand;
import org.cubeville.cvchat.commands.UnholdCommand;
import org.cubeville.cvchat.commands.UnmuteAllCommand;
import org.cubeville.cvchat.commands.UnmuteCommand;
import org.cubeville.cvchat.commands.VcbAddCommand;
import org.cubeville.cvchat.commands.WhoCommand;

import org.cubeville.cvchat.log.Logger;

import org.cubeville.cvchat.ranks.RankManager;
import org.cubeville.cvchat.sanctions.SanctionManager;
import org.cubeville.cvchat.playerdata.PlayerDataManager;
import org.cubeville.cvchat.playerdata.PlayerDataDao;
import org.cubeville.cvchat.playerdata.ProfilesDao;
import org.cubeville.cvchat.textcommands.TextCommandManager;
import org.cubeville.cvchat.tickets.TicketManager;
import org.cubeville.cvchat.tickets.TicketDao;

// TODO: Private messaging with afk function :)
// TODO: hidden staff

public class CVChat extends Plugin {

    private ChannelManager channelManager;
    private SanctionManager sanctionManager;
    private TicketManager ticketManager;
    private PlayerDataManager playerDataManager;

    private Logger logger;
    private Set<String> commandLoggingBlacklist;

    private long startupTime;
    
    private static CVChat instance;
    public static CVChat getInstance() {
        return instance;
    }

    public long getUptime() {
        long uptime = System.currentTimeMillis() - startupTime;
        uptime /= 1000;
        return uptime;
    }
    
    @Override
    public void onEnable() {
        instance = this;

        commandLoggingBlacklist = new HashSet<>();
        startupTime = System.currentTimeMillis();
        ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() {
                public void run() {
                    System.out.println("Uptime: " + (getUptime() / 60) + " minutes");
                    ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "who");
                }
            }, 60, 60, TimeUnit.SECONDS);
        
        logger = new Logger(new File(getDataFolder(), "logs"));
        ProxyServer.getInstance().getScheduler().schedule(this, logger, 2000, 2000, TimeUnit.MILLISECONDS);

        // Create player data directory
        File statusFolder = new File(getDataFolder(), "status");
        statusFolder.mkdirs();

        PluginManager pm = getProxy().getPluginManager();

        CVIPC ipc = (CVIPC) pm.getPlugin("CVIPC");
        
        // Load configuration (must exist for the plugin to work properly)
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

            // Start auto messager
            AutoMessager messager = new AutoMessager(120, config.getStringList("automessager"), this);

            { // Install ticket system
                Configuration ticketDaoConfig = (Configuration) config.get("tickets");
                if(ticketDaoConfig != null) {
                    TicketDao ticketDao = new TicketDao(ticketDaoConfig.getString("db_user"),
                                                        ticketDaoConfig.getString("db_password"),
                                                        ticketDaoConfig.getString("db_database"));
                    ticketManager = new TicketManager(this, ipc, ticketDao);
                    pm.registerCommand(this, new CheckCommand(ticketManager));
                    pm.registerCommand(this, new DibsCommand(ticketManager));
                    pm.registerCommand(this, new DoneCommand(ticketManager));
                    pm.registerCommand(this, new HoldCommand(ticketManager));
                    pm.registerCommand(this, new ReopenCommand(ticketManager));
                    pm.registerCommand(this, new TpidCommand(ticketManager));
                    pm.registerCommand(this, new BacksiesCommand(ticketManager));
                    pm.registerCommand(this, new UnholdCommand(ticketManager));
                }
                else {
                    System.out.println("No ticket dao configuration found. Ticket system not available.");
                }
            }
            
            // Initialize channel manager from configuration
            Configuration channelList = (Configuration) config.get("channels");
            channelManager = new ChannelManager(channelList, statusFolder, ipc);

            // Add commands and listeners
            for(Channel channel: channelManager.getChannels()) {
                for(String command: channel.getCommands()) {
                    pm.registerCommand(this, new ChatCommand(command, channel));
                    commandLoggingBlacklist.add(command);
                }
            }

            GroupChannel group = channelManager.getGroupChannel();
            if(group != null) {
                pm.registerCommand(this, new GroupCommand(group));
            }

            Configuration textCommandConfig = (Configuration)config.get("textcommands");
            TextCommandManager textCommandManager = new TextCommandManager(textCommandConfig);

            LocalChannel local = channelManager.getLocalChannel();
            //Set<String> commandWhitelist = new HashSet<String>(((Configuration)config.get("whitelist")).getStringList("standard"));
            //Set<String> commandWhitelistTutorial = new HashSet<String>(((Configuration)config.get("whitelist")).getStringList("tutorial"));

            Map<String, Set<String>> commandWhitelist = new HashMap<>();
            Configuration whitelistConfig = (Configuration) config.get("whitelist");
            for(String whitelist: whitelistConfig.getKeys()) {
                commandWhitelist.put(whitelist, new HashSet<String>(whitelistConfig.getStringList(whitelist)));
            }

            ChatListener chatListener = new ChatListener(local, commandWhitelist, textCommandManager, ticketManager, ipc, commandLoggingBlacklist);
	    List<HashMap> aliasconf = (List<HashMap>)config.getList("aliases");
	    for(HashMap a: aliasconf) {
		List<String> cmds;
		if(a.get("commands") instanceof String) {
		    cmds = new ArrayList<>();
		    cmds.add((String) a.get("commands"));
		}
		else {
		    cmds = (List<String>) a.get("commands");
		}
		List<String> translations;
		if(a.get("translate") instanceof String) {
		    translations = new ArrayList<>();
		    translations.add((String) a.get("translate"));
		}
		else {
		    translations = (List<String>) a.get("translate");
		}
		chatListener.addAlias(cmds,
				      translations,
				      (String)a.get("server"),
				      (String)a.get("permission"));
	    }
            pm.registerListener(this, chatListener);

            Set<UUID> versionCheckBypass = new HashSet<>();
            List<String> versionCheckBypassStrings = config.getStringList("versioncheckbypass");
            for(String s: versionCheckBypassStrings) {
                versionCheckBypass.add(UUID.fromString(s));
            }
            LoginListener loginListener = new LoginListener(channelManager, ticketManager, versionCheckBypass);
            pm.registerListener(this, loginListener);
            pm.registerCommand(this, new ChannelCommand(channelManager));
            
            pm.registerCommand(this, new FinishCommand(ipc, textCommandManager));
            pm.registerCommand(this, new ForceTutorialFinishCommand());
            
            // Load ranks configuration
            Configuration ranksList = (Configuration) config.get("ranks");
            Configuration prefixesList = (Configuration) config.get("prefixes");
            RankManager rankManager = new RankManager(ranksList, prefixesList);
            
            // Load swear filter words 
            sanctionManager = new SanctionManager(config.getStringList("filter"));
            pm.registerCommand(this, new SwearCheckCommand(this));

            {
                // Initialize private message chat commands
                pm.registerCommand(this, new MsgCommand());
                pm.registerCommand(this, new RCommand());
                pm.registerCommand(this, new RlCommand());
                commandLoggingBlacklist.add("msg");
                commandLoggingBlacklist.add("r");
                commandLoggingBlacklist.add("rl");
                
                // Sanction commands
                pm.registerCommand(this, new KickCommand());
                pm.registerCommand(this, new MuteCommand());
                pm.registerCommand(this, new UnmuteCommand());
                pm.registerCommand(this, new MuteAllCommand());
                pm.registerCommand(this, new UnmuteAllCommand());
                pm.registerCommand(this, new BanCommand());
                pm.registerCommand(this, new TempbanCommand());
                pm.registerCommand(this, new UnbanCommand());
                pm.registerCommand(this, new CheckbanCommand());
                
                // Player list commands
                pm.registerCommand(this, new ModlistCommand());
                pm.registerCommand(this, new NewCommand());
                pm.registerCommand(this, new WhoCommand());
                
                // Little helper command, remove when done! TODO
                pm.registerCommand(this, new TestCommand());

                // other commands
                pm.registerCommand(this, new PrefixCommand());
                pm.registerCommand(this, new LocalCommand(local));
                pm.registerCommand(this, new LocchatCommand());
                pm.registerCommand(this, new TrCommand());
                pm.registerCommand(this, new PTrCommand());
                pm.registerCommand(this, new GTrCommand());
                pm.registerCommand(this, new SuCommand());
                pm.registerCommand(this, new ClearchatCommand());
                pm.registerCommand(this, new VcbAddCommand(loginListener));
                pm.registerCommand(this, new BlockNewPlayersCommand(loginListener));
            }

            { // Install playerdata system
                Configuration playerDataDaoConfig = (Configuration) config.get("playerdata");
                if(playerDataDaoConfig != null) {
                    PlayerDataDao playerDataDao = new PlayerDataDao(playerDataDaoConfig.getString("db_user"),
                                                                    playerDataDaoConfig.getString("db_password"),
                                                                    playerDataDaoConfig.getString("db_database"));
                    playerDataManager = new PlayerDataManager(playerDataDao);

                    ProfilesDao profilesDao = new ProfilesDao(playerDataDaoConfig.getString("db_user"),
                                                              playerDataDaoConfig.getString("db_password"),
                                                              playerDataDaoConfig.getString("db_database"));

                    pm.registerCommand(this, new ProfileCommand(this));
                    pm.registerCommand(this, new NoteCommand(this));
                    pm.registerCommand(this, new CommandCheckCommand(this));
                }
                else {
                    System.out.println("No playerdata dao configuration found.");
                }
            }

            { // Other commands
                pm.registerCommand(this, new FjCommand());
                pm.registerCommand(this, new FqCommand());
            }
            
            { // Chat forward commands for quest
                for(int i = 0; i < 20; i++) {
                    pm.registerCommand(this, new ForwardCommand(String.valueOf(i), String.valueOf(i)));
                }
            }
        }

        catch (IOException e) {
            System.out.println("Fatal error: CVChat config file not found or readable!");
            throw new RuntimeException("CVChat initialization failed!");
        }

    }
}
