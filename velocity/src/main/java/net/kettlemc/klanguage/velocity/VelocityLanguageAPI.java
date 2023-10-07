package net.kettlemc.klanguage.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kettlemc.klanguage.api.LanguageAPI;
import net.kettlemc.klanguage.common.LanguageEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

public class VelocityLanguageAPI extends LanguageAPI<Player> {

    private final ProxyServer server;

    private VelocityLanguageAPI(ProxyServer server) {
        super();
        this.server = server;
    }

    public static VelocityLanguageAPI of(ProxyServer server) {
        return new VelocityLanguageAPI(server);
    }

    @Override
    public String getLanguage(@NotNull Player entity) {
        return getLanguage(entity.getUniqueId());
    }

    @Override
    public void setLanguage(@NotNull Player entity, @NotNull Locale language) {
        setLanguage(entity.getUniqueId(), language);
    }

    @Override
    public boolean shouldGc(@NotNull UUID uuid) {
        return server.getPlayer(uuid).isEmpty();
    }

    @Override
    public LanguageEntity getEntity(Player entity) {
        return getEntity(entity.getUniqueId());
    }
}
