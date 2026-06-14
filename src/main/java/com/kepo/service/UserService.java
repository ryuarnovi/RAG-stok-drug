package com.kepo.service;

import com.kepo.model.AuditLog;
import com.kepo.model.User;
import com.kepo.repository.AuditLogRepository;
import com.kepo.repository.UserRepository;
import com.kepo.util.PasswordUtil;

import java.util.List;

public class UserService {

    private final UserRepository userRepo;
    private final AuditLogRepository auditRepo;
    private User currentUser;

    public UserService(UserRepository userRepo, AuditLogRepository auditRepo) {
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
    }

    public User login(String username, String password) {
        User user = userRepo.findByUsername(username);
        if (user != null && PasswordUtil.verify(password, user.getPasswordHash())) {
            currentUser = user;
            logActivity(username, "LOGIN", "Pengguna berhasil masuk ke sistem.");
            return user;
        }
        logActivity(username, "LOGIN_FAILED", "Gagal masuk ke sistem: password salah atau username tidak ditemukan.");
        return null;
    }

    public void logout() {
        if (currentUser != null) {
            logActivity(currentUser.getUsername(), "LOGOUT", "Pengguna keluar dari sistem.");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logActivity(String username, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUsername(username != null ? username : "SYSTEM");
        log.setAction(action);
        log.setDetails(details);
        auditRepo.save(log);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public boolean saveUser(User u) {
        boolean res = userRepo.save(u);
        if (res && currentUser != null) {
            logActivity(currentUser.getUsername(), "SAVE_USER", "Menyimpan data user: " + u.getUsername());
        }
        return res;
    }

    public boolean deleteUser(int userId) {
        if (currentUser != null) {
            logActivity(currentUser.getUsername(), "DELETE_USER", "Menghapus user ID: " + userId);
        }
        return userRepo.delete(userId);
    }

    public List<AuditLog> getAuditLogs() {
        return auditRepo.findAll();
    }
}
