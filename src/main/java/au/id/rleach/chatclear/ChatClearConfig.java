package au.id.rleach.chatclear;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatClearConfig {

    public static final TypeToken<ChatClearConfig> TYPE = TypeToken.of(ChatClearConfig.class);

    @Setting(value = "aliases", comment = "chatclear command aliases")
    String[] aliases;
    @Setting(value = "permission", comment = "permission used for command")
    String permission;
    @Setting(value = "description", comment = "description used for help text on command")
    String description;

    @Setting(value = "lines", comment = "Amount of lines to send to the client")
    int lines;
    @Setting(value = "clearOnJoin", comment = "whether chat is cleared on join for people")
    boolean clearOnJoin;
    @Setting(value = "clearOnJoinPermission", comment = "permission required to have chat cleared on join")
    String clearOnJoinPermission;

    @Setting(value = "globalClearAliases", comment = "globalClear command aliases")
    String[] globalClearAliases;
    @Setting(value = "globalClearPemission", comment = "permission required to clear chat of an entire server")
    String globalClearPemission;
    @Setting(value = "globalClearDescription", comment = "clears the chat for everyone that does not have immunity")
    String globalClearDescription;

    @Setting(value = "immunityPermissionGlobal", comment = "immunity to global chat clears")
    String immunityPermissionGlobal;
    @Setting(value = "immunityMessage", comment = "Message sent to players with immunity")
    String immunityMessage;

}