package com.project.notification.provider;

public interface NotificationProvider {

    void send(String userId, String message);
}
