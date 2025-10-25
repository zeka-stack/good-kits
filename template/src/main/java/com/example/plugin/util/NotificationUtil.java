package com.example.plugin.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * 通知工具类
 */
public class NotificationUtil {

    public static final String NOTIFICATION_GROUP_ID = "Example Plugin Notifications";

    /**
     * 显示信息通知
     */
    public static void showInfo(Project project, String message) {
        showNotification(project, message, NotificationType.INFORMATION);
    }

    /**
     * 显示警告通知
     */
    public static void showWarning(Project project, String message) {
        showNotification(project, message, NotificationType.WARNING);
    }

    /**
     * 显示错误通知
     */
    public static void showError(Project project, String message) {
        showNotification(project, message, NotificationType.ERROR);
    }

    /**
     * 显示通知
     */
    private static void showNotification(Project project, String message, NotificationType type) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification("Example Plugin", message, type);
        
        notification.notify(project);
    }
}
