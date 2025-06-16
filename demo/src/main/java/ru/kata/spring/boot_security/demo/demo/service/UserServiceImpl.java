package ru.kata.spring.boot_security.demo.demo.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import ru.kata.spring.boot_security.demo.demo.model.Role;
import ru.kata.spring.boot_security.demo.demo.model.User;
import ru.kata.spring.boot_security.demo.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.demo.repositories.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional
    @Override
    public User getUserById(Long id) {
        User user = userRepository.getUserById(id);
        if (user == null) {
            throw new IllegalStateException("User with id " + id + " not found");
        }
        return user;
    }

    @Transactional
    @Override
    public User findByUsername(String name) {
        User user = userRepository.findByUsername(name);
        if (user == null) {
            throw new IllegalStateException("User with name " + name + " not found");
        }
        return user;
    }

    @Transactional
    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Transactional
    @Override
    public void saveUser(User user) {

        //проверка на уникальность имени
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalStateException("Ошибка, пользователь " + user.getUsername()
                    + " уже существует");
        }

        //Кодирование пароля
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.saveUser(user);
        } else {
            throw new IllegalStateException("Пароль не может быть пустым");
        }
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.getUserById(id);
        if (user == null) {
            throw new IllegalStateException("User with id " + id + " not found");
        }
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("Нельзя удалить администратора!");
        }
        userRepository.deleteUser(id);
    }

    @Transactional
    @Override
    public void updateUser(Long id, User user, BindingResult bindingResult, List<Long> roleIds) {

        if (bindingResult.hasErrors()) {
            throw new IllegalStateException("Ошибка валидации данных пользователя: " +
                    bindingResult.getAllErrors()
                            .stream()
                            .map(ObjectError::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
        }

        User existingUser = userRepository.getUserById(id);
        if (existingUser != null) {
            return;
        }

        // Проверка на уникальность имени пользователя
        User userByUsername = userRepository.findByUsername(user.getUsername());
        if (userByUsername != null && !userByUsername.getId().equals(id)) {
            return;
        }

        //проверка на администратора
        boolean isAdmin = existingUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"))
                && existingUser.getUsername().equals("admin");


        if (isAdmin) {
            throw new IllegalStateException("Нельзя изменить администратора!");
        }

        // Копирование всех свойств из переданного объекта user в existingUser
        BeanUtils.copyProperties(user, existingUser, "id", "password", "roles");
        // Исключаем поля, которые обрабатываются отдельно

        //Обновление пароля, если он не пустой и не равен null
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }

        //обновление ролей
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findRolesByIds(roleIds);

            if (roles.isEmpty()) {
                throw new IllegalStateException("Указанные роли не найдены!");
            }
            existingUser.setRoles(new HashSet<>(roles));
        }
        userRepository.saveUser(user);
    }

    @Transactional
    @Override
    public Map<String, Object> getUserWithRolesForEdit(long id) {
        return userRepository.getUserWithRolesForEdit(id);
    }
}

