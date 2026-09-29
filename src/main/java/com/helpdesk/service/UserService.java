package com.helpdesk.service;

import java.util.List;

import com.helpdesk.model.User;

public interface UserService {
    User create(User user);
    User update(User user);
    void delete(Long id);
    User findById(Long id);
    List<User> findAll();
    List<User> findTechnicians();
}