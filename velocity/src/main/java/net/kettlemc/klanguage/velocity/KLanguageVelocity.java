package net.kettlemc.klanguage.velocity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import io.github.almightysatan.slams.Placeholder;
import net.kettlemc.klanguage.api.DataHandler;
import net.kettlemc.klanguage.api.LanguageAPI;
import net.kettlemc.klanguage.common.LanguageEntity;
import net.kettlemc.klanguage.common.data.HibernateDataHandler;
import net.kettlemc.klanguage.common.config.Configuration;

import com.google.inject.Inject;
import net.kettlemc.klanguage.common.config.Messages;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@Plugin(id = "klanguage", name = "kLanguage", version = "1.0.0", url = "https://kettlemc.net", description = "Allows the player to change their language.", authors = {"LeStegii"})
public final class KLanguageVelocity implements SimpleCommand {

    private final ProxyServer server;
    private final Logger logger;
    private final CommandManager commandManager;
    DataHandler dataHandler;

    private final LanguageAPI<Player> languageAPI;

    private final LegacyChannelIdentifier OUTGOING;

    @Inject
    public KLanguageVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.commandManager = server.getCommandManager();
        this.languageAPI = VelocityLanguageAPI.of(server);
        this.dataHandler = new HibernateDataHandler();
        server.getChannelRegistrar().register(OUTGOING = new LegacyChannelIdentifier(Configuration.MESSAGE_NAMESPACE.getValue() + ":" + Configuration.MESSAGE_IDENTIFIER.getValue()));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing on Velocity...");

        if (!Configuration.load()) {
            logger.severe("Failed to load config!");
        }

        if (!Messages.load()) {
            logger.severe("Failed to load language config!");
        }

        if (!Configuration.MYSQL_HOST.getValue().equals(Configuration.MYSQL_HOST.getDefaultValue())) {
            if (!dataHandler.initialize()) {
                logger.severe("Failed to initialize data handler!");
            }
        } else {
            logger.warning("MySQL host is not set! Please set it in the config. Commands will not be registered.");
            return;
        }

        logger.info("Registering command...");
        commandManager.register(commandManager.metaBuilder("language1").aliases("lang1", "sprache1").build(), this);

        server.getScheduler().buildTask(this, () -> {
            if (languageAPI.gc()) {
                logger.info("Removed unused entities from cache.");
            }
        }).repeat(Configuration.SECONDS_BETWEEN_GC.getValue(), TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Unloading config...");
        Configuration.unload();
    }

    @Subscribe
    public EventTask onAsyncLogin(LoginEvent event) {
        return EventTask.withContinuation(task -> {
            if (this.dataHandler.initialized()) {
                try {
                    LanguageEntity entity = this.dataHandler.load(event.getPlayer().getUniqueId().toString()).get(30, TimeUnit.SECONDS);
                    LanguageAPI.addEntity(entity);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    this.logger.severe("Failed to load language entity for " + event.getPlayer().getUniqueId() + "!");
                    task.resume();
                    throw new RuntimeException(e);
                }
                String languages = Configuration.LANGUAGES.getValue().stream().reduce((a, b) -> a + ", " + b).orElse("none");
                event.getPlayer().sendMessage(
                        Messages.PREFIX.value().append(
                                Messages.LANGUAGE_JOIN.value(
                                        languageAPI.getEntity(event.getPlayer()),
                                        Placeholder.contextual("language", LanguageEntity.class, LanguageEntity::language, "-"),
                                        Placeholder.constant("languages", languages)
                                )
                        )
                );
                task.resume();
            }
        });
    }

    @Subscribe
    public void onAsyncDisconnect(DisconnectEvent event) {
        if (!this.dataHandler.initialized()) return;
        if (languageAPI.getEntity(event.getPlayer()) == null) return;
        this.dataHandler.save(languageAPI.getEntity(event.getPlayer()));
        LanguageAPI.removeEntity(event.getPlayer().getUniqueId());
    }

    /**
     * Sends a plugin message to the current server of the player.
     * The message contains the player's UUID and the locale.
     * The message will be received by the plugin on the server.
     *
     * @param player the player
     * @param locale the locale
     */
    public void updateSubServers(Player player, Locale locale) {

        if (player.getCurrentServer().isPresent()) {
            ServerConnection server = player.getCurrentServer().get();
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Configuration.MESSAGE_IDENTIFIER.getValue());
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(locale.toLanguageTag());
            boolean sent = server.sendPluginMessage(OUTGOING, out.toByteArray());
            this.logger.info("Sent plugin message " + OUTGOING.getId() + " for " + player.getUniqueId() + " with content " + locale.toLanguageTag() + ": " + sent);
        } else {
            this.logger.warning("Tried to send message but player " + player.getUniqueId() + " is not connected to a server!");
        }
    }

    @Override
    public void execute(Invocation invocation) {
        if (!this.dataHandler.initialized()) return;
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player player)) {
            return;
        }

        if (args.length >= 1) {

            Locale locale = Locale.forLanguageTag(args[0]);
            if (Configuration.LANGUAGES.getValue().contains(locale.toLanguageTag())) {
                LanguageEntity entity = languageAPI.getEntity(player);
                entity.setLanguage(locale);
                updateSubServers(player, locale);
                this.dataHandler.save(entity);
                player.sendMessage(
                        Messages.PREFIX.value().append(
                                Messages.LANGUAGE_CHANGED.value(
                                        languageAPI.getEntity(player),
                                        Placeholder.contextual("language", LanguageEntity.class, LanguageEntity::language, "error")
                                )
                        )
                );
                return;
            }
        }
        player.sendMessage(
                Messages.PREFIX.value().append(
                        Messages.LANGUAGE_NOT_FOUND.value(
                                languageAPI.getEntity(player),
                                Placeholder.constant("languages", Configuration.LANGUAGES.getValue().stream().reduce((a, b) -> a + ", " + b).orElse("none"))
                        )
                )
        );
    }

}
