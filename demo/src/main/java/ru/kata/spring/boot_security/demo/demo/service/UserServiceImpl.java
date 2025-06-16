package ru.kata.spring.boot_security.demo.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import ru.kata.spring.boot_security.demo.demo.model.Role;
import ru.kata.spring.boot_security.demo.demo.model.User;
import ru.kata.spring.boot_security.demo.demo.repositories.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Transactional
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Пользователь с  id "  + id + " не найден (это сообщение из сервиса)"
                ));
    }

    @Transactional
    @Override
    public User findByUsername(String name) {
        return userRepository.findByUsername(name).orElseThrow(() -> new IllegalStateException(
                "Пользователь с именем " + name + " не найден"
        ));
    }

    @Transactional
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public void saveUser(User user, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new IllegalStateException("Ошибка валидации данных пользователя: " +
                    bindingResult
                            .getAllErrors()
                            .stream()
                            .map(ObjectError::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
        }

        //проверка на уникальность имени
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalStateException("Ошибка, пользователь " + user.getUsername()
                    + " уже существует");
        }

        //Кодирование пароля
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
        } else {
            throw new IllegalStateException("Пароль не может быть пустым");
        }
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Пользователь с id " + id
                        + " не найден"));
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("Нельзя удалить администратора!");
        }
        userRepository.delete(user);
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

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User with id " + id + " not found"));

        //проверка на администратора
        boolean isAdmin = existingUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"))
                && existingUser.getUsername().equals("admin");


        if (isAdmin) {
            throw new IllegalStateException("Нельзя изменить администратора!");
        }

        //обновление данных пользователя
        existingUser.setUsername(user.getUsername());
        existingUser.setCountry(user.getCountry());
        existingUser.setCar(user.getCar());

        //Обновление пароля, если он не пустой и не равен null
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }

        //обновление ролей
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> roles = roleService.findRolesByIds(roleIds);

            if (roles.isEmpty()) {
                throw new IllegalStateException("Указанные роли не найдены!");
            }
            existingUser.setRoles(new HashSet<>(roles));
        }
        userRepository.save(existingUser);
    }
}

