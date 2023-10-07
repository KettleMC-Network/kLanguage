package net.kettlemc.klanguage.common.data;

import net.kettlemc.klanguage.api.DataHandler;
import net.kettlemc.klanguage.api.LanguageAPI;
import net.kettlemc.klanguage.common.LanguageEntity;
import net.kettlemc.klanguage.common.config.Configuration;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class HibernateDataHandler implements DataHandler {

    private SessionFactory sessionFactory;
    private DataThreadHandler dataThreadHandler;

    @Override
    public Future<LanguageEntity> save(@NotNull LanguageEntity entity) {
        if (!initialized()) {
            throw new IllegalStateException("HibernateDataHandler not initialized!");
        }


        CompletableFuture<LanguageEntity> future = new CompletableFuture<>();
        this.dataThreadHandler.queue(() -> {
            Session session = this.sessionFactory.openSession();
            session.beginTransaction();
            session.saveOrUpdate(entity);
            session.getTransaction().commit();
            session.close();
            future.complete(entity);
        });
        return future;
    }

    @Override
    public Future<LanguageEntity> load(@NotNull String uuid) {
        CompletableFuture<LanguageEntity> future = new CompletableFuture<>();
        this.dataThreadHandler.queue(() -> {
            Session session = this.sessionFactory.openSession();
            session.beginTransaction();
            LanguageEntity entity = session.get(LanguageEntity.class, uuid);
            if (entity == null) {
                entity = new LanguageEntity(uuid, Configuration.DEFAULT_LANGUAGE.getValue());
                session.saveOrUpdate(entity);
            }
            session.getTransaction().commit();
            session.close();
            future.complete(entity);
        });
        return future;
    }

    @Override
    public boolean initialized() {
        return sessionFactory != null && sessionFactory.isOpen();
    }

    @Override
    public boolean initialize() {
        try {
            this.sessionFactory = new org.hibernate.cfg.Configuration()
                    .setProperty("hibernate.connection.url", "jdbc:mysql://" + Configuration.MYSQL_HOST.getValue() + ":" + Configuration.MYSQL_PORT.getValue() + "/" + Configuration.MYSQL_DATABASE.getValue() + "?useSSL=false")
                    .setProperty("hibernate.connection.username", Configuration.MYSQL_USER.getValue())
                    .setProperty("hibernate.connection.password", Configuration.MYSQL_PASSWORD.getValue())
                    .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                    .setProperty("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect")
                    .setProperty("hibernate.show_sql", "false")
                    .setProperty("hibernate.hbm2ddl.auto", "update")
                    .setProperty("hibernate.connection.pool_size", "1")
                    .setProperty("hibernate.current_session_context_class", "thread")
                    .addAnnotatedClass(LanguageEntity.class)
                    .buildSessionFactory();
            this.dataThreadHandler = new DataThreadHandler();
            this.dataThreadHandler.init();
            return true;
        } catch (HibernateException | IllegalStateException throwable) {
            LanguageAPI.LOGGER.severe("Problem initializing Hibernate. See error below.");
            throwable.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        if (this.sessionFactory != null) {
            this.sessionFactory.close();
        }
        if (this.dataThreadHandler != null) {
            this.dataThreadHandler.shutdown();
        }
    }
}
