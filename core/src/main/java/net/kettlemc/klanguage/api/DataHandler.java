package net.kettlemc.klanguage.api;

import net.kettlemc.klanguage.common.LanguageEntity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public interface DataHandler {

    /**
     * Saves a language entity to the database
     *
     * @param entity the entity to save
     * @return a future containing the entity
     */
    Future<LanguageEntity> save(@NotNull LanguageEntity entity);

    /**
     * Loads a language entity from the database
     *
     * @param uuid the uuid of the entity
     * @return a future containing the entity
     */
    Future<LanguageEntity> load(@NotNull String uuid);

    /**
     * Initializes the data handler
     *
     * @return true if successful
     */
    boolean initialize();

    /**
     * Checks if the data handler is initialized
     *
     * @return true if initialized
     */
    boolean initialized();

    /**
     * Closes the data handler
     */
    void close();

}
