package ru.kata.spring.boot_security.demo.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api")
public class RestControler {

    private final UserService userService;
    private final RoleService roleService;


    @Autowired
    public RestControler(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/admin")
    public List<User> showAllUser() {
        return userService.getAllUsers();
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Пользователь с ID " + id + " не найден");
        }
        return ResponseEntity.ok(user); // 200 OK + JSON
    }

    @PostMapping("/admin/new")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        System.out.println("Received User: " + user);  // Логируем, что приходит в запросе
        userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);

    }

    @GetMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEditUserForm(@PathVariable("id") long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Пользователь с ID " + id + " не найден");
        }
        List<Role> roles = roleService.getAllRoles();

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("roles", roles);

        return ResponseEntity.ok(response);
    }

    // Обновить пользователя
    @PostMapping("/admin/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable("id") long id,
                                        @RequestBody User user, // Получаем данные пользователя, включая роли
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Ошибка валидации данных пользователя");
        }

        try {
            System.out.println("Updated roles: " + user.getRolesIds()); // Печатаем роли для отладки
            userService.updateUser(id, user, user.getRolesIds()); // Передаем роли, полученные из тела запроса
            return ResponseEntity.ok("Пользователь успешно обновлён!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        System.out.println("Deleting user with id: " + id);
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Пользователь успешно удалён!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

}