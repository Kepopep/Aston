package com.example;

import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class UserDaoImpl implements UserDao {

    private static final Logger logger =
            LoggerFactory.getLogger(UserDaoImpl.class);

    private final UserRepository userRepository;

    public UserDaoImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        return execute("save", () -> userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return execute("findById", () -> userRepository.findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return execute("findAll", () ->
                userRepository.findAllByOrderByIdAsc()
        );
    }

    @Override
    public User update(User user) {
        return execute("update", () -> userRepository.save(user));
    }

    @Override
    public boolean delete(Long id) {
        return execute("delete", () -> {
            if (!userRepository.existsById(id)) {
                return false;
            }

            userRepository.deleteById(id);
            return true;
        });
    }

    private <T> T execute(String opName, Operation<T> operation) {
        try {
            return operation.execute();
        } catch (DataIntegrityViolationException e) {
            logger.error("Нарушение ограничения БД: {}", opName, e);
            throw new DaoException(
                    "Нарушение ограничения БД: " + opName, e
            );
        } catch (DataAccessException | PersistenceException e) {
            logger.error("Ошибка при работе с БД: {}", opName, e);
            throw new DaoException(
                    "Ошибка при работе с БД: " + opName, e
            );
        }
    }

    @FunctionalInterface
    private interface Operation<T> {
        T execute();
    }
}
