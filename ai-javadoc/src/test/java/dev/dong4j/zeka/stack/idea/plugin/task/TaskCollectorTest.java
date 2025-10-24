package dev.dong4j.zeka.stack.idea.plugin.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.javadoc.PsiDocComment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.List;

import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * TaskCollector 单元测试
 */
@DisplayName("TaskCollector 单元测试")
public class TaskCollectorTest {

    @Mock
    private Project mockProject;

    @Mock
    private PsiManager mockPsiManager;

    @Mock
    private PsiJavaFile mockPsiJavaFile;

    @Mock
    private PsiMethod mockMethod;

    @Mock
    private PsiMethod mockTestMethod;

    @Mock
    private PsiField mockField;

    @Mock
    private PsiClass mockClass;

    @Mock
    private PsiModifierList mockModifierList;

    @Mock
    private PsiAnnotation mockJUnit4Annotation;

    @Mock
    private PsiAnnotation mockJUnit5Annotation;

    @Mock
    private PsiDocComment mockDocComment;

    @Mock
    private VirtualFile mockVirtualFile;

    @Mock
    private VirtualFile mockDirectory;

    @Mock
    private SettingsState mockSettings;

    @Mock
    private com.intellij.openapi.application.Application mockApplication;

    private TaskCollector taskCollector;
    private SettingsState realSettings;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 创建真实的 settings 用于测试
        realSettings = new SettingsState();
        realSettings.generateForClass = true;
        realSettings.generateForMethod = true;
        realSettings.generateForField = true;
        realSettings.skipExisting = false;

        // Mock Application 和 SettingsState
        try (MockedStatic<ApplicationManager> mockedAppManager = mockStatic(ApplicationManager.class);
             MockedStatic<SettingsState> mockedSettings = mockStatic(SettingsState.class)) {

            mockedAppManager.when(ApplicationManager::getApplication).thenReturn(mockApplication);
            mockedSettings.when(SettingsState::getInstance).thenReturn(realSettings);

            taskCollector = new TaskCollector(mockProject);
        }

        // 基本的 mock 设置
        when(mockMethod.getText()).thenReturn("public void testMethod() {}");
        when(mockMethod.getName()).thenReturn("testMethod");
        when(mockMethod.getModifierList()).thenReturn(mockModifierList);
        when(mockMethod.getContainingFile()).thenReturn(mockPsiJavaFile);

        when(mockTestMethod.getText()).thenReturn("@Test public void testSomething() {}");
        when(mockTestMethod.getName()).thenReturn("testSomething");
        when(mockTestMethod.getModifierList()).thenReturn(mockModifierList);
        when(mockTestMethod.getContainingFile()).thenReturn(mockPsiJavaFile);

        when(mockField.getText()).thenReturn("private String username;");
        when(mockField.getName()).thenReturn("username");
        when(mockField.getContainingFile()).thenReturn(mockPsiJavaFile);

        when(mockClass.getText()).thenReturn("public class TestClass {}");
        when(mockClass.getName()).thenReturn("TestClass");
        when(mockClass.getMethods()).thenReturn(new PsiMethod[0]);
        when(mockClass.getFields()).thenReturn(new PsiField[0]);
        when(mockClass.getInnerClasses()).thenReturn(new PsiClass[0]);
        when(mockClass.getContainingFile()).thenReturn(mockPsiJavaFile);

