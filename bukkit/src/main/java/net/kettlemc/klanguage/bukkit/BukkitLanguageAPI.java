package net.kettlemc.klanguage.bukkit;

import net.kettlemc.klanguage.api.LanguageAPI;
import net.kettlemc.klanguage.common.LanguageEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

public class BukkitLanguageAPI extends LanguageAPI<Player> {

    private BukkitLanguageAPI() {
        super();
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
        return Bukkit.getPlayer(uuid) == null;
    }

    @Override
    public LanguageEntity getEntity(Player entity) {
        return getEntity(entity.getUniqueId());
    }

    public static BukkitLanguageAPI of() {
        return new BukkitLanguageAPI();
    }
}
