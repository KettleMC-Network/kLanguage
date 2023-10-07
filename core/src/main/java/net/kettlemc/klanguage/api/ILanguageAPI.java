package net.kettlemc.klanguage.api;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

public interface ILanguageAPI<T> {

    String getLanguage(@NotNull UUID uuid);

    String getLanguage(@NotNull T entity);

    void setLanguage(@NotNull UUID uuid, @NotNull Locale language);

    void setLanguage(@NotNull T entity, @NotNull Locale language);

    boolean shouldGc(@NotNull UUID entity);

}
