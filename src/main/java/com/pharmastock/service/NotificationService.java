package com.pharmastock.service;

import com.pharmastock.model.Notification;
import java.util.List;

public interface NotificationService {
    List<Notification> getNotifications();
    int getUnreadCount();
    void markAsRead(String id);
    void markAllAsRead();
    void refreshNotifications();
}
