package au.id.rleach.chatclear;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.nio.file.Path;
import java.util.Optional;

@Plugin( id = au.id.rleach.chatclear.Plugin.ID,
         name = au.id.rleach.chatclear.Plugin.NAME,
         version = au.id.rleach.chatclear.Plugin.VERSION,
         description = au.id.rleach.chatclear.Plugin.DESCRIPTION
)
public class ChatClear {
    @Inject
    @ConfigDir(sharedRoot = true)
    private Path configDir;

    @Inject
    private PluginContainer container;

    private CommentedConfigurationNode configNode;
    private ChatClearConfig chatClearConfig;
    private Optional<CommandMapping> clearMapping = Optional.empty();
    private Optional<CommandMapping> globalClearMapping = Optional.empty();
    private PermissionService permissionService;

    @Listener
    public void onPreInit(GameInitializationEvent event){
        permissionService = Sponge.getServiceManager().provide(PermissionService.class).get();
        setup();
    }

    private void setup(){
        configNode = ConfigLoader.loadConfigUnchecked("chatclear.conf", configDir, container);
        try {
            chatClearConfig = configNode.getValue(ChatClearConfig.TYPE);
        } catch (ObjectMappingException e) {
            System.out.print(e);
        }
        clearMapping = Sponge.getCommandManager().register(this, clearchat(), chatClearConfig.aliases);
        globalClearMapping = Sponge.getCommandManager().register(this, globalClearChat(), chatClearConfig.globalClearAliases);

        registerPD(PermissionDescription.ROLE_USER, chatClearConfig.permission, "Allow access to clearchat command");
        registerPD(PermissionDescription.ROLE_STAFF, chatClearConfig.globalClearPermission, "Allow access to global clearchat command");
        registerPD(PermissionDescription.ROLE_USER, chatClearConfig.clearOnJoinPermission, "having this clears your chat on join");
        registerPD(PermissionDescription.ROLE_STAFF, chatClearConfig.immunityPermissionGlobal, "having this prevents you from having your"
                + " chat cleared during global clear commands");
    }

    void registerPD(final String role, final String permission, final String description){
        final Optional<PermissionDescription.Builder> pdbuilder = permissionService.newDescriptionBuilder(this);
        pdbuilder.ifPresent(
                pd->pd.assign(PermissionDescription.ROLE_USER, true).id(permission).description(Text.of(description)).register()
        );
    }

    private void removeMapping(final Optional<CommandMapping> mapping){
        mapping.ifPresent(
            Sponge.getCommandManager()::removeMapping
        );
    }

    @Listener
    public void clearOnJoin(final ClientConnectionEvent.Join join, @Root final Player p){
        if(chatClearConfig.clearOnJoin && p.hasPermission(chatClearConfig.clearOnJoinPermission)) {
            doClearChat(p);
        }
    }


    private CommandCallable clearchat() {
        return CommandSpec.builder()
                .permission(chatClearConfig.permission)
                .description(Text.of(chatClearConfig.description))
                .executor(this::doClearChat)
                .build();
    }

    private CommandCallable globalClearChat(){
        return CommandSpec.builder()
                .permission(chatClearConfig.globalClearPermission)
                .description(Text.of(chatClearConfig.globalClearDescription))
                .executor(this::doGlobalClear)
                .build();
    }

    private CommandResult doGlobalClear(final CommandSource commandSource, final CommandContext commandContext) {
        int count = 0;
        for(final Player player:Sponge.getServer().getOnlinePlayers()){
           if(player.hasPermission(chatClearConfig.immunityPermissionGlobal)){
                player.sendMessage(Text.of(chatClearConfig.immunityMessage));
            } else {
                count++;
               this.doClearChat(player);
            }
        }
        return CommandResult.builder().successCount(count).build();
    }

    private CommandResult doClearChat(final CommandSource commandSource, final CommandContext commandContext) {
        doClearChat(commandSource);
        return CommandResult.builder().successCount(chatClearConfig.lines).build();
    }

    void doClearChat(final MessageReceiver receiver){
        final Text.Builder builder = Text.builder();
        for(int i=0; i < chatClearConfig.lines; i++){
            builder.append(Text.NEW_LINE);
        }
        receiver.sendMessages(builder.build());
    }
}
