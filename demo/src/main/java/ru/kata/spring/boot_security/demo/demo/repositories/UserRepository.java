package ru.kata.spring.boot_security.demo.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.validation.BindingResult;
import ru.kata.spring.boot_security.demo.demo.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository {
    User getUserById(Long id);

    User findByUsername(String name);

    List<User> getAllUsers();

    void saveUser(User user);

    void deleteUser(Long id);

    void updateUser(Long id, User user, BindingResult bindingResult, List<Long> roleIds);

    Map<String, Object> getUserWithRolesForEdit(long id);
}