        when(mockPsiJavaFile.getVirtualFile()).thenReturn(mockVirtualFile);
        when(mockVirtualFile.getPath()).thenReturn("/test/path/TestFile.java");
    }

    @Test
    @DisplayName("测试从单个方法收集任务")
    void testCollectFromElement_method() {
        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockMethod);

        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        assertThat(task.getType()).isEqualTo(DocumentationTask.TaskType.METHOD);
        assertThat(task.getElement()).isEqualTo(mockMethod);
    }

    @Test
    @DisplayName("测试从测试方法收集任务 - JUnit 4")
    void testCollectFromElement_testMethod_JUnit4() {
        when(mockModifierList.findAnnotation("org.junit.Test")).thenReturn(mockJUnit4Annotation);
        when(mockModifierList.findAnnotation("org.junit.jupiter.api.Test")).thenReturn(null);

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockTestMethod);

        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        assertThat(task.getType()).isEqualTo(DocumentationTask.TaskType.TEST_METHOD);
    }

    @Test
    @DisplayName("测试从测试方法收集任务 - JUnit 5")
    void testCollectFromElement_testMethod_JUnit5() {
        when(mockModifierList.findAnnotation("org.junit.Test")).thenReturn(null);
        when(mockModifierList.findAnnotation("org.junit.jupiter.api.Test")).thenReturn(mockJUnit5Annotation);

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockTestMethod);

        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        assertThat(task.getType()).isEqualTo(DocumentationTask.TaskType.TEST_METHOD);
    }

    @Test
    @DisplayName("测试从单个字段收集任务")
    void testCollectFromElement_field() {
        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockField);

        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        assertThat(task.getType()).isEqualTo(DocumentationTask.TaskType.FIELD);
        assertThat(task.getElement()).isEqualTo(mockField);
    }

    @Test
    @DisplayName("测试从类收集任务 - 空类")
    void testCollectFromElement_emptyClass() {
        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockClass);

        // 应该只为类本身生成一个任务
        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        assertThat(task.getType()).isEqualTo(DocumentationTask.TaskType.CLASS);
    }

    @Test
    @DisplayName("测试从类收集任务 - 包含方法和字段")
    void testCollectFromElement_classWithMembers() {
        when(mockClass.getMethods()).thenReturn(new PsiMethod[] {mockMethod});
        when(mockClass.getFields()).thenReturn(new PsiField[] {mockField});

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockClass);

        // 应该为类、方法和字段各生成一个任务
        assertThat(tasks).hasSize(3);
        assertThat(tasks).anyMatch(t -> t.getType() == DocumentationTask.TaskType.CLASS);
        assertThat(tasks).anyMatch(t -> t.getType() == DocumentationTask.TaskType.METHOD);
        assertThat(tasks).anyMatch(t -> t.getType() == DocumentationTask.TaskType.FIELD);
    }

    @Test
    @DisplayName("测试跳过已有文档的元素")
    void testCollectFromElement_skipExisting() {
        realSettings.skipExisting = true;

        // Mock 方法已有文档
        PsiDocCommentOwner methodWithDoc = mock(PsiDocCommentOwner.class, withSettings()
            .extraInterfaces(PsiMethod.class));
        when(methodWithDoc.getDocComment()).thenReturn(mockDocComment);
        when(methodWithDoc.getText()).thenReturn("public void test() {}");
        when(((PsiMethod) methodWithDoc).getModifierList()).thenReturn(mockModifierList);
        when(methodWithDoc.getContainingFile()).thenReturn(mockPsiJavaFile);

        List<DocumentationTask> tasks = taskCollector.collectFromElement(methodWithDoc);

        // 应该跳过已有文档的方法
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("测试不跳过已有文档的元素")
    void testCollectFromElement_notSkipExisting() {
        realSettings.skipExisting = false;

        // Mock 方法已有文档
        PsiDocCommentOwner methodWithDoc = mock(PsiDocCommentOwner.class, withSettings()
            .extraInterfaces(PsiMethod.class));
        when(methodWithDoc.getDocComment()).thenReturn(mockDocComment);
        when(methodWithDoc.getText()).thenReturn("public void test() {}");
        when(((PsiMethod) methodWithDoc).getModifierList()).thenReturn(mockModifierList);
        when(methodWithDoc.getContainingFile()).thenReturn(mockPsiJavaFile);

        List<DocumentationTask> tasks = taskCollector.collectFromElement(methodWithDoc);

        // 不应该跳过
        assertThat(tasks).hasSize(1);
    }

    @Test
    @DisplayName("测试配置禁用某类型时不生成任务 - 方法")
    void testCollectFromElement_methodDisabled() {
        realSettings.generateForMethod = false;

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockMethod);

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("测试配置禁用某类型时不生成任务 - 字段")
    void testCollectFromElement_fieldDisabled() {
        realSettings.generateForField = false;

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockField);

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("测试配置禁用某类型时不生成任务 - 类")
    void testCollectFromElement_classDisabled() {
        realSettings.generateForClass = false;

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockClass);

        // 类本身被禁用，但可能有方法和字段
        assertThat(tasks).noneMatch(t -> t.getType() == DocumentationTask.TaskType.CLASS);
    }

    @Test
    @DisplayName("测试从文件收集任务 - 非 Java 文件")
    void testCollectFromFile_nonJavaFile() {
        PsiFile mockNonJavaFile = mock(PsiFile.class);

        List<DocumentationTask> tasks = taskCollector.collectFromFile(mockNonJavaFile);

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("测试从虚拟文件收集任务 - 文件不存在")
    void testCollectFromVirtualFile_fileNotFound() {
        when(mockPsiManager.findFile(mockVirtualFile)).thenReturn(null);

        try (MockedStatic<PsiManager> mockedPsiManager = mockStatic(PsiManager.class)) {
            mockedPsiManager.when(() -> PsiManager.getInstance(mockProject))
                .thenReturn(mockPsiManager);

            List<DocumentationTask> tasks = taskCollector.collectFromVirtualFile(mockVirtualFile);

            assertThat(tasks).isEmpty();
        }
    }

    @Test
    @DisplayName("测试从目录收集任务 - 空目录")
    void testCollectFromDirectory_emptyDirectory() {
        when(mockDirectory.isDirectory()).thenReturn(true);
        when(mockDirectory.getChildren()).thenReturn(new VirtualFile[0]);

        List<DocumentationTask> tasks = taskCollector.collectFromDirectory(mockDirectory);

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("测试从目录收集任务 - 非目录")
    void testCollectFromDirectory_notDirectory() {
        when(mockDirectory.isDirectory()).thenReturn(false);

        List<DocumentationTask> tasks = taskCollector.collectFromDirectory(mockDirectory);

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("测试任务包含正确的代码内容")
    void testTask_hasCorrectCode() {
        String expectedCode = "public void testMethod() {}";
        when(mockMethod.getText()).thenReturn(expectedCode);

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockMethod);

        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        assertThat(task.getCode()).isEqualTo(expectedCode);
    }

    @Test
    @DisplayName("测试任务包含正确的文件路径")
    void testTask_hasCorrectFilePath() {
        String expectedPath = "/test/path/TestFile.java";

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockMethod);

        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        assertThat(task.getFilePath()).isEqualTo(expectedPath);
    }

    @Test
    @DisplayName("测试任务初始状态")
    void testTask_initialStatus() {
        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockMethod);

        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        assertThat(task.getStatus()).isEqualTo(DocumentationTask.TaskStatus.PENDING);
    }

    @Test
    @DisplayName("测试从类收集任务 - 包含内部类")
    void testCollectFromElement_classWithInnerClass() {
        PsiClass mockInnerClass = mock(PsiClass.class);
        when(mockInnerClass.getText()).thenReturn("public static class InnerClass {}");
        when(mockInnerClass.getName()).thenReturn("InnerClass");
        when(mockInnerClass.getMethods()).thenReturn(new PsiMethod[0]);
        when(mockInnerClass.getFields()).thenReturn(new PsiField[0]);
        when(mockInnerClass.getInnerClasses()).thenReturn(new PsiClass[0]);
        when(mockInnerClass.getContainingFile()).thenReturn(mockPsiJavaFile);

        when(mockClass.getInnerClasses()).thenReturn(new PsiClass[] {mockInnerClass});

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockClass);

        // 应该为外部类和内部类各生成一个任务
        assertThat(tasks).hasSizeGreaterThanOrEqualTo(2);
        assertThat(tasks).anyMatch(t -> t.getElement() == mockClass);
        assertThat(tasks).anyMatch(t -> t.getElement() == mockInnerClass);
    }

    @Test
    @DisplayName("测试从 PsiFile 收集任务")
    void testCollectFromElement_psiFile() {
        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockPsiJavaFile);

        // 调用 collectFromFile
        verify(mockPsiJavaFile, never()).accept(any()); // 验证是否调用了 visitor
    }

    @Test
    @DisplayName("测试所有配置都禁用时不生成任务")
    void testCollectFromElement_allDisabled() {
        realSettings.generateForClass = false;
        realSettings.generateForMethod = false;
        realSettings.generateForField = false;

        when(mockClass.getMethods()).thenReturn(new PsiMethod[] {mockMethod});
        when(mockClass.getFields()).thenReturn(new PsiField[] {mockField});

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockClass);

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("测试识别测试方法 - 两种注解都存在")
    void testCollectFromElement_testMethod_bothAnnotations() {
        when(mockModifierList.findAnnotation("org.junit.Test")).thenReturn(mockJUnit4Annotation);
        when(mockModifierList.findAnnotation("org.junit.jupiter.api.Test")).thenReturn(mockJUnit5Annotation);

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockTestMethod);

        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        // 只要有一个测试注解就识别为测试方法
        assertThat(task.getType()).isEqualTo(DocumentationTask.TaskType.TEST_METHOD);
    }

    @Test
    @DisplayName("测试普通方法不被识别为测试方法")
    void testCollectFromElement_normalMethod_notTestMethod() {
        when(mockModifierList.findAnnotation("org.junit.Test")).thenReturn(null);
        when(mockModifierList.findAnnotation("org.junit.jupiter.api.Test")).thenReturn(null);

        List<DocumentationTask> tasks = taskCollector.collectFromElement(mockMethod);

        assertThat(tasks).hasSize(1);
        DocumentationTask task = tasks.get(0);
        assertThat(task.getType()).isEqualTo(DocumentationTask.TaskType.METHOD);
    }
}

