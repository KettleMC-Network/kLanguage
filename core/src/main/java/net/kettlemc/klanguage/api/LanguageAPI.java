package net.kettlemc.klanguage.api;

import net.kettlemc.klanguage.common.LanguageEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

public abstract class LanguageAPI<T> implements ILanguageAPI<T> {

    public static final Logger LOGGER = Logger.getLogger("LanguageAPI");
    private static final HashMap<UUID, LanguageEntity> ENTITIES = new HashMap<>();

    public static void addEntity(LanguageEntity entity) {
        ENTITIES.put(UUID.fromString(entity.uuid()), entity);
    }

    public static void removeEntity(UUID uuid) {
        ENTITIES.remove(uuid);
    }

    @Override
    public void setLanguage(@NotNull UUID uuid, @NotNull Locale language) {
        LanguageEntity entity = getEntity(uuid);
        entity.setLanguage(language);
    }

    @Override
    public String getLanguage(@NotNull UUID uuid) {
        return ENTITIES.get(uuid).language();
    }

    public boolean gc() {
        return ENTITIES.values().removeIf(entity -> shouldGc(UUID.fromString(entity.uuid())));
    }

    public LanguageEntity getEntity(UUID uuid) {
        return ENTITIES.getOrDefault(uuid, null);
    }

    public abstract LanguageEntity getEntity(T entity);
}
