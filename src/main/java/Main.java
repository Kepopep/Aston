import org.hibernate.SessionFactory;

public class Main {
    public static void main(String[] args) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        UserDao dao = new UserDaoImpl(sessionFactory);
        UserService service = new UserService(dao);
        try {
            new ConsoleUI(service).run();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}