package org.cubeville.cvchat;

import java.io.File;
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

import org.cubeville.cvchat.commands.*;

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

    private ChatListener chatListener;
    
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

    public void updateCommandWhitelist() {
        Configuration config = loadConfigFile();
        Map<String, Set<String>> commandWhitelist = new HashMap<>();
        Configuration whitelistConfig = (Configuration) config.get("whitelist");
        for(String whitelist: whitelistConfig.getKeys()) {
            commandWhitelist.put(whitelist, new HashSet<>(whitelistConfig.getStringList(whitelist)));
        }
        chatListener.setCommandWhitelist(commandWhitelist);
    }

    private Configuration loadConfigFile() {
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        }
        catch(Exception e) {
            throw new RuntimeException("Config loading failed: " + e.getMessage());
        }
    }
    
    @Override
    public void onEnable() {
        instance = this;

        commandLoggingBlacklist = new HashSet<>();
        startupTime = System.currentTimeMillis();
        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            System.out.println("Uptime: " + (getUptime() / 60) + " minutes");
            ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "who");
        }, 60, 60, TimeUnit.SECONDS);
        
        logger = new Logger(new File(getDataFolder(), "logs"));
        ProxyServer.getInstance().getScheduler().schedule(this, logger, 2000, 2000, TimeUnit.MILLISECONDS);

        // Create player data directory
        File statusFolder = new File(getDataFolder(), "status");
        statusFolder.mkdirs();

        PluginManager pm = getProxy().getPluginManager();

        CVIPC ipc = (CVIPC) pm.getPlugin("CVIPC");
        
        // Load configuration (must exist for the plugin to work properly)
        Configuration config = loadConfigFile();

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
                pm.registerCommand(this, new ReloadWhitelistCommand(this));
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

        chatListener = new ChatListener(local, textCommandManager, ticketManager, ipc, commandLoggingBlacklist);
        updateCommandWhitelist();
            
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
        boolean enableAdminMFA = config.getBoolean("enableAdminMFA", true);
        LoginListener loginListener = new LoginListener(channelManager, ticketManager, versionCheckBypass, enableAdminMFA);
        pm.registerListener(this, loginListener);
        pm.registerCommand(this, new ChannelCommand(channelManager));
            
        pm.registerCommand(this, new FinishCommand(ipc, textCommandManager));
        pm.registerCommand(this, new ForceTutorialFinishCommand());
            
        // Load ranks configuration
        Configuration ranksList = (Configuration) config.get("ranks");
        Configuration prefixesList = (Configuration) config.get("prefixes");
        RankManager rankManager = new RankManager(ranksList, prefixesList);

        // Load unicode characters
        HashMap<String, List<String>> unicodeTranslations = new HashMap<>();
        List<HashMap> letters = (List<HashMap>) config.getList("unicode");
        for(HashMap letter : letters) {
            List<String> values = (List<String>) letter.values().iterator().next();
            unicodeTranslations.put(letter.keySet().toArray()[0].toString(), values);
        }

        // Load swear filter words
        sanctionManager = new SanctionManager(config.getStringList("filter"), unicodeTranslations);
        pm.registerCommand(this, new SwearCheckCommand(this));

        {
            // Initialize private message chat commands
            pm.registerCommand(this, new MsgCommand());
            pm.registerCommand(this, new RCommand());
            pm.registerCommand(this, new RlCommand());
            commandLoggingBlacklist.add("msg");
            commandLoggingBlacklist.add("r");
            commandLoggingBlacklist.add("rl");
            commandLoggingBlacklist.add("w");
            commandLoggingBlacklist.add("whisper");
            commandLoggingBlacklist.add("tell");
                
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
            pm.registerCommand(this, new SetAliasCommand());
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
                pm.registerCommand(this, new SCommand());
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
}
