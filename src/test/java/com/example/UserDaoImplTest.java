package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserDaoImpl.class)
@Testcontainers
class UserDaoImplTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:18.6"
    );

    @Autowired
    TestEntityManager em;

    @Autowired
    UserDaoImpl userDao;

    @Test
    public void saveTest() {
        User test = new User("1", "2", 3);

        LocalDateTime before = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        User daoUser = userDao.save(test);
        em.flush();
        LocalDateTime after = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).plus(1, ChronoUnit.MICROS);
        em.clear();

        assertNotNull(daoUser.getId(), "Не присвоено id");

        User dbUser = em.find(User.class, daoUser.getId());
        assertNotNull(dbUser, "Нету сущности в БД");
        assertEquals("1", dbUser.getName(), "Неверное name");
        assertEquals("2", dbUser.getEmail(), "Неверный email");
        assertEquals(3, dbUser.getAge(), "Неверный age");

        assertNotNull(dbUser.getCreatedAt(), "Не присвоено createdAt");
        assertTrue(!dbUser.getCreatedAt().isBefore(before), "Неверный createdAt: раньше фактического создания");
        assertTrue(!dbUser.getCreatedAt().isAfter(after), "Неверный createdAt: позже фактического создания");
    }

    @Test
    public void findByIdTest() {
        User test = em.persistFlushFind(new User("1", "2", 3));
        em.clear();

        User findUser = userDao.findById(test.getId()).orElse(null);

        assertNotNull(findUser);
        assertEquals(test.getId(), findUser.getId(), "Неверный id");
        assertEquals(test.getName(), findUser.getName(), "Неверное name");
        assertEquals(test.getEmail(), findUser.getEmail(), "Неверный email");
        assertEquals(test.getAge(), findUser.getAge(), "Неверный age");
        assertEquals(test.getCreatedAt(), findUser.getCreatedAt(), "Неверный createdAt");
    }

    @Test
    public void findNotExistTest() {
        User findUser = userDao.findById(-1L).orElse(null);

        assertNull(findUser, "Найдена сущность с несуществующим id");
    }

    @Test
    public void findAllTest() {
        User test = em.persist(new User("1", "2", 3));
        User test1 = em.persist(new User("2", "3", 4));
        User test2 = em.persist(new User("3", "4", 5));
        em.flush();
        em.clear();

        List<User> userList = userDao.findAll();

        assertEquals(3, userList.size(), "Неверное количество");
        assertEquals(test.getId(), userList.getFirst().getId(), "Неверные сущности");
        assertEquals(test2.getId(), userList.getLast().getId(), "Неверные сущности");
        assertEquals(test1.getName(), userList.get(1).getName(), "Неверные сущности");
    }

    @Test
    public void updateTest() {
        User test = em.persistAndFlush(new User("1", "2", 3));

        test.setName("Name");
        test.setEmail("Email");
        test.setAge(299);

        userDao.update(test);
        em.flush();
        em.clear();

        User dbUser = em.find(User.class, test.getId());
        assertEquals("Name", dbUser.getName(), "Неверный name");
        assertEquals("Email", dbUser.getEmail(), "Неверный email");
        assertEquals(299, dbUser.getAge(), "Неверный age");
    }

    @Test
    public void deleteTest() {
        User test = em.persistAndFlush(new User("1", "2", 3));
        em.clear();

        boolean result = userDao.delete(test.getId());
        em.flush();
        em.clear();

        assertTrue(result, "Сущность не удалена");
        assertNull(em.find(User.class, test.getId()), "Сущность не удалена");
    }
}
