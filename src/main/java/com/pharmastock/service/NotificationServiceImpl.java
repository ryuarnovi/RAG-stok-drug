package com.pharmastock.service;

import com.pharmastock.model.Medicine;
import com.pharmastock.model.Notification;
import com.pharmastock.repository.IMedicineRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationServiceImpl implements NotificationService {

    private final IMedicineRepository medicineRepo;
    private final List<Notification> notifications = new ArrayList<>();
    private final Set<String> readNotificationIds = new HashSet<>();

    public NotificationServiceImpl(IMedicineRepository medicineRepo) {
        this.medicineRepo = medicineRepo;
        refreshNotifications();
    }

    @Override
    public synchronized List<Notification> getNotifications() {
        return new ArrayList<>(notifications);
    }

    @Override
    public synchronized int getUnreadCount() {
        int count = 0;
        for (Notification n : notifications) {
            if (!n.isRead()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public synchronized void markAsRead(String id) {
        for (Notification n : notifications) {
            if (n.getId().equals(id)) {
                n.setRead(true);
                readNotificationIds.add(id);
                break;
            }
        }
    }

    @Override
    public synchronized void markAllAsRead() {
        for (Notification n : notifications) {
            n.setRead(true);
            readNotificationIds.add(n.getId());
        }
    }

    @Override
    public synchronized void refreshNotifications() {
        notifications.clear();
        LocalDateTime now = LocalDateTime.now();

        // 1. Fetch Low Stock
        try {
            List<Medicine> lowStock = medicineRepo.findLowStock();
            for (Medicine m : lowStock) {
                String id = "low_" + m.getMedicineId() + "_" + m.getStockQuantity();
                String title = "Stok Kritis: " + m.getMedicineName();
                String message = "Sisa stok hanya " + m.getStockQuantity() + " " + m.getUnit() + " (Minimum: " + m.getMinimumStock() + ")";
                Notification n = new Notification(id, title, message, "WARNING", now);
                if (readNotificationIds.contains(id)) {
                    n.setRead(true);
                }
                notifications.add(n);
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat notifikasi low stock: " + e.getMessage());
        }

        // 2. Fetch Expired
        try {
            List<Medicine> expired = medicineRepo.findExpired();
            for (Medicine m : expired) {
                String id = "exp_" + m.getMedicineId();
                String title = "Obat Kedaluwarsa: " + m.getMedicineName();
                String message = "Obat sudah kedaluwarsa pada " + (m.getExpiryDate() != null ? m.getExpiryDate() : "tanggal yang tidak ditentukan");
                Notification n = new Notification(id, title, message, "DANGER", now);
                if (readNotificationIds.contains(id)) {
                    n.setRead(true);
                }
                notifications.add(n);
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat notifikasi kedaluwarsa: " + e.getMessage());
        }

        // 3. Fetch Near Expiry (30 hari)
        try {
            List<Medicine> nearExpiry = medicineRepo.findNearExpiry(30);
            for (Medicine m : nearExpiry) {
                // Jangan duplikasi jika sudah terdaftar di expired
                boolean alreadyExpired = false;
                for (Notification existing : notifications) {
                    if (existing.getId().equals("exp_" + m.getMedicineId())) {
                        alreadyExpired = true;
                        break;
                    }
                }
                if (alreadyExpired) continue;

                String id = "near_" + m.getMedicineId();
                String title = "Mendekati Kedaluwarsa: " + m.getMedicineName();
                String message = "Akan kedaluwarsa pada " + m.getExpiryDate() + " (Kurang dari 30 hari)";
                Notification n = new Notification(id, title, message, "WARNING", now);
                if (readNotificationIds.contains(id)) {
                    n.setRead(true);
                }
                notifications.add(n);
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat notifikasi hampir kadaluarsa: " + e.getMessage());
        }
    }
}
