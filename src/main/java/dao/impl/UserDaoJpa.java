package dao.impl;

import java.util.List;
import java.util.Optional;

import dao.UserDao;
import model.User;
import model.enums.Role;
import persistence.EntityManagerProvider;

public class UserDaoJpa extends AbstractJpaDao<User, Long> implements UserDao {

    public UserDaoJpa(EntityManagerProvider emProvider) {
        super(User.class, emProvider);
    }

    @Override
    public List<User> findAll() {
        return em().createQuery("SELECT u FROM User u ORDER BY u.lastName, u.firstName", User.class)
                .getResultList();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return em().createQuery("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<User> findByRole(Role role) {
        return em().createQuery(
                        "SELECT u FROM User u WHERE u.role = :role ORDER BY u.lastName, u.firstName", User.class)
                .setParameter("role", role)
                .getResultList();
    }
}