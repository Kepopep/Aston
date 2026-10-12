import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory factory = build();

    private static SessionFactory build() {
       try {
           Configuration configuration =  new Configuration().configure();
           configuration.setProperty("hibernate.connection.username", requireEnv("DB_USERNAME"));
           configuration.setProperty("hibernate.connection.password", requireEnv("DB_PASSWORD"));
           return configuration.buildSessionFactory();
       } catch (HibernateException e) {
           logger.error(e.getLocalizedMessage());
           throw new ExceptionInInitializerError(e);
       }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задана переменная окружения " + name);
        }
        return value;
    }

    public static SessionFactory getSessionFactory() { return factory; }
    public static void shutdown() { factory.close(); }
}
