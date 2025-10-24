package dev.dong4j.zeka.stack.idea.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

import org.jetbrains.annotations.NotNull;

/**
 * IntelliJ Platform 测试基类
 * <p>
 * 提供通用的测试工具方法和配置，展示如何使用 IntelliJ Platform SDK 进行测试。
 * <p>
 * 主要功能：
 * <ul>
 *   <li>创建测试用的 PSI 文件</li>
 *   <li>读写操作的封装</li>
 *   <li>文档和 PSI 的同步</li>
 *   <li>通用的断言方法</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * public class MyTest extends BasePlatformTestCase {
 *     public void testSomething() {
 *         PsiJavaFile file = createJavaFile("Test.java", "public class Test {}");
 *         // 进行测试
 *     }
 * }
 * </pre>
 *
 * @author Cursor AI Assistant
 * @version 1.0
 */
public abstract class MyBasePlatformTestCase extends com.intellij.testFramework.fixtures.BasePlatformTestCase {

    /**
     * 创建 Java 测试文件
     * <p>
     * 这是 IntelliJ SDK 中创建测试文件的标准方法。
     *
     * @param fileName 文件名
     * @param content  文件内容
     * @return PSI Java 文件
     */
    protected PsiJavaFile createJavaFile(@NotNull String fileName, @NotNull String content) {
        // 使用 fixture 创建文件 - 这是 IntelliJ 测试框架提供的便捷方法
        // fixture 是测试框架提供的核心工具，包含了项目、编辑器、PSI 等所有测试需要的组件
        PsiFile file = myFixture.configureByText(fileName, content);

        if (!(file instanceof PsiJavaFile)) {
            throw new IllegalStateException("Created file is not a Java file");
        }

        return (PsiJavaFile) file;
    }

    /**
     * 在读操作中执行代码
     * <p>
     * IntelliJ Platform 的核心概念：所有 PSI 读取操作必须在 Read Action 中执行。
     * 这确保了 PSI 树在读取过程中不会被修改，保证线程安全。
     * <p>
     * 使用示例：
     * <pre>
     * String className = runReadAction(() -> psiClass.getName());
     * </pre>
     *
     * @param computation 要执行的计算
     * @param <T>         返回类型
     * @return 计算结果
     */
    protected <T> T runReadAction(@NotNull Computable<T> computation) {
        return ApplicationManager.getApplication().runReadAction(computation);
    }

    /**
     * 在写操作中执行代码
     * <p>
     * IntelliJ Platform 的核心概念：所有 PSI 修改操作必须在 Write Action 中执行。
     * Write Action 会锁定 PSI 树，确保没有并发修改。
     * <p>
     * 使用示例：
     * <pre>
     * runWriteAction(() -> {
     *     document.insertString(0, "// Comment\n");
     *     PsiDocumentManager.getInstance(getProject()).commitDocument(document);
     * });
     * </pre>
     *
     * @param runnable 要执行的操作
     */
    protected void runWriteAction(@NotNull Runnable runnable) {
        WriteCommandAction.runWriteCommandAction(getProject(), runnable);
    }

    /**
     * 获取文件对应的 Document
     * <p>
     * Document 是 IntelliJ 中文本文件的编辑器表示。
     * PSI 是代码的语义树表示。
     * 两者需要保持同步。
     *
     * @param file PSI 文件
     * @return Document 对象
     */
    protected Document getDocument(@NotNull PsiFile file) {
        return FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
    }

    /**
     * 提交 Document 变更到 PSI
     * <p>
     * 当修改 Document 后，需要调用此方法同步到 PSI 树。
     * 这是 IntelliJ Platform 的重要概念：Document 和 PSI 的同步。
     *
     * @param file PSI 文件
     */
    protected void commitDocument(@NotNull PsiFile file) {
        Document document = getDocument(file);
        PsiDocumentManager.getInstance(getProject()).commitDocument(document);
    }

    /**
     * 获取文件的文本内容
     *
     * @param file PSI 文件
     * @return 文件文本
     */
    protected String getFileText(@NotNull PsiFile file) {
        return runReadAction(() -> file.getText());
    }

    /**
     * 断言文件包含指定文本
     *
     * @param file         PSI 文件
     * @param expectedText 期望的文本
     */
    protected void assertFileContains(@NotNull PsiFile file, @NotNull String expectedText) {
        String fileText = getFileText(file);
        assertTrue("File should contain: " + expectedText,
                   fileText.contains(expectedText));
    }

    /**
     * 断言文件不包含指定文本
     *
     * @param file           PSI 文件
     * @param unexpectedText 不应该出现的文本
     */
    protected void assertFileNotContains(@NotNull PsiFile file, @NotNull String unexpectedText) {
        String fileText = getFileText(file);
        assertFalse("File should not contain: " + unexpectedText,
                    fileText.contains(unexpectedText));
    }

    /**
     * 打印文件内容（用于调试）
     *
     * @param file PSI 文件
     */
    protected void printFileContent(@NotNull PsiFile file) {
        String content = getFileText(file);
        System.out.println("=== File Content ===");
        System.out.println(content);
        System.out.println("=== End of Content ===");
    }
}

