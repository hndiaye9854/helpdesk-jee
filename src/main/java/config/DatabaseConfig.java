package config;


/**
 * Configuration de la base de données, lue depuis l'environnement (12-factor).
 * Priorité : variable d'environnement > propriété système (-D) > valeur par défaut (dev).
 */
public record DatabaseConfig(String url, String user, String password, int maxPoolSize) {

    public static DatabaseConfig fromEnvironment() {
        return new DatabaseConfig(
                read("DB_URL", "jdbc:postgresql://localhost:5432/helpdesk"),
                read("DB_USER", "helpdesk"),
                read("DB_PASSWORD", "helpdesk"),
                Integer.parseInt(read("DB_POOL_SIZE", "10")));
    }

    private static String read(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /** Ne jamais afficher un mot de passe dans les logs. */
    @Override
    public String toString() {
        return "DatabaseConfig[url=" + url + ", user=" + user
                + ", password=****, maxPoolSize=" + maxPoolSize + "]";
    }
}