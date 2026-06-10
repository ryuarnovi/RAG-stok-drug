package com.pharmastock.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.pharmastock.model.User;
import com.pharmastock.repository.IUserRepository;

import java.util.Optional;

public class LoginController {

    private final IUserRepository userRepo;
    private User currentUser;

    public LoginController(IUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.isBlank()) {
            throw new AuthenticationException("Username tidak boleh kosong.");
        }
        if (password == null || password.isBlank()) {
            throw new AuthenticationException("Password tidak boleh kosong.");
        }

        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Username tidak ditemukan.");
        }

        User user = userOpt.get();
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash());

        if (!result.verified) {
            throw new AuthenticationException("Password salah.");
        }

        this.currentUser = user;
        return user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean changePassword(String oldPassword, String newPassword) throws AuthenticationException {
        if (currentUser == null) {
            throw new AuthenticationException("Belum login.");
        }
        BCrypt.Result result = BCrypt.verifyer().verify(oldPassword.toCharArray(), currentUser.getPasswordHash());
        if (!result.verified) {
            throw new AuthenticationException("Password lama salah.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new AuthenticationException("Password baru minimal 6 karakter.");
        }

        String newHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
        currentUser.setPasswordHash(newHash);
        return userRepo.update(currentUser);
    }

    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
