import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$");

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User save(
            String name,
            String email,
            Integer age
    ) {
        String normalizedName = normalizeName(name);
        validateName(normalizedName);
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        validateAge(age);

        User saved = userDao.save(new User(normalizedName, normalizedEmail, age));
        logger.info("Создан пользователь id={}", saved.getId());
        return saved;
    }

    public User findById(Long id) {
        validateId(id);
        return userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }


    public List<User> findAll() {
        return userDao.findAll();
    }

    public User update(
            Long id,
            String name,
            String email,
            Integer age
    ) {
        User user = findById(id);

        String normalizedName = normalizeName(name);
        if (normalizedName != null && !normalizedName.isEmpty()) {
            validateName(normalizedName);
            user.setName(normalizedName);
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail != null && !normalizedEmail.isEmpty()) {
            validateEmail(normalizedEmail);
            user.setEmail(normalizedEmail);
        }

        if (age != null) {
            validateAge(age);
            user.setAge(age);
        }

        User updated = userDao.update(user);
        logger.info("Обновлён пользователь id={}", id);
        return updated;
    }

    public void delete(Long id) {
        validateId(id);
        if (!userDao.delete(id)) {
            throw new UserNotFoundException(id);
        }
        logger.info("Пользователь удалён id={}", id);
    }


    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Некорректный id, должен быть > 0");
        }
    }

    private void validateName(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Некорректный name");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Некорректный email");
        }
    }

    private void validateAge(Integer age) {
        if (age != null && (age < 0 || age > 150)) {
            throw new IllegalArgumentException("Некорректный age, должен быть [0, 150]");
        }
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
