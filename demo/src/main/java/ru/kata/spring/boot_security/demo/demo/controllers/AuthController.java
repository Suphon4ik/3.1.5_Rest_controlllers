
package ru.kata.spring.boot_security.demo.demo.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class AuthController {

    @GetMapping("/admin")
    public String adminPanel() {
        return "admin";
    }

    @GetMapping("/user")
    public String usernPage() {
        return "user";
    }




}





