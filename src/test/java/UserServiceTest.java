import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserDao userDao;

    @InjectMocks
    UserService userService;

    @Test
    public void saveTest() {
        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.id = 1L;
            return user;
        });

        User saved = userService.save("1", "2", 3);

        assertEquals(1L, saved.getId(), "Неверный id");
        assertEquals("1", saved.getName(), "Неверное name");
        assertEquals("2", saved.getEmail(), "Неверный email");
        assertEquals(3, saved.getAge(), "Неверный age");
        verify(userDao).save(any(User.class));
    }

    @Test
    public void findByIdTest() {
        User test = new User("1", "2", 3);
        when(userDao.findById(1L)).thenReturn(Optional.of(test));

        User findUser = userService.findById(1L);

        assertSame(test, findUser, "Неверная сущность");
        verify(userDao).findById(1L);
    }

    @Test
    public void findNotExistTest() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(1L),
                "Найдена сущность с несуществующим id");
    }

    @Test
    public void findAllTest() {
        User test = new User("1", "2", 3);
        User test1 = new User("2", "3", 4);
        User test2 = new User("3", "4", 5);
        when(userDao.findAll()).thenReturn(List.of(test, test1, test2));

        List<User> userList = userService.findAll();

        assertEquals(List.of(test, test1, test2), userList, "Неверные сущности");
        verify(userDao).findAll();
    }

    @Test
    public void updateTest() {
        User test = new User("1", "2", 3);
        when(userDao.findById(1L)).thenReturn(Optional.of(test));
        when(userDao.update(test)).thenReturn(test);

        User updated = userService.update(1L, "Name", "email@mail.ru", 99);

        assertEquals("Name", updated.getName(), "Неверный name");
        assertEquals("email@mail.ru", updated.getEmail(), "Неверный email");
        assertEquals(99, updated.getAge(), "Неверный age");
        verify(userDao).update(test);
    }

    @Test
    public void deleteTest() {
        when(userDao.delete(1L)).thenReturn(true);

        assertDoesNotThrow(() -> userService.delete(1L), "Сущность не удалена");
        verify(userDao).delete(1L);
    }
}
