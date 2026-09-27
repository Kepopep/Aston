public class Main {
    public static void main(String[] args) {
        UserDao dao = new UserDaoImpl();
        UserService service = new UserService(dao);
        try {
            new ConsoleUI(service).run();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}