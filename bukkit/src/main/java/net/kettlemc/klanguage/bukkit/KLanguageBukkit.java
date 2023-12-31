package net.kettlemc.klanguage.bukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kettlemc.klanguage.api.DataHandler;
import net.kettlemc.klanguage.api.LanguageAPI;
import net.kettlemc.klanguage.bukkit.loading.Loadable;
import net.kettlemc.klanguage.common.LanguageEntity;
import net.kettlemc.klanguage.common.config.Configuration;
import net.kettlemc.klanguage.common.config.Messages;
import net.kettlemc.klanguage.common.data.HibernateDataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class KLanguageBukkit implements Loadable, Listener, PluginMessageListener {

    private final LanguageAPI<Player> languageAPI = BukkitLanguageAPI.of();
    private final DataHandler dataHandler = new HibernateDataHandler();
    private final JavaPlugin plugin;

    public KLanguageBukkit(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onEnable() {
        plugin.getLogger().info("Initializing on Bukkit...");

        if (!Configuration.load()) {
            plugin.getLogger().severe("Failed to load config!");
        }

        if (!Messages.load()) {
            plugin.getLogger().severe("Failed to load language config!");
        }

        if (!Configuration.MYSQL_HOST.getValue().equals(Configuration.MYSQL_HOST.getDefaultValue())) {
            this.dataHandler.initialize();
        } else {
            plugin.getLogger().warning("MySQL host is not set! Please set it in the config. Plugin will not be registered.");
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            return;
        }

        plugin.getLogger().info("Registering listeners...");
        Bukkit.getPluginManager().registerEvents(this, this.plugin);

        plugin.getLogger().info("Registering incoming plugin message channel...");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this.plugin, Configuration.MESSAGE_NAMESPACE.getValue() + ":" + Configuration.MESSAGE_IDENTIFIER.getValue(), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (languageAPI.gc()) {
                    plugin.getLogger().info("Removed unused entities from cache.");
                }
            }
        }.runTaskLater(this.plugin, Configuration.SECONDS_BETWEEN_GC.getValue() * 20L);
    }

    @Override
    public void onDisable() {
        plugin.getLogger().info("Unloading config...");
        Configuration.unload();
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equalsIgnoreCase(Configuration.MESSAGE_IDENTIFIER.getValue())) {
            String uuid = in.readUTF();
            String locale = in.readUTF();
            languageAPI.setLanguage(UUID.fromString(uuid), Locale.forLanguageTag(locale));
            plugin.getLogger().info("Received plugin message for " + player.getUniqueId().toString() + " with language-code " + locale + ".");
        }
    }

    @EventHandler
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        try {
            LanguageEntity entity = this.dataHandler.load(event.getUniqueId().toString()).get(30, TimeUnit.SECONDS);
            LanguageAPI.addEntity(entity);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            plugin.getLogger().severe("Failed to load language entity for " + event.getUniqueId() + "!");
            throw new RuntimeException(e);
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () ->
                Bukkit.getPlayer(event.getUniqueId()).sendMessage(languageAPI.getLanguage(event.getUniqueId())), 20L
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.dataHandler.save(languageAPI.getEntity(event.getPlayer()));
        if (languageAPI.getEntity(event.getPlayer()) == null) return;
        LanguageAPI.removeEntity(event.getPlayer().getUniqueId());
    }
}
