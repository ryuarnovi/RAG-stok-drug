package com.kepo.controller;

import com.kepo.model.User;
import com.kepo.service.UserService;

public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    public User login(String username, String password) throws Exception {
        if (username == null || username.isBlank()) {
            throw new Exception("Username tidak boleh kosong.");
        }
        if (password == null || password.isBlank()) {
            throw new Exception("Password tidak boleh kosong.");
        }

        User u = userService.login(username, password);
        if (u == null) {
            throw new Exception("Username tidak ditemukan atau password salah.");
        }
        return u;
    }

    public void logout() {
        userService.logout();
    }

    public User getCurrentUser() {
        return userService.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return userService.getCurrentUser() != null;
    }

    public boolean isAdmin() {
        User u = userService.getCurrentUser();
        return u != null && u.getRole() == User.Role.ADMIN;
    }
}
