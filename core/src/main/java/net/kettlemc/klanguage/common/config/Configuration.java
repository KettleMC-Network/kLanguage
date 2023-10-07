package net.kettlemc.klanguage.common.config;

import io.github.almightysatan.jaskl.*;
import io.github.almightysatan.jaskl.entries.IntegerConfigEntry;
import io.github.almightysatan.jaskl.entries.ListConfigEntry;
import io.github.almightysatan.jaskl.entries.LongConfigEntry;
import io.github.almightysatan.jaskl.entries.StringConfigEntry;
import io.github.almightysatan.jaskl.hocon.HoconConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Configuration {

    public static final Path CONFIG_DIRECTORY = Paths.get("plugins", "kLanguage");
    private static final Config CONFIG = HoconConfig.of(CONFIG_DIRECTORY.resolve("settings.conf").toFile(), "Config for SQL and other settings");

    public static final StringConfigEntry DEFAULT_LANGUAGE = StringConfigEntry.of(CONFIG, "settings.default-language", "The tag of the default language to use", "de");
    public static final StringConfigEntry MESSAGE_NAMESPACE  = StringConfigEntry.of(CONFIG, "settings.message-namespace", "The namespace for plugin messages", "klang");
    public static final StringConfigEntry MESSAGE_IDENTIFIER = StringConfigEntry.of(CONFIG, "settings.message-identifier", "The identifier for plugin messages", "switch");

    public static final IntegerConfigEntry SECONDS_BETWEEN_GC = IntegerConfigEntry.of(CONFIG, "settings.gc.seconds-between", "The seconds between garbage collection", 60);

    public static final StringConfigEntry MYSQL_HOST = StringConfigEntry.of(CONFIG, "settings.sql.host", "The MYSQL host", "host");
    public static final LongConfigEntry MYSQL_PORT = LongConfigEntry.of(CONFIG, "settings.sql.port", "The MYSQL port", 3306);
    public static final StringConfigEntry MYSQL_DATABASE = StringConfigEntry.of(CONFIG, "settings.sql.database", "The MYSQL database", "database");
    public static final StringConfigEntry MYSQL_USER = StringConfigEntry.of(CONFIG, "settings.sql.user", "The MYSQL user", "user");
    public static final StringConfigEntry MYSQL_PASSWORD = StringConfigEntry.of(CONFIG, "settings.sql.password", "The MYSQL password", "password");
    public static final ListConfigEntry<String> LANGUAGES = ListConfigEntry.of(CONFIG, "settings.languages", "The languages to load", Arrays.asList("de", "en"), Type.STRING);

    private Configuration() {
    }

    public static boolean load() {
        try {
            CONFIG.load();
            CONFIG.write();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void unload() {
        CONFIG.close();
    }

    public static boolean write() {
        try {
            CONFIG.write();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
