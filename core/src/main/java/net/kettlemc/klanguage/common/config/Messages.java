package net.kettlemc.klanguage.common.config;

import io.github.almightysatan.slams.Slams;
import io.github.almightysatan.slams.minimessage.AdventureMessage;
import io.github.almightysatan.slams.parser.JacksonParser;
import net.kettlemc.klanguage.common.Util;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

public class Messages {

    private static final Path LANGUAGE_PATH = Paths.get("plugins", "kLanguage", "languages");
    private static final Slams LANGUAGE_MANAGER = Slams.create(getDefaultLocale());

    public static final AdventureMessage PREFIX = AdventureMessage.of("messages.common.prefix", LANGUAGE_MANAGER);
    public static final AdventureMessage LANGUAGE_JOIN = AdventureMessage.of("messages.join.selected", LANGUAGE_MANAGER);
    public static final AdventureMessage LANGUAGE_CHANGED = AdventureMessage.of("messages.language.changed", LANGUAGE_MANAGER);
    public static final AdventureMessage LANGUAGE_NOT_FOUND = AdventureMessage.of("messages.language.invalid", LANGUAGE_MANAGER);

    private Messages() {
    }

    private static String getDefaultLocale() {
        return Locale.forLanguageTag(Configuration.DEFAULT_LANGUAGE.getValue()).toLanguageTag();
    }

    public static boolean load() {
        if (!LANGUAGE_PATH.toFile().exists()) LANGUAGE_PATH.toFile().mkdirs();
        Configuration.LANGUAGES.getValue().forEach(lang -> Util.saveResourceAsFile(Messages.class, "lang/" + lang + ".json", LANGUAGE_PATH.resolve(lang + ".json")));
        try {
            loadFromFilesInDirectory(LANGUAGE_PATH.toFile());
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    private static void loadFromFilesInDirectory(@NotNull File directory) throws IOException {
        if (!directory.isDirectory()) return;
        for (File file : Objects.requireNonNull(LANGUAGE_PATH.toFile().listFiles())) {
            if (file.isDirectory()) loadFromFilesInDirectory(file);
            else if (file.getName().endsWith(".json"))
                LANGUAGE_MANAGER.load(file.getName().replace(".json", ""), JacksonParser.createJsonParser(file));
        }
    }



}
