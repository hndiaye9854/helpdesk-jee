package com.helpdesk.config;


import java.util.Map;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helpdesk.persistence.TransactionManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Démarre la couche de persistance : pool de connexions → migrations Flyway → JPA.
 * Utilisé par l'application (listener) ET par les tests d'intégration.
 */
public final class PersistenceBootstrap implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PersistenceBootstrap.class);
    private static final String PERSISTENCE_UNIT = "helpdeskPU";

    private final HikariDataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    private final TransactionManager transactionManager;

    private PersistenceBootstrap(HikariDataSource dataSource, EntityManagerFactory emf) {
        this.dataSource = dataSource;
        this.entityManagerFactory = emf;
        this.transactionManager = new TransactionManager(emf);
    }

    public static PersistenceBootstrap start(DatabaseConfig config) {
        log.info("Initialisation de la persistance avec {}", config);
        HikariDataSource dataSource = createDataSource(config);
        try {
            migrate(dataSource);
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(
                    PERSISTENCE_UNIT,
                    Map.of("jakarta.persistence.nonJtaDataSource", dataSource));
            log.info("Persistance initialisée");
            return new PersistenceBootstrap(dataSource, emf);
        } catch (RuntimeException e) {
            dataSource.close();
            throw e;
        }
    }

    private static HikariDataSource createDataSource(DatabaseConfig config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("helpdesk-pool");
        hikari.setDriverClassName("org.postgresql.Driver");
        hikari.setJdbcUrl(config.url());
        hikari.setUsername(config.user());
        hikari.setPassword(config.password());
        hikari.setMaximumPoolSize(config.maxPoolSize());
        return new HikariDataSource(hikari);
    }

    private static void migrate(HikariDataSource dataSource) {
        var result = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        log.info("Flyway : {} migration(s) appliquée(s)", result.migrationsExecuted);
    }

    public TransactionManager transactionManager() {
        return transactionManager;
    }

    @Override
    public void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        dataSource.close();
        log.info("Persistance arrêtée");
    }
}