package org.cubeville.cvchat.channels;

import java.util.Collection;
import java.util.Map;

public class ChannelFactory
{
    public static Channel getChannel(String name, String type, String viewPermission, String sendPermission, String colorPermission, String leavePermission, Map<String, String> format, boolean isDefault, boolean autojoin, boolean listable, boolean filtered, Collection<String> commands, Collection<String> users) {
        if(type != null && type.equals("group")) {
            return new GroupChannel(name,
                                    viewPermission,
                                    sendPermission,
                                    colorPermission,
                                    leavePermission,
                                    format,
                                    isDefault,
                                    autojoin,
                                    listable,
                                    filtered,
                                    commands);
        }
        else if(type != null && type.equals("local")) {
            return new LocalChannel(name,
                                    viewPermission,
                                    sendPermission,
                                    colorPermission,
                                    leavePermission,
                                    format,
                                    isDefault,
                                    autojoin,
                                    listable,
                                    filtered,
                                    commands);
        }
        else {
            return new Channel(name,
                               viewPermission,
                               sendPermission,
                               colorPermission,
                               leavePermission,
                               format,
                               isDefault,
                               autojoin,
                               listable,
                               filtered,
                               commands,
                               users);
        }
    }

}
