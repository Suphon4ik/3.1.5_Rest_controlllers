package ru.kata.spring.boot_security.demo.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.demo.model.Role;
import ru.kata.spring.boot_security.demo.demo.model.User;
import ru.kata.spring.boot_security.demo.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.demo.service.UserService;
import java.security.Principal;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final RoleService roleService;

    public AdminRestController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public List<User> showAllUser() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user); // 200 OK + JSON
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user, BindingResult bindingResult) {
        userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);

    }

    @GetMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEditUserForm(@PathVariable("id") long id) {

        return ResponseEntity.ok(userService.getUserWithRolesForEdit(id));
    }

    // Обновить пользователя
    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable("id") long id,
                                        @RequestBody User user, // Получаем данные пользователя, включая роли
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Ошибка валидации данных пользователя");
        }
        try {
            userService.updateUser(id, user,bindingResult , user.getRolesIds()); // Передаем роли, полученные из тела запроса
            return ResponseEntity.ok("Пользователь успешно обновлён!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Пользователь успешно удалён!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

}