package ru.kata.spring.boot_security.demo.demo.service;

import org.springframework.validation.BindingResult;
import ru.kata.spring.boot_security.demo.demo.model.User;

import java.util.List;

public interface UserService {

    User getUserById(Long id);

    User findByUsername(String name);

    List<User> getAllUsers();

    void saveUser(User user, BindingResult bindingResult);

    void deleteUser(Long id);

    void updateUser(Long id, User user, BindingResult bindingResult, List<Long> roleIds);
}


