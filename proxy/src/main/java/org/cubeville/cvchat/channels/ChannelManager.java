package org.cubeville.cvchat.channels;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvipc.IPCInterface;

import org.cubeville.cvchat.CVChat;
import org.cubeville.cvchat.Util;

public class ChannelManager implements IPCInterface
{
    private Map<String, Channel> channels;
    private File statusFolder;

    private Map<Integer, String> messageQueue;
    private Integer messageQueueId;

    private CVIPC ipc;

    private static ChannelManager instance;
    public static ChannelManager getInstance() {
        return instance;
    }

    public ChannelManager(Configuration config, File statusFolder, CVIPC ipc) {
        instance = this;

        this.ipc = ipc;
        ipc.registerInterface("chatquery", this);
        ipc.registerInterface("localmonitor", this); //TODO: Disable this.
            
        this.statusFolder = statusFolder;

        channels = new HashMap<>();
        
        Collection<String> channelNames = config.getKeys();
        for(String channelName: channelNames) {
            Configuration channelConfig = (Configuration) config.get(channelName);
            String type = channelConfig.getString("type");

            Map<String, String> format = new HashMap<>();
            {
                Collection<String> formatKeys = channelConfig.getSection("format").getKeys();
                for(String key: formatKeys) {
                    format.put(key, channelConfig.getSection("format").getString(key));
                }
            }
            
            Channel channel =
                ChannelFactory.getChannel(channelName, type,
                                          channelConfig.getString("permission"),
                                          channelConfig.getString("sendpermission"),
                                          channelConfig.getString("colorpermission"),
                                          channelConfig.getString("leavepermission"),
                                          format,
                                          channelConfig.getBoolean("default"),
                                          channelConfig.getBoolean("autojoin"),
                                          channelConfig.getBoolean("listable"),
                                          channelConfig.getBoolean("filtered"),
                                          (List<String>) channelConfig.getList("commands"),
                                          (List<String>) channelConfig.getList("users"));
            channels.put(channelName, channel);
        }
        
        
    }

    public void sendQuery(String channel, Integer id, ProxiedPlayer player, String parameter) {
        ipc.sendMessage(player.getServer().getInfo().getName(), "chatquery|" + channel + "|" + id + "|" + player + "|" + parameter);
    }

    public CVIPC getIPC() {
        return ipc;
    }
    
    public void process(String serverName, String channel, String message) {
        if(channel.equals("chatquery")) {
            int idx = message.indexOf("|");
            if(idx == -1) return;
            String channelName = message.substring(0, idx);
            if(channels.containsKey(channelName)) {
                channels.get(channelName).processIpcQuery(message);
            }
        }
        else if(channel.equals("localmonitor")){
            System.out.println("Localmonitor message: " + serverName + " -> " + message);
            getLocalChannel().sendMonitorMessage(serverName, message);
        }
    }
    
    public void saveStatus(ProxiedPlayer player) {
        List<String> list = new ArrayList<>();
        for(Channel channel: channels.values()) {
            String s = channel.getConfigurationString(player);
            if(s != null) list.add(s);
        }
        UUID uuid = player.getUniqueId();
        ProxyServer.getInstance().getScheduler()
            .runAsync(CVChat.getInstance(),
                      new Runnable() {
                          public void run() {
                              // this feels like it needs a lock, but then also feels like it doesn't?
                              Util.saveFile(new File(statusFolder, uuid.toString()), list);
                          }
                      });
    }

    public void playerLogin(ProxiedPlayer player) {
        File file = new File(statusFolder, player.getUniqueId().toString());
        Map<String, String> config = new HashMap<>();
        if(file.exists()) {
            List<String> t = Util.readFile(file);
            for(String s: t) {
                config.put(Util.getPropertyName(s), s);
            }
        }
        for(String channel: channels.keySet()) {
            channels.get(channel).playerLogin(player, config.get(channel));
        }
        // I was very sure that I wanted to save if it's a new player, but
        // I absolutely can't remember why.
    }

    public void playerDisconnect(ProxiedPlayer player) {
        for(Channel channel: channels.values()) {
            channel.playerDisconnect(player);
        }
    }
    
    public GroupChannel getGroupChannel() {
        for(String channel: channels.keySet()) {
            if(channels.get(channel) instanceof GroupChannel) {
                return (GroupChannel) channels.get(channel);
            }
        }
        return null;
    }

    public LocalChannel getLocalChannel() {
        for(String channel: channels.keySet()) {
            if(channels.get(channel) instanceof LocalChannel) {
                return (LocalChannel) channels.get(channel);
            }
        }
        return null;        
    }

    public Channel getChannel(String name) {
        return channels.get(name);
    }
    
    public Collection<Channel> getChannels() {
        return channels.values();
    }

    public Map<String, Channel> getChannelMap() {
        return channels;
    }
}
