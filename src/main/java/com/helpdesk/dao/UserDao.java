package com.helpdesk.dao;


import java.util.List;
import java.util.Optional;

import com.helpdesk.model.User;
import com.helpdesk.model.enums.Role;

public interface UserDao extends GenericDao<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
}