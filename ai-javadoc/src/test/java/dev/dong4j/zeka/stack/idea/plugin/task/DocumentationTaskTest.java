package dev.dong4j.zeka.stack.idea.plugin.task;

import com.intellij.psi.PsiElement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * DocumentationTask 单元测试
 */
@DisplayName("DocumentationTask 单元测试")
public class DocumentationTaskTest {

    @Mock
    private PsiElement mockElement;

    private DocumentationTask task;
    private static final String TEST_CODE = "public void testMethod() { return 42; }";
    private static final String TEST_FILE_PATH = "/path/to/TestFile.java";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockElement.getText()).thenReturn(TEST_CODE);

        task = new DocumentationTask(
            mockElement,
            TEST_CODE,
            DocumentationTask.TaskType.METHOD,
            TEST_FILE_PATH
        );
    }

    @Test
    @DisplayName("测试任务创建")
    void testTaskCreation() {
        assertThat(task.getElement()).isEqualTo(mockElement);
        assertThat(task.getCode()).isEqualTo(TEST_CODE);
        assertThat(task.getType()).isEqualTo(DocumentationTask.TaskType.METHOD);
        assertThat(task.getFilePath()).isEqualTo(TEST_FILE_PATH);
        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.PENDING);
        assertThat(task.getResult()).isNull();
        assertThat(task.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("测试任务状态变更")
    void testStatusChange() {
        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.PENDING);

        task.setStatus(DocumentationTask.TaskStatus.PROCESSING);
        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.PROCESSING);

        task.setStatus(DocumentationTask.TaskStatus.COMPLETED);
        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.COMPLETED);
    }

    @Test
    @DisplayName("测试任务状态 - 失败")
    void testStatusFailed() {
        task.setStatus(DocumentationTask.TaskStatus.FAILED);
        task.setErrorMessage("API 调用失败");

        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).isEqualTo("API 调用失败");
    }

    @Test
    @DisplayName("测试任务状态 - 跳过")
    void testStatusSkipped() {
        task.setStatus(DocumentationTask.TaskStatus.SKIPPED);

        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.SKIPPED);
    }

    @Test
    @DisplayName("测试设置结果")
    void testSetResult() {
        String expectedResult = "/** 测试方法 JavaDoc */";
        task.setResult(expectedResult);

        assertThat(task.getResult()).isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("测试设置错误消息")
    void testSetErrorMessage() {
        String errorMessage = "网络连接失败";
        task.setErrorMessage(errorMessage);

        assertThat(task.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("测试任务类型 - CLASS")
    void testTaskTypeClass() {
        DocumentationTask classTask = new DocumentationTask(
            mockElement,
            "public class TestClass {}",
            DocumentationTask.TaskType.CLASS,
            TEST_FILE_PATH
        );

        assertThat(classTask.getType()).isEqualTo(DocumentationTask.TaskType.CLASS);
    }

    @Test
    @DisplayName("测试任务类型 - METHOD")
    void testTaskTypeMethod() {
        assertThat(task.getType()).isEqualTo(DocumentationTask.TaskType.METHOD);
    }

    @Test
    @DisplayName("测试任务类型 - TEST_METHOD")
    void testTaskTypeTestMethod() {
        DocumentationTask testMethodTask = new DocumentationTask(
            mockElement,
            "@Test public void testSomething() {}",
            DocumentationTask.TaskType.TEST_METHOD,
            TEST_FILE_PATH
        );

        assertThat(testMethodTask.getType()).isEqualTo(DocumentationTask.TaskType.TEST_METHOD);
    }

    @Test
    @DisplayName("测试任务类型 - FIELD")
    void testTaskTypeField() {
        DocumentationTask fieldTask = new DocumentationTask(
            mockElement,
            "private String username;",
            DocumentationTask.TaskType.FIELD,
            TEST_FILE_PATH
        );

        assertThat(fieldTask.getType()).isEqualTo(DocumentationTask.TaskType.FIELD);
    }

    @Test
    @DisplayName("测试任务类型 - INTERFACE")
    void testTaskTypeInterface() {
        DocumentationTask interfaceTask = new DocumentationTask(
            mockElement,
            "public interface UserService {}",
            DocumentationTask.TaskType.INTERFACE,
            TEST_FILE_PATH
        );

        assertThat(interfaceTask.getType()).isEqualTo(DocumentationTask.TaskType.INTERFACE);
    }

    @Test
    @DisplayName("测试任务类型 - ENUM")
    void testTaskTypeEnum() {
        DocumentationTask enumTask = new DocumentationTask(
            mockElement,
            "public enum Status { ACTIVE, INACTIVE }",
            DocumentationTask.TaskType.ENUM,
            TEST_FILE_PATH
        );

        assertThat(enumTask.getType()).isEqualTo(DocumentationTask.TaskType.ENUM);
    }

    @Test
    @DisplayName("测试获取元素显示名称 - 短代码")
    void testGetElementName_shortCode() {
        DocumentationTask shortTask = new DocumentationTask(
            mockElement,
            "short code",
            DocumentationTask.TaskType.METHOD,
            TEST_FILE_PATH
        );

        String elementName = shortTask.getElementName();
        assertThat(elementName).isEqualTo("short code...");
    }

    @Test
    @DisplayName("测试获取元素显示名称 - 长代码")
    void testGetElementName_longCode() {
        String longCode = "public void veryLongMethodNameThatExceedsFiftyCharactersForTesting() { return; }";
        when(mockElement.getText()).thenReturn(longCode);

        DocumentationTask longTask = new DocumentationTask(
            mockElement,
            longCode,
            DocumentationTask.TaskType.METHOD,
            TEST_FILE_PATH
        );

        String elementName = longTask.getElementName();
        assertThat(elementName).hasSize(53); // 50 + "..."
        assertThat(elementName).endsWith("...");
        assertThat(elementName).startsWith("public void very");
    }

    @Test
    @DisplayName("测试 toString 方法")
    void testToString() {
        String result = task.toString();

        assertThat(result).contains("DocumentationTask");
        assertThat(result).contains("type=METHOD");
        assertThat(result).contains("filePath='/path/to/TestFile.java'");
        assertThat(result).contains("status=PENDING");
    }

    @Test
    @DisplayName("测试完整的任务生命周期")
    void testCompleteTaskLifecycle() {
        // 1. 初始状态
        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.PENDING);
        assertThat(task.getResult()).isNull();
        assertThat(task.getErrorMessage()).isNull();

        // 2. 开始处理
        task.setStatus(DocumentationTask.TaskStatus.PROCESSING);
        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.PROCESSING);

        // 3. 完成处理
        String javadoc = "/** 测试方法文档 */";
        task.setResult(javadoc);
        task.setStatus(DocumentationTask.TaskStatus.COMPLETED);

        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.COMPLETED);
        assertThat(task.getResult()).isEqualTo(javadoc);
        assertThat(task.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("测试失败的任务生命周期")
    void testFailedTaskLifecycle() {
        // 1. 初始状态
        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.PENDING);

        // 2. 开始处理
        task.setStatus(DocumentationTask.TaskStatus.PROCESSING);

        // 3. 处理失败
        String errorMsg = "API 请求超时";
        task.setErrorMessage(errorMsg);
        task.setStatus(DocumentationTask.TaskStatus.FAILED);

        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).isEqualTo(errorMsg);
        assertThat(task.getResult()).isNull();
    }

    @Test
    @DisplayName("测试跳过的任务")
    void testSkippedTask() {
        task.setStatus(DocumentationTask.TaskStatus.SKIPPED);

        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.SKIPPED);
        assertThat(task.getResult()).isNull();
        assertThat(task.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("测试任务状态枚举的所有值")
    void testAllTaskStatusValues() {
        DocumentationTask.TaskStatus[] statuses = DocumentationTask.TaskStatus.values();

        assertThat(statuses).contains(
            DocumentationTask.TaskStatus.PENDING,
            DocumentationTask.TaskStatus.PROCESSING,
            DocumentationTask.TaskStatus.COMPLETED,
            DocumentationTask.TaskStatus.FAILED,
            DocumentationTask.TaskStatus.SKIPPED
                                     );
    }

    @Test
    @DisplayName("测试任务类型枚举的所有值")
    void testAllTaskTypeValues() {
        DocumentationTask.TaskType[] types = DocumentationTask.TaskType.values();

        assertThat(types).contains(
            DocumentationTask.TaskType.CLASS,
            DocumentationTask.TaskType.METHOD,
            DocumentationTask.TaskType.TEST_METHOD,
            DocumentationTask.TaskType.FIELD,
            DocumentationTask.TaskType.INTERFACE,
            DocumentationTask.TaskType.ENUM
                                  );
    }
}

