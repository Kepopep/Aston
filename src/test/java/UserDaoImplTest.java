import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoImplTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:18.6"
    );

    static SessionFactory sessionFactory;

    static UserDaoImpl userDao;

    @BeforeAll
    public static void setupAll() {
        Configuration configuration = new Configuration().configure();
        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.hbm2ddl.auto", "create");
        sessionFactory = configuration.buildSessionFactory();

        userDao = new UserDaoImpl(sessionFactory);
    }

    @AfterAll
    public static void cleanUp() {
        if (sessionFactory != null) sessionFactory.close();
    }

    @BeforeEach
    public void clean() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createMutationQuery("""
                    DELETE FROM User
                """).executeUpdate();
            transaction.commit();
        }
    }

    @Test
    public void saveTest() {
        User test = new User("1", "2", 3);

        LocalDateTime before = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        User daoUser = userDao.save(test);
        LocalDateTime after = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).plus(1, ChronoUnit.MICROS);

        assertNotNull(daoUser.getId(), "Не присвоено id");

        try (Session session = sessionFactory.openSession()) {
            User sessionUser = session.get(User.class, daoUser.getId());
            assertNotNull(sessionUser, "Нету сущности в БД");
            assertEquals("1", sessionUser.getName(), "Неверное name");
            assertEquals("2", sessionUser.getEmail(), "Неверный email");
            assertEquals(3, sessionUser.getAge(), "Неверный age");

            assertNotNull(sessionUser.getCreatedAt(), "Не присвоено createdAt");
            assertTrue(!sessionUser.getCreatedAt().isBefore(before), "Неверный createdAt: раньше фактического создания");
            assertTrue(!sessionUser.getCreatedAt().isAfter(after), "Неверный createdAt: позже фактического создания");
        }
    }

    @Test
    public void findByIdTest() {
        User test = new User("1", "2", 3);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(test);
            transaction.commit();
        }

        User findUser = userDao.findById(test.getId()).orElse(null);

        assertNotNull(findUser);
        try (Session session = sessionFactory.openSession()) {
            User sessionUser = session.get(User.class, test.getId());
            assertEquals(sessionUser.getId(), findUser.getId(), "Неверный id");
            assertEquals(sessionUser.getName(), findUser.getName(), "Неверное name");
            assertEquals(sessionUser.getEmail(), findUser.getEmail(), "Неверный email");
            assertEquals(sessionUser.getAge(), findUser.getAge(), "Неверный age");
            assertEquals(sessionUser.getCreatedAt(), findUser.getCreatedAt(), "Неверный createdAt");
        }
    }

    @Test
    public void findNotExistTest() {
        User findUser = userDao.findById(-1L).orElse(null);

        assertNull(findUser, "Найдена сущность с несуществующим id");
    }

    @Test
    public void findAllTest() {
        User test = new User("1", "2", 3);
        User test1 = new User("2", "3", 4);
        User test2 = new User("3", "4", 5);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(test);
            session.persist(test1);
            session.persist(test2);
            transaction.commit();
        }

        List<User> userList = userDao.findAll();

        assertEquals(3, userList.size(), "Неверное количество");
        assertEquals(test.getId(), userList.getFirst().getId(), "Неверные сущности");
        assertEquals(test2.getId(), userList.getLast().getId(), "Неверные сущности");
        assertEquals(test1.getName(), userList.get(1).getName(), "Неверные сущности");
    }

    @Test
    public void updateTest() {
        User test = new User("1", "2", 3);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(test);
            transaction.commit();
        }

        test.setName("Name");
        test.setEmail("Email");
        test.setAge(299);

        userDao.update(test);

        try (Session session = sessionFactory.openSession()) {
            User sessionUser = session.get(User.class, test.getId());

            assertEquals("Name", sessionUser.getName(), "Неверный name");
            assertEquals("Email", sessionUser.getEmail(), "Неверный email");
            assertEquals(299, sessionUser.getAge(), "Неверный age");
        }
    }

    @Test
    public void deleteTest() {
        User test = new User("1", "2", 3);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(test);
            transaction.commit();
        }

        boolean result = userDao.delete(test.getId());

        assertEquals(true, result, "Сущность не удалена");

        try (Session session = sessionFactory.openSession()) {
            User sessionUser = session.get(User.class, test.getId());

            assertNull(sessionUser, "Сущность не удалена");
        }
    }
}
