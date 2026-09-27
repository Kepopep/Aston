import jakarta.persistence.PersistenceException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);
    private static final SessionFactory factory = HibernateUtil.getSessionFactory();

    @Override
    public User save(User user) {
        return inTransaction(s -> {
            s.persist(user);
            return user;
        }, "save");
    }

    @Override
    public Optional<User> findById(Long id) {
        return inTransaction(s ->
                        Optional.ofNullable(s.get(User.class, id)),
                "findById");
    }

    @Override
    public List<User> findAll() {
        return inTransaction(s ->
                        s.createQuery("from User order by id", User.class).list(),
                "findAll"
        );
    }

    @Override
    public User update(User user) {
        return inTransaction(s ->
                        s.merge(user),
                "update");
    }

    @Override
    public boolean delete(Long id) {
        return inTransaction(s -> {
            User user = s.get(User.class, id);
            if (user == null) return false;
            s.remove(user);
            return true;
        }, "delete");
    }

    private <T> T inTransaction(Function<Session, T> action, String opName) {
        Transaction transaction = null;

        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            try {
                T result = action.apply(session);
                transaction.commit();
                return result;
            }
            catch (RuntimeException e) {
                rollback(transaction);
                throw e;
            }
        } catch (ConstraintViolationException e) {
            logger.error("Пользователь с таким email уже существует: {}", opName);
            throw new DaoException("Пользователь с таким email уже существует: " + opName, e);
        } catch (JDBCConnectionException e) {
            logger.error("База данных недоступна: {}", opName);
            throw new DaoException("База данных недоступна: " + opName, e);
        } catch (PersistenceException e) {
            logger.error("Ошибка при работе с БД: {}", opName);
            throw new DaoException("Ошибка при работе с БД: " + opName, e);
        }
    }


    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            try {
                transaction.rollback();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }
}
