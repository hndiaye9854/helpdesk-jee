package config;


import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {

    public static final String PERSISTENCE_ATTRIBUTE = "helpdesk.persistence";
    private static final Logger log = LoggerFactory.getLogger(AppStartupListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        PersistenceBootstrap persistence = PersistenceBootstrap.start(DatabaseConfig.fromEnvironment());
        sce.getServletContext().setAttribute(PERSISTENCE_ATTRIBUTE, persistence);
        log.info("HelpDesk démarré");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Object attr = sce.getServletContext().getAttribute(PERSISTENCE_ATTRIBUTE);
        if (attr instanceof PersistenceBootstrap persistence) {
            persistence.close();
        }
        deregisterJdbcDrivers();
    }

    /** Évite les fuites mémoire lors d'un redéploiement à chaud sous Tomcat. */
    private void deregisterJdbcDrivers() {
        ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
        for (Driver driver : Collections.list(DriverManager.getDrivers())) {
            if (driver.getClass().getClassLoader() == webappClassLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    log.warn("Impossible de désenregistrer le driver {}", driver, e);
                }
            }
        }
    }
}