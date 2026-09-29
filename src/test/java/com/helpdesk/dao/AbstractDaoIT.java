package com.helpdesk.dao;


import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.helpdesk.config.DatabaseConfig;
import com.helpdesk.config.PersistenceBootstrap;
import com.helpdesk.persistence.TransactionManager;

/**
 * Un seul conteneur PostgreSQL pour toute la suite de tests (pattern "singleton container").
 * Les migrations Flyway réelles sont appliquées : on teste le vrai schéma de production.
 */
public abstract class AbstractDaoIT {

    protected static final TransactionManager tx;

    @SuppressWarnings("resource") // Volontaire : conteneur partagé, supprimé par Ryuk en fin de tests
    private static PostgreSQLContainer startContainer() {
        PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");
        postgres.start();
        return postgres;
    }

    static {
        PostgreSQLContainer postgres = startContainer();
        PersistenceBootstrap persistence = PersistenceBootstrap.start(new DatabaseConfig(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), 5));
        tx = persistence.transactionManager();
    }
    

    @BeforeEach
    void cleanDatabase() {
        tx.runInTransaction(() -> {
            tx.get().createNativeQuery("DELETE FROM tickets").executeUpdate();
            tx.get().createNativeQuery("DELETE FROM users").executeUpdate();
        });
    }
    
    
}