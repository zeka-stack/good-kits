package dev.dong4j.zeka.stack.idea.plugin.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceException;
import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceFactory;
import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceProvider;
import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;
import dev.dong4j.zeka.stack.idea.plugin.util.NotificationUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务执行器
 *
 * <p>负责执行文档生成任务队列，处理多个文件的批量生成。
 * 作为文档生成流程的核心组件，协调 AI 服务调用、文档插入和进度管理。
 *
 * <p>核心功能：
 * <ul>
 *   <li>批量处理文档生成任务</li>
 *   <li>与 AI 服务交互生成文档内容</li>
 *   <li>将生成的文档插入到源代码中</li>
 *   <li>实时显示处理进度和统计信息</li>
 *   <li>处理异常和错误情况</li>
 *   <li>支持用户取消操作</li>
 * </ul>
 *
 * <p>执行流程：
 * <ol>
 *   <li>初始化 AI 服务提供商</li>
 *   <li>遍历任务列表逐个处理</li>
 *   <li>更新进度指示器</li>
 *   <li>调用 AI 服务生成文档</li>
 *   <li>将文档插入到源代码</li>
 *   <li>收集处理统计信息</li>
 * </ol>
 *
 * <p>线程安全：
 * <ul>
 *   <li>使用 AtomicInteger 确保计数器线程安全</li>
 *   <li>PSI 访问在适当的线程上下文中执行</li>
 *   <li>UI 更新通过 invokeLater 调度</li>
 * </ul>
 *
 * @author dong4j
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class TaskExecutor {

    private final Project project;
    private final ProgressIndicator indicator;
    private final SettingsState settings;
    private final AIServiceProvider aiService;

    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger skippedCount = new AtomicInteger(0);

    /**
     * 构造任务执行器
     *
     * @param project   项目对象
     * @param indicator 进度指示器
     */
    public TaskExecutor(@NotNull Project project, @NotNull ProgressIndicator indicator) {
        this.project = project;
        this.indicator = indicator;
        this.settings = SettingsState.getInstance();
        this.aiService = AIServiceFactory.createProvider(settings);
    }

    /**
     * 检查 AI 服务是否可用
     *
     * @return 如果 AI 服务可用返回 true，否则返回 false
     */
    public boolean isServiceAvailable() {
        return aiService != null;
    }

    /**
     * 处理任务列表
     *
     * <p>批量处理文档生成任务列表，按顺序执行每个任务。
     * 在处理过程中更新进度指示器，显示实时统计信息。
     *
     * <p>处理流程：
     * <ol>
     *   <li>检查 AI 服务是否可用</li>
     *   <li>检查任务列表是否为空</li>
     *   <li>设置进度指示器为确定模式</li>
     *   <li>遍历任务列表逐个处理</li>
     *   <li>更新进度和统计信息</li>
     *   <li>处理完成后更新最终状态</li>
     * </ol>
     *
     * <p>取消支持：
     * <ul>
     *   <li>检查 indicator.isCanceled() 决定是否继续处理</li>
     *   <li>用户可以通过进度对话框取消操作</li>
     * </ul>
     *
     * @param tasks 任务列表
     * @see #processTask(DocumentationTask)
     */
    public boolean processTasks(@NotNull List<DocumentationTask> tasks) {
        if (tasks.isEmpty()) {
            return false;
        }

        indicator.setIndeterminate(false);
        int totalTasks = tasks.size();

        log.info("开始处理 {} 个文档生成任务", totalTasks);

        for (int i = 0; i < totalTasks && !indicator.isCanceled(); i++) {
            DocumentationTask task = tasks.get(i);

            // 更新进度
            double fraction = (double) i / totalTasks;
            indicator.setFraction(fraction);
            indicator.setText(String.format("正在处理 (%d/%d): %s",
                                            i + 1, totalTasks, task.getFilePath()));

            // 处理任务
            processTask(task);

            // 显示统计信息
            indicator.setText2(String.format("完成: %d, 失败: %d, 跳过: %d",
                                             completedCount.get(), failedCount.get(), skippedCount.get()));
        }

        indicator.setFraction(1.0);
        indicator.setText("处理完成");

        log.info("任务处理完成。成功: {}, 失败: {}, 跳过: {}",
                 completedCount.get(), failedCount.get(), skippedCount.get());

        return true;
    }

    /**
     * 处理单个任务
     *
     * <p>处理单个文档生成任务，包括跳过检查、文档生成和插入。
     * 完整的错误处理确保单个任务失败不会影响其他任务。
     *
     * <p>处理步骤：
     * <ol>
     *   <li>设置任务状态为 PROCESSING</li>
     *   <li>检查是否应该跳过任务</li>
     *   <li>调用 AI 服务生成文档</li>
     *   <li>将生成的文档插入到源代码</li>
     *   <li>更新任务状态和统计计数器</li>
     * </ol>
     *
     * <p>异常处理：
     * <ul>
     *   <li>捕获所有异常，防止中断整个处理流程</li>
     *   <li>记录详细错误日志</li>
     *   <li>设置任务错误信息</li>
     *   <li>更新失败计数器</li>
     * </ul>
     *
     * @param task 要处理的文档生成任务
     * @see #shouldSkip(DocumentationTask)
     * @see #generateDocumentation(DocumentationTask)
     * @see #insertDocumentation(DocumentationTask, String)
     */
    private void processTask(@NotNull DocumentationTask task) {
        try {
            task.setStatus(DocumentationTask.TaskStatus.PROCESSING);

            // 检查是否应该跳过
            if (shouldSkip(task)) {
                task.setStatus(DocumentationTask.TaskStatus.SKIPPED);
                skippedCount.incrementAndGet();
                return;
            }

            // 生成文档
            String documentation = generateDocumentation(task);

            if (documentation.trim().isEmpty()) {
                task.setStatus(DocumentationTask.TaskStatus.FAILED);
                task.setErrorMessage("生成的文档为空");
                failedCount.incrementAndGet();
                return;
            }

            // 插入文档
            insertDocumentation(task, documentation);

            task.setStatus(DocumentationTask.TaskStatus.COMPLETED);
            task.setResult(documentation);
            completedCount.incrementAndGet();

        } catch (AIServiceException e) {
            // AI 服务异常 - 提供友好的错误提示
            String errorMessage = getAIServiceErrorMessage(e);
            log.info("AI 服务调用失败: {} - {}", task, errorMessage, e);
            task.setStatus(DocumentationTask.TaskStatus.FAILED);
            task.setErrorMessage(errorMessage);
            failedCount.incrementAndGet();

            // 只在第一次失败时显示通知，避免过多通知
            if (failedCount.get() == 1) {
                NotificationUtil.notifyErrorMessage(
                    project,
                    errorMessage
                                                   );
            }
        } catch (Exception e) {
            log.info("处理任务失败: {}", task, e);
            task.setStatus(DocumentationTask.TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            failedCount.incrementAndGet();
        }
    }

    /**
     * 将 AI 服务异常转换为友好的错误消息
     *
     * <p>根据异常类型生成用户友好的错误提示信息。
     *
     * @param e AI 服务异常
     * @return 友好的错误消息
     */
    private String getAIServiceErrorMessage(AIServiceException e) {
        AIServiceException.ErrorCode errorCode = e.getErrorCode();

        if (errorCode == null) {
            return "AI 服务调用失败: " + e.getMessage();
        }

        return switch (errorCode) {
            case INVALID_API_KEY -> "API Key 无效，请在设置中检查并更新 API Key";
            case RATE_LIMIT -> "请求频率过高，请稍后再试";
            case SERVICE_UNAVAILABLE -> "AI 服务暂时不可用，请稍后再试";
            case NETWORK_ERROR -> "网络连接失败，请检查网络连接或服务器地址";
            case CONFIGURATION_ERROR -> "配置错误: " + e.getMessage();
            case INVALID_RESPONSE -> "AI 服务返回的数据格式错误";
            default -> "AI 服务调用失败: " + e.getMessage();
        };
    }

    /**
     * 判断是否应该跳过任务
     *
     * <p>根据用户配置和元素状态决定是否跳过任务。
     * 主要用于避免重复生成已有文档的元素。
     *
     * <p>跳过条件：
     * <ul>
     *   <li>skipExisting 配置为 true</li>
     *   <li>元素支持文档（PsiDocCommentOwner）</li>
     *   <li>元素已有 JavaDoc 注释</li>
     * </ul>
     *
     * <p>线程安全：
     * <ul>
     *   <li>PSI 访问必须在 read-action 中执行</li>
     *   <li>使用 ApplicationManager.runReadAction 确保线程安全</li>
     * </ul>
     *
     * @param task 文档生成任务
     * @return 如果应该跳过返回 true，否则返回 false
     * @see SettingsState#skipExisting
     */
    private boolean shouldSkip(@NotNull DocumentationTask task) {
        if (!settings.skipExisting) {
            return false;
        }

        // PSI 访问必须在 read-action 中
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
            PsiElement element = task.getElement();

            // 检查是否已有文档
            if (element instanceof PsiDocCommentOwner) {
                PsiDocComment docComment = ((PsiDocCommentOwner) element).getDocComment();
                return docComment != null;
            }

            return false;
        });
    }

    /**
     * 生成文档
     *
     * <p>调用 AI 服务提供商生成文档内容。
     * 将任务类型转换为文档类型，然后调用 AI 服务。
     *
     * <p>处理流程：
     * <ol>
     *   <li>转换任务类型为文档类型</li>
     *   <li>调用 AI 服务生成文档</li>
     *   <li>返回生成的文档内容</li>
     * </ol>
     *
     * <p>异常处理：
     * <ul>
     *   <li>AIServiceException 会向上传播</li>
     *   <li>由调用方统一处理</li>
     * </ul>
     *
     * @param task 文档生成任务
     * @return 生成的文档内容
     * @throws AIServiceException 当 AI 服务调用失败时抛出
     * @see AIServiceProvider#generateDocumentation(String, DocumentationTask.TaskType, String)
     */
    private String generateDocumentation(@NotNull DocumentationTask task) throws AIServiceException {
        return aiService.generateDocumentation(task.getCode(), task.getType(), "java");
    }

    /**
     * 插入文档到代码中
     *
     * <p>将生成的文档注释插入到源代码的适当位置。
     * 如果元素已有注释，会先删除旧注释，再插入新注释。
     * 整个操作在 IntelliJ 的命令和写入操作上下文中执行。
     *
     * <p>插入流程：
     * <ol>
     *   <li>获取元素对应的文档对象</li>
     *   <li>删除已有的旧注释</li>
     *   <li>确定插入位置</li>
     *   <li>格式化并插入新注释</li>
     *   <li>应用代码格式化</li>
     * </ol>
     *
     * <p>线程模型：
     * <ul>
     *   <li>使用 invokeLater 调度到事件调度线程</li>
     *   <li>在命令上下文中执行</li>
     *   <li>在写入操作中执行</li>
     * </ul>
     *
     * @param task          文档生成任务
     * @param documentation 生成的文档内容
     * @see #deleteOldDocComment(PsiElement, Document)
     * @see #getInsertPosition(PsiElement)
     */
    @SuppressWarnings("D")
    private void insertDocumentation(@NotNull DocumentationTask task, @NotNull String documentation) {
        ApplicationManager.getApplication().invokeLater(() -> {
            PsiElement element = task.getElement();
            Document document = FileDocumentManager.getInstance()
                .getDocument(element.getContainingFile().getVirtualFile());

            if (document == null) {
                return;
            }

            PsiDocumentManager.getInstance(project)
                .doPostponedOperationsAndUnblockDocument(document);

            CommandProcessor.getInstance().executeCommand(
                project,
                () -> ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        // 1. 先删除旧注释（如果存在）
                        deleteOldDocComment(element, document);

                        // 2. 提交删除操作
                        PsiDocumentManager.getInstance(project).commitDocument(document);

                        // 3. 获取插入位置（删除后需要重新获取）
                        int startPosition = getInsertPosition(element);
                        int lineNumber = document.getLineNumber(startPosition);
                        int lineStartPosition = document.getLineStartOffset(lineNumber);

                        // 4. 确保文档以 /** 开头
                        String javadoc = documentation.trim();
                        if (!javadoc.startsWith("/**")) {
                            javadoc = "/**\n" + javadoc;
                        }
                        if (!javadoc.endsWith("*/")) {
                            javadoc = javadoc + "\n */";
                        }

                        // 5. 插入新 JavaDoc
                        document.insertString(lineStartPosition, javadoc + "\n");
                        PsiDocumentManager.getInstance(project).commitDocument(document);

                        // 6. 格式化插入的 JavaDoc
                        PsiFile psiFile = element.getContainingFile();
                        if (psiFile != null) {
                            int endPosition = lineStartPosition + javadoc.length() + 1;
                            CodeStyleManager.getInstance(project)
                                .reformatText(psiFile, lineStartPosition, endPosition);
                        }

                    } catch (Exception e) {
                        log.info("插入文档失败", e);
                    }
                }),
                "Insert JavaDoc",
                "AI Javadoc"
                                                         );
        });
    }

    /**
     * 删除元素的旧 JavaDoc 注释
     *
     * <p>删除元素已有的 JavaDoc 注释，为新注释腾出空间。
     * 同时删除注释前后的空白行，防止空行累积。
     *
     * <p>删除策略：
     * <ul>
     *   <li>删除注释本身</li>
     *   <li>删除注释后面的一个换行符（如果有）</li>
     *   <li>删除注释前面的所有空白行（防止累积）</li>
     * </ul>
     *
     * <p>安全措施：
     * <ul>
     *   <li>检查元素是否支持文档</li>
     *   <li>检查是否已有注释</li>
     *   <li>捕获异常防止中断操作</li>
     *   <li>边界检查防止越界</li>
     * </ul>
     *
     * @param element  目标元素
     * @param document 文档对象
     */
    @SuppressWarnings("D")
    private void deleteOldDocComment(@NotNull PsiElement element, @NotNull Document document) {
        if (!(element instanceof PsiDocCommentOwner)) {
            return;
        }

        PsiDocComment oldComment = ((PsiDocCommentOwner) element).getDocComment();
        if (oldComment == null) {
            return;
        }

        try {
            int startOffset = oldComment.getTextRange().getStartOffset();
            int endOffset = oldComment.getTextRange().getEndOffset();

            // 计算实际删除范围
            int deleteStart = startOffset;
            final int deleteEnd = getDeleteEnd(document, endOffset);

            // 2. 向前扩展：删除注释前面的所有空白行（包括空格、制表符）
            // 这是防止空行累积的关键！
            int lineStart = document.getLineStartOffset(document.getLineNumber(startOffset));
            while (deleteStart > lineStart) {
                char prevChar = document.getCharsSequence().charAt(deleteStart - 1);
                // 只删除空白字符（空格和制表符），但保留换行符
                if (prevChar == ' ' || prevChar == '\t') {
                    deleteStart--;
                } else {
                    break;
                }
            }

            // 如果注释前面只有空白字符，则从行首开始删除
            if (deleteStart == lineStart) {
                // 检查是否可以继续向前删除空行
                while (lineStart > 0) {
                    int prevLineEnd = lineStart - 1;
                    // 跳过换行符
                    if (document.getCharsSequence().charAt(prevLineEnd) == '\n') {
                        int prevLineStart = document.getLineStartOffset(document.getLineNumber(prevLineEnd));
                        // 检查前一行是否为空行（只包含空白字符）
                        boolean isEmptyLine = true;
                        for (int i = prevLineStart; i < prevLineEnd; i++) {
                            char c = document.getCharsSequence().charAt(i);
                            if (c != ' ' && c != '\t' && c != '\r') {
                                isEmptyLine = false;
                                break;
                            }
                        }

                        if (isEmptyLine) {
                            // 是空行，继续向前删除
                            deleteStart = prevLineStart;
                            lineStart = prevLineStart;
                        } else {
                            // 不是空行，停止向前扩展
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }

            // 执行删除
            document.deleteString(deleteStart, deleteEnd);

            if (settings.verboseLogging) {
                log.debug("删除旧注释: 从 {} 到 {} (原注释: {} 到 {})",
                          deleteStart, deleteEnd, startOffset, endOffset);
            }

        } catch (Exception e) {
            log.warn("删除旧注释失败", e);
        }
    }

    private static int getDeleteEnd(@NotNull Document document, int endOffset) {
        int deleteEnd = endOffset;

        // 1. 向后扩展：删除注释后面的一个换行符（如果有）
        if (deleteEnd < document.getTextLength()) {
            char nextChar = document.getCharsSequence().charAt(deleteEnd);
            if (nextChar == '\n') {
                deleteEnd++;
            } else if (nextChar == '\r' && deleteEnd + 1 < document.getTextLength()) {
                // 处理 Windows 风格的换行符 \r\n
                if (document.getCharsSequence().charAt(deleteEnd + 1) == '\n') {
                    deleteEnd += 2;
                } else {
                    deleteEnd++;
                }
            }
        }
        return deleteEnd;
    }

    /**
     * 获取文档插入位置
     *
     * <p>确定新文档注释应该插入的位置。
     * 通常插入在元素修饰符列表之前，确保注释位置正确。
     *
     * <p>位置规则：
     * <ul>
     *   <li>PsiMethod：方法修饰符列表之前</li>
     *   <li>PsiClass：类修饰符列表之前</li>
     *   <li>PsiField：字段修饰符列表之前</li>
     *   <li>其他：元素起始位置</li>
     * </ul>
     *
     * @param element PSI 元素
     * @return 文档插入位置的偏移量
     */
    private int getInsertPosition(@NotNull PsiElement element) {
        if (element instanceof PsiMethod) {
            return ((PsiMethod) element).getModifierList().getTextRange().getStartOffset();
        } else if (element instanceof PsiClass) {
            return ((PsiClass) element).getModifierList().getTextRange().getStartOffset();
        } else if (element instanceof PsiField) {
            return ((PsiField) element).getModifierList().getTextRange().getStartOffset();
        }
        return element.getTextRange().getStartOffset();
    }

    /**
     * 获取统计信息
     *
     * <p>返回任务处理的统计信息，包括完成、失败和跳过的任务数量。
     * 用于向用户显示处理结果。
     *
     * <p>统计内容：
     * <ul>
     *   <li>完成数量：成功处理的任务数</li>
     *   <li>失败数量：处理失败的任务数</li>
     *   <li>跳过数量：被跳过的任务数</li>
     *   <li>总计：所有任务的总数</li>
     * </ul>
     *
     * @return 任务统计信息
     * @see TaskStatistics
     */
    public TaskStatistics getStatistics() {
        return new TaskStatistics(
            completedCount.get(),
            failedCount.get(),
            skippedCount.get()
        );
    }

    /**
     * 任务统计信息
     *
     * <p>记录任务处理的统计信息，用于结果展示和日志记录。
     * 使用 record 简化代码，提供基本的统计计算和格式化功能。
     *
     * <p>包含的信息：
     * <ul>
     *   <li>completed：成功完成的任务数</li>
     *   <li>failed：处理失败的任务数</li>
     *   <li>skipped：被跳过的任务数</li>
     * </ul>
     *
     * <p>提供的方法：
     * <ul>
     *   <li>getTotal()：计算任务总数</li>
     *   <li>toString()：格式化统计信息</li>
     * </ul>
     */
    public record TaskStatistics(int completed, int failed, int skipped) {

        public int getTotal() {
            return completed + failed + skipped;
        }

        @NotNull
        @Override
        public String toString() {
            return String.format("完成: %d, 失败: %d, 跳过: %d, 总计: %d",
                                 completed, failed, skipped, getTotal());
        }
    }
}

