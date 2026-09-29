package dao;


import java.util.List;
import java.util.Optional;

import model.User;
import model.enums.Role;

public interface UserDao extends GenericDao<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
}