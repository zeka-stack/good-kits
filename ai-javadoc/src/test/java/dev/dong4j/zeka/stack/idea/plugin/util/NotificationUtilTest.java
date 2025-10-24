package dev.dong4j.zeka.stack.idea.plugin.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NotificationUtil 单元测试
 */
@DisplayName("NotificationUtil 单元测试")
public class NotificationUtilTest {

    @Mock
    private Project mockProject;

    @Mock
    private NotificationGroupManager mockGroupManager;

    @Mock
    private NotificationGroup mockNotificationGroup;

    @Mock
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("测试通知信息 - Info")
    void testNotifyInfo() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyInfo(mockProject, "测试标题", "测试内容");

            verify(mockNotificationGroup).createNotification(
                eq("测试标题"),
                eq("测试内容"),
                eq(NotificationType.INFORMATION)
                                                            );
            verify(mockNotification).notify(mockProject);
        }
    }

    @Test
    @DisplayName("测试通知信息 - Warning")
    void testNotifyWarning() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyWarning(mockProject, "警告标题", "警告内容");

            verify(mockNotificationGroup).createNotification(
                eq("警告标题"),
                eq("警告内容"),
                eq(NotificationType.WARNING)
                                                            );
            verify(mockNotification).notify(mockProject);
        }
    }

    @Test
    @DisplayName("测试通知信息 - Error")
    void testNotifyError() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyError(mockProject, "错误标题", "错误内容");

            verify(mockNotificationGroup).createNotification(
                eq("错误标题"),
                eq("错误内容"),
                eq(NotificationType.ERROR)
                                                            );
            verify(mockNotification).notify(mockProject);
        }
    }

    @Test
    @DisplayName("测试完成通知 - 有失败")
    void testNotifyCompletion_withFailures() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyCompletion(mockProject, 5, 2, 1);

            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<NotificationType> typeCaptor = ArgumentCaptor.forClass(NotificationType.class);

            verify(mockNotificationGroup).createNotification(
                eq("JavaDoc 生成完成"),
                contentCaptor.capture(),
                typeCaptor.capture()
                                                            );

            String content = contentCaptor.getValue();
            assertThat(content).contains("完成: 5");
            assertThat(content).contains("失败: 2");
            assertThat(content).contains("跳过: 1");

            // 有失败时应该是 WARNING
            assertThat(typeCaptor.getValue()).isEqualTo(NotificationType.WARNING);
        }
    }

    @Test
    @DisplayName("测试完成通知 - 全部成功")
    void testNotifyCompletion_allSuccess() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyCompletion(mockProject, 10, 0, 2);

            ArgumentCaptor<NotificationType> typeCaptor = ArgumentCaptor.forClass(NotificationType.class);

            verify(mockNotificationGroup).createNotification(
                eq("JavaDoc 生成完成"),
                anyString(),
                typeCaptor.capture()
                                                            );

            // 没有失败时应该是 INFORMATION
            assertThat(typeCaptor.getValue()).isEqualTo(NotificationType.INFORMATION);
        }
    }

    @Test
    @DisplayName("测试完成通知 - 没有完成任何任务")
    void testNotifyCompletion_noCompleted() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyCompletion(mockProject, 0, 0, 5);

            ArgumentCaptor<NotificationType> typeCaptor = ArgumentCaptor.forClass(NotificationType.class);

            verify(mockNotificationGroup).createNotification(
                eq("JavaDoc 生成完成"),
                anyString(),
                typeCaptor.capture()
                                                            );

            // 没有完成任何任务时应该是 WARNING
            assertThat(typeCaptor.getValue()).isEqualTo(NotificationType.WARNING);
        }
    }

    @Test
    @DisplayName("测试目标完成通知")
    void testNotifyTargetCompletion() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyTargetCompletion(mockProject, "UserService.java", 3, 1, 0);

            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

            verify(mockNotificationGroup).createNotification(
                eq("JavaDoc 生成完成"),
                contentCaptor.capture(),
                any(NotificationType.class)
                                                            );

            String content = contentCaptor.getValue();
            assertThat(content).contains("UserService.java");
            assertThat(content).contains("完成: 3");
            assertThat(content).contains("失败: 1");
            assertThat(content).contains("跳过: 0");
        }
    }

    @Test
    @DisplayName("测试无任务通知")
    void testNotifyNoTask() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyNoTask(mockProject, "没有需要生成文档的元素");

            verify(mockNotificationGroup).createNotification(
                eq("AI Javadoc"),
                eq("没有需要生成文档的元素"),
                eq(NotificationType.INFORMATION)
                                                            );
        }
    }

    @Test
    @DisplayName("测试错误消息通知")
    void testNotifyErrorMessage() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyErrorMessage(mockProject, "API 调用失败");

            verify(mockNotificationGroup).createNotification(
                eq("AI Javadoc - 错误"),
                eq("API 调用失败"),
                eq(NotificationType.ERROR)
                                                            );
        }
    }

    @Test
    @DisplayName("测试索引中通知")
    void testNotifyIndexing() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            NotificationUtil.notifyIndexing(mockProject);

            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

            verify(mockNotificationGroup).createNotification(
                eq("AI Javadoc"),
                contentCaptor.capture(),
                eq(NotificationType.WARNING)
                                                            );

            String content = contentCaptor.getValue();
            assertThat(content).contains("不可用");
            assertThat(content).contains("索引中");
        }
    }

    @Test
    @DisplayName("测试通知可以传递 null project")
    void testNotify_withNullProject() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            // 传递 null project 应该不会抛出异常
            NotificationUtil.notifyInfo(null, "测试标题", "测试内容");

            verify(mockNotification).notify(null);
        }
    }

    @Test
    @DisplayName("测试目标完成通知 - 不同的状态组合")
    void testNotifyTargetCompletion_differentStatusCombinations() {
        try (MockedStatic<NotificationGroupManager> mockedStatic =
                 mockStatic(NotificationGroupManager.class)) {

            mockedStatic.when(NotificationGroupManager::getInstance)
                .thenReturn(mockGroupManager);
            when(mockGroupManager.getNotificationGroup(anyString()))
                .thenReturn(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            // 测试有失败的情况
            NotificationUtil.notifyTargetCompletion(mockProject, "Test1", 5, 2, 1);
            ArgumentCaptor<NotificationType> typeCaptor1 = ArgumentCaptor.forClass(NotificationType.class);
            verify(mockNotificationGroup).createNotification(anyString(), anyString(), typeCaptor1.capture());
            assertThat(typeCaptor1.getValue()).isEqualTo(NotificationType.WARNING);

            reset(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            // 测试全部成功的情况
            NotificationUtil.notifyTargetCompletion(mockProject, "Test2", 5, 0, 1);
            ArgumentCaptor<NotificationType> typeCaptor2 = ArgumentCaptor.forClass(NotificationType.class);
            verify(mockNotificationGroup).createNotification(anyString(), anyString(), typeCaptor2.capture());
            assertThat(typeCaptor2.getValue()).isEqualTo(NotificationType.INFORMATION);

            reset(mockNotificationGroup);
            when(mockNotificationGroup.createNotification(anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(mockNotification);

            // 测试没有完成任何任务的情况
            NotificationUtil.notifyTargetCompletion(mockProject, "Test3", 0, 0, 5);
            ArgumentCaptor<NotificationType> typeCaptor3 = ArgumentCaptor.forClass(NotificationType.class);
            verify(mockNotificationGroup).createNotification(anyString(), anyString(), typeCaptor3.capture());
            assertThat(typeCaptor3.getValue()).isEqualTo(NotificationType.WARNING);
        }
    }
}

