package org.cubeville.cvchat.sanctions;

import java.util.*;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.cubeville.cvchat.playerdata.PlayerDataManager;

public class SanctionManager
{
    Map<UUID, List<Map<Long, String>>> filteredMessages;
    boolean allChatMuted;
    Map<UUID, Long> mutedPlayers;
    List<String> filterTerms;
    
    private static SanctionManager instance;
    public static SanctionManager getInstance() {
        return instance;
    }

    public SanctionManager(List<String> filterTerms) {
        instance = this;
        filteredMessages = new HashMap<>();
        allChatMuted = false;
        mutedPlayers = new HashMap<>();
        this.filterTerms = filterTerms;
    }

    public void mutePlayer(ProxiedPlayer player) {
        mutedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void mutePlayer(UUID player) {
        mutedPlayers.put(player, System.currentTimeMillis());
    }

    public void unmutePlayer(ProxiedPlayer player) {
        mutedPlayers.remove(player.getUniqueId());
    }

    public void unmutePlayer(UUID player) {
        mutedPlayers.remove(player);
    }

    public boolean isPlayerMuted(CommandSender player) {
        if(player instanceof ProxiedPlayer) {
            return mutedPlayers.containsKey(((ProxiedPlayer) player).getUniqueId());
        }
        else {
            return false;
        }
    }

    public boolean isPlayerMuted(UUID player) {
        return mutedPlayers.containsKey(player);
    }

    public void muteAllChat() {
        allChatMuted = true;
    }

    public void unmuteAllChat() {
        allChatMuted = false;
    }

    public boolean isAllChatMuted() {
        return allChatMuted;
    }

    public boolean banPlayer(CommandSender sender, UUID bannedPlayerId, String banReason, long duration, boolean silent) {
        UUID senderId = null;
        if(sender instanceof ProxiedPlayer) senderId = ((ProxiedPlayer) sender).getUniqueId();
        PlayerDataManager pdm = PlayerDataManager.getInstance();

        if(duration == 0 && pdm.isBanned(bannedPlayerId, false) && pdm.isPermanentlyBanned(bannedPlayerId)) return false;

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(bannedPlayerId);

        boolean startBanNow = duration > 0 && player != null;
        pdm.banPlayer(bannedPlayerId, senderId, banReason, duration * 1000, startBanNow);
           
        if(player != null) {
            String kname = "Console";
            if(sender instanceof ProxiedPlayer) kname = ((ProxiedPlayer) sender).getDisplayName();
            player.disconnect("§6You have been " + (duration != 0 ? "temporarily " : "") + "banned by §e" + kname + "§6.\nReason: §e" + banReason);
        }

        return true;
    }

    public String unbanPlayer(CommandSender sender, String bannedPlayerName, boolean silent) {
        PlayerDataManager pdm = PlayerDataManager.getInstance();
        UUID bannedPlayerId = pdm.getPlayerId(bannedPlayerName);
        if(bannedPlayerId == null) return null;
        pdm.unbanPlayer(bannedPlayerId);
        return pdm.getPlayerName(bannedPlayerId);
    }

    public void addFilteredMessage(UUID player, String message) {
        List<Map<Long, String>> messages;
        if(filteredMessages.containsKey(player)) {
            messages = filteredMessages.get(player);
        } else {
            messages = new ArrayList<>();
        }
        Map<Long, String> messageMap = new HashMap<>();
        messageMap.put(System.currentTimeMillis(), message);
        messages.add(messageMap);
        filteredMessages.put(player, messages);
    }

    public List<Map<Long, String>> getFilteredMessages(UUID player) {
        if(filteredMessages.containsKey(player)) {
            return filteredMessages.get(player);
        }
        return null;
    }

    public boolean checkFilter(String message) {
        for(String term: filterTerms) {
            if(message.toLowerCase().indexOf(term) != -1) return true;
        }
        return false;
    }

    public boolean checkCaps(String message) {
        if(message.length() < 10) return false;
        int cnt = 0;
        int maxcnt = message.length() / 3;
        for(int i = 0; i < message.length(); i++) {
            if(message.charAt(i) >= 'A' && message.charAt(i) <= 'Z') cnt++;
            if(cnt > maxcnt) return true;
        }
        return false;
    }

    public boolean checkSpam(String message) {
        if(message.length() < 10) return false;
        String s = message.toLowerCase();
        int cnt = 0;
        char lastchar = 0;
        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == lastchar) {
                cnt++;
                if(cnt >= 5) return true;
            }
            else {
                lastchar = s.charAt(i);
                cnt = 0;
            }
        }
        return false;
    }
}
