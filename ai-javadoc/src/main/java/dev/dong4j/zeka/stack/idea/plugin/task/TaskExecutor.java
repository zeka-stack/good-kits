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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceException;
import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceFactory;
import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceProvider;
import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;
import dev.dong4j.zeka.stack.idea.plugin.util.NotificationUtil;
import lombok.Getter;
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

    /** 项目对象，用于表示当前操作的项目上下文 */
    private final Project project;
    /** 进度指示器，用于显示任务执行进度 */
    private final ProgressIndicator indicator;
    /** 用户设置状态对象，用于存储和管理应用的配置和用户偏好设置 */
    private final SettingsState settings;
    /** AI 服务提供者实例，用于获取和管理 AI 相关服务 */
    private final AIServiceProvider aiService;

    /** 完成的任务数量计数器，用于记录已成功完成的任务数 */
    private final AtomicInteger completedCount = new AtomicInteger(0);
    /** 失败次数计数器，用于记录任务或操作失败的次数 */
    private final AtomicInteger failedCount = new AtomicInteger(0);
    /** 被跳过的记录数量 */
    private final AtomicInteger skippedCount = new AtomicInteger(0);

    /**
     * 提供商统计信息
     */
    public static class ProviderStatistics {
        /** 服务提供商名称 */
        @Getter
        private final String providerName;
        /** 完成的任务数量计数器，用于记录已成功完成的任务数 */
        private final AtomicInteger completedCount = new AtomicInteger(0);
        /** 失败计数器，用于记录失败操作的次数 */
        private final AtomicInteger failedCount = new AtomicInteger(0);
        /** 被跳过的记录数量 */
        private final AtomicInteger skippedCount = new AtomicInteger(0);
        /** 开始时间戳，表示操作或任务开始的时刻 */
        private final long startTime;
        /** 结束时间，表示某个操作或任务的结束时间戳 */
        private long endTime;

        /**
         * 初始化 ProviderStatistics 实例
         * <p>
         * 通过传入的 providerName 初始化统计信息，并记录开始时间
         *
         * @param providerName 提供商名称
         */
        public ProviderStatistics(String providerName) {
            this.providerName = providerName;
            this.startTime = System.currentTimeMillis();
        }

        /**
         * 获取已完成任务的数量
         * <p>
         * 返回当前已完成任务的计数值
         *
         * @return 已完成任务的数量
         */
        public int getCompletedCount() {
            return completedCount.get();
        }

        /**
         * 获取失败操作的计数
         * <p>
         * 返回当前记录的失败操作次数。
         *
         * @return 失败操作的计数
         */
        public int getFailedCount() {
            return failedCount.get();
        }

        /**
         * 获取已跳过的项目数量
         * <p>
         * 返回当前已跳过的项目计数。
         *
         * @return 已跳过的项目数量
         */
        public int getSkippedCount() {
            return skippedCount.get();
        }

        /**
         * 获取总任务数
         * <p>
         * 返回已完成、失败和跳过任务数的总和
         *
         * @return 总任务数
         */
        public int getTotalCount() {
            return completedCount.get() + failedCount.get() + skippedCount.get();
        }

        /**
         * 获取操作的持续时间
         * <p>
         * 计算并返回从开始时间到结束时间的时间差，单位为毫秒
         *
         * @return 操作的持续时间（毫秒）
         */
        public long getDuration() {
            return endTime - startTime;
        }

        /**
         * 增加已完成任务的计数
         * <p>
         * 调用计数器的 incrementAndGet 方法，将已完成任务的数量增加 1。
         */
        public void incrementCompleted() {
            completedCount.incrementAndGet();
        }

        /**
         * 增加失败计数器的值
         * <p>
         * 该方法用于将失败计数器的值增加1，通常用于记录系统或操作失败的次数。
         */
        public void incrementFailed() {
            failedCount.incrementAndGet();
        }

        /**
         * 增加跳过计数
         * <p>
         * 用于增加跳过操作的计数器值。
         */
        public void incrementSkipped() {
            skippedCount.incrementAndGet();
        }

        /**
         * 结束计时，记录当前时间作为结束时间
         * <p>
         * 该方法用于标记操作或任务的结束时间，将当前系统时间赋值给 endTime 字段
         */
        public void finish() {
            this.endTime = System.currentTimeMillis();
        }

        /**
         * 返回该对象的字符串表示形式，包含执行状态的详细信息。
         * <p>
         * 该方法按照指定格式拼接字符串，展示完成数、失败数、跳过数、总计数以及耗时信息。
         *
         * @return 对象的字符串表示，格式为：providerName: 完成=..., 失败=..., 跳过=..., 总计=..., 耗时=...
         */
        @Override
        public String toString() {
            return String.format("%s: 完成=%d, 失败=%d, 跳过=%d, 总计=%d, 耗时=%.1fs",
                                 providerName, getCompletedCount(), getFailedCount(),
                                 getSkippedCount(), getTotalCount(), getDuration() / 1000.0);
        }
    }

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

        // 检查是否启用性能模式且任务数量大于5个
        if (settings.performanceMode && totalTasks > 5) {
            return processTasksInParallel(tasks);
        } else {
            return processTasksSequentially(tasks);
        }
    }

    /**
     * 顺序处理任务（原有逻辑）
     */
    private boolean processTasksSequentially(@NotNull List<DocumentationTask> tasks) {
        int totalTasks = tasks.size();

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
     * 并行处理任务（性能模式）
     */
    private boolean processTasksInParallel(@NotNull List<DocumentationTask> tasks) {
        List<AIServiceProvider> availableProviders = AIServiceFactory.getAvailableProviders();

        if (availableProviders.isEmpty()) {
            log.warn("性能模式启用但无可用提供商，回退到顺序处理");
            return processTasksSequentially(tasks);
        }

        log.info("性能模式：使用 {} 个提供商并行处理 {} 个任务", availableProviders.size(), tasks.size());

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(availableProviders.size());

        // 为每个提供商创建统计对象
        Map<String, ProviderStatistics> providerStats = new ConcurrentHashMap<>();
        for (int i = 0; i < availableProviders.size(); i++) {
            AIServiceProvider provider = availableProviders.get(i);
            String providerName = provider.getProviderName();
            providerStats.put(providerName, new ProviderStatistics(providerName));
        }

        try {
            // 将任务分配给不同的提供商
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger taskIndex = new AtomicInteger(0);

            for (AIServiceProvider provider : availableProviders) {
                String providerName = provider.getProviderName();
                ProviderStatistics stats = providerStats.get(providerName);

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    processTasksWithProvider(tasks, provider, taskIndex, stats);
                }, executor);
                futures.add(future);
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 完成所有统计
            providerStats.values().forEach(ProviderStatistics::finish);

            indicator.setFraction(1.0);
            indicator.setText("处理完成");

            // 显示每个提供商的统计信息
            showProviderStatistics(providerStats);

            log.info("并行任务处理完成。成功: {}, 失败: {}, 跳过: {}",
                     completedCount.get(), failedCount.get(), skippedCount.get());

            return true;

        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 使用指定提供商处理任务
     */
    private void processTasksWithProvider(@NotNull List<DocumentationTask> tasks,
                                          @NotNull AIServiceProvider provider,
                                          @NotNull AtomicInteger taskIndex,
                                          @NotNull ProviderStatistics stats) {
        int totalTasks = tasks.size();

        while (taskIndex.get() < totalTasks && !indicator.isCanceled()) {
            int currentIndex = taskIndex.getAndIncrement();
            if (currentIndex >= totalTasks) {
                break;
            }

            DocumentationTask task = tasks.get(currentIndex);

            // 更新进度
            SwingUtilities.invokeLater(() -> {
                double fraction = (double) currentIndex / totalTasks;
                indicator.setFraction(fraction);
                indicator.setText(String.format("正在处理 (%d/%d): %s",
                                                currentIndex + 1, totalTasks, task.getFilePath()));
                indicator.setText2(String.format("完成: %d, 失败: %d, 跳过: %d",
                                                 completedCount.get(), failedCount.get(), skippedCount.get()));
            });

            // 处理任务
            processTaskWithProvider(task, provider, stats);
        }
    }

    /**
     * 使用指定提供商处理单个任务
     */
    private void processTaskWithProvider(@NotNull DocumentationTask task,
                                         @NotNull AIServiceProvider provider,
                                         @NotNull ProviderStatistics stats) {
        try {
            task.setStatus(DocumentationTask.TaskStatus.PROCESSING);

            // 检查是否应该跳过
            if (shouldSkip(task)) {
                task.setStatus(DocumentationTask.TaskStatus.SKIPPED);
                skippedCount.incrementAndGet();
                stats.incrementSkipped();
                return;
            }

            // 生成文档
            String documentation = provider.generateDocumentation(task.getCode(), task.getType(), "java");

            if (documentation.trim().isEmpty()) {
                task.setStatus(DocumentationTask.TaskStatus.FAILED);
                task.setErrorMessage("生成的文档为空");
                failedCount.incrementAndGet();
                stats.incrementFailed();
                return;
            }

            // 插入文档
            insertDocumentation(task, documentation);

            task.setStatus(DocumentationTask.TaskStatus.COMPLETED);
            task.setResult(documentation);
            completedCount.incrementAndGet();
            stats.incrementCompleted();

        } catch (AIServiceException e) {
            String errorMessage = getAIServiceErrorMessage(e);
            log.info("AI 服务调用失败: {} - {}", task, errorMessage, e);
            task.setStatus(DocumentationTask.TaskStatus.FAILED);
            task.setErrorMessage(errorMessage);
            failedCount.incrementAndGet();
            stats.incrementFailed();
        } catch (Exception e) {
            log.info("处理任务失败: {}", task, e);
            task.setStatus(DocumentationTask.TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            failedCount.incrementAndGet();
            stats.incrementFailed();
        }
    }

    /**
     * 显示提供商统计信息
     */
    private void showProviderStatistics(@NotNull Map<String, ProviderStatistics> providerStats) {
        // 创建HTML格式的统计信息
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><style>");
        htmlContent.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 10px; font-size: 12px; }");
        htmlContent.append("h2 { color: #2E7D32; margin-bottom: 15px; font-size: 16px; }");
        htmlContent.append("h3 { color: #1976D2; margin-bottom: 10px; font-size: 14px; }");
        htmlContent.append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; font-size: 11px; border: 1px solid #ddd;" +
                           " }");
        htmlContent.append("th { background-color: #6c757d; color: white; padding: 8px; text-align: center; font-weight: bold; font-size:" +
                           " 11px; border: 1px solid #ddd; }");
        htmlContent.append("td { padding: 8px; text-align: center; font-size: 11px; border: 1px solid #ddd; }");
        htmlContent.append("td.provider-name { text-align: left; }");
        htmlContent.append("tr:nth-child(even) { background-color: #f8f9fa; }");
        htmlContent.append("tr:hover { background-color: #e3f2fd; }");
        htmlContent.append(".summary-row { background-color: #495057; color: white; font-weight: bold; }");
        htmlContent.append(".summary-row td { border: 1px solid #ddd; }");
        htmlContent.append("</style></head><body>");

        // 添加标题
        htmlContent.append("<h2>🚀 性能模式处理完成</h2>");

        // 创建提供商统计表格
        htmlContent.append("<table>");
        htmlContent.append("<tr><th>服务商名称</th><th>完成数量</th><th>失败数量</th><th>跳过数量</th><th>耗时</th></tr>");

        int totalCompleted = 0;
        int totalFailed = 0;
        int totalSkipped = 0;
        long totalDuration = 0;

        for (ProviderStatistics stats : providerStats.values()) {
            htmlContent.append("<tr>");
            htmlContent.append("<td class='provider-name'>").append(stats.getProviderName()).append("</td>");
            htmlContent.append("<td>").append(stats.getCompletedCount()).append("</td>");
            htmlContent.append("<td>").append(stats.getFailedCount()).append("</td>");
            htmlContent.append("<td>").append(stats.getSkippedCount()).append("</td>");
            htmlContent.append("<td>").append(String.format("%.1fs", stats.getDuration() / 1000.0)).append("</td>");
            htmlContent.append("</tr>");

            totalCompleted += stats.getCompletedCount();
            totalFailed += stats.getFailedCount();
            totalSkipped += stats.getSkippedCount();
            totalDuration += stats.getDuration();
        }

        // 添加总体统计行
        htmlContent.append("<tr class='summary-row'>");
        htmlContent.append("<td>📊 总体统计</td>");
        htmlContent.append("<td>").append(totalCompleted).append("</td>");
        htmlContent.append("<td>").append(totalFailed).append("</td>");
        htmlContent.append("<td>").append(totalSkipped).append("</td>");
        htmlContent.append("<td>").append(String.format("%.1fs", totalDuration / 1000.0)).append("</td>");
        htmlContent.append("</tr>");

        htmlContent.append("</table>");
        htmlContent.append("</body></html>");

        // 在日志中记录详细信息
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("性能模式统计详情：\n");
        logMessage.append("各提供商处理统计：\n");

        for (ProviderStatistics stats : providerStats.values()) {
            logMessage.append("• ").append(stats.toString()).append("\n");
        }

        logMessage.append("\n总体统计：\n");
        logMessage.append(String.format("• 总计: %d 个任务\n", totalCompleted + totalFailed + totalSkipped));
        logMessage.append(String.format("• 完成: %d 个\n", totalCompleted));
        logMessage.append(String.format("• 失败: %d 个\n", totalFailed));
        logMessage.append(String.format("• 跳过: %d 个\n", totalSkipped));
        logMessage.append(String.format("• 总耗时: %.1f 秒\n", totalDuration / 1000.0));

        if (totalCompleted > 0) {
            double avgTimePerTask = (double) totalDuration / totalCompleted;
            logMessage.append(String.format("• 平均每任务耗时: %.1f 秒", avgTimePerTask / 1000.0));
        }

        log.info("{}", logMessage);

        // 显示HTML格式的通知给用户
        SwingUtilities.invokeLater(() -> {
            // 创建自定义对话框
            javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Frame) null, "性能模式处理完成", true);
            dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);

            // 创建HTML内容面板
            javax.swing.JEditorPane editorPane = new javax.swing.JEditorPane();
            editorPane.setContentType("text/html");
            editorPane.setText(htmlContent.toString());
            editorPane.setEditable(false);
            editorPane.setBackground(javax.swing.UIManager.getColor("Panel.background"));

            // 计算动态高度
            int providerCount = providerStats.size();
            int totalRows = providerCount + 2; // 提供商行数 + 表头 + 总体统计行

            // 每行高度约30px，表头高度约35px，总体统计行高度约35px, 在加上标题和一定的冗余量
            int calculatedHeight = 35 + (providerCount * 30) + 35 + 50;

            // 设置最小和最大高度阈值
            int minHeight = 200;  // 最小高度
            int maxHeight = 600;  // 最大高度

            // 应用阈值限制
            int finalHeight = Math.max(minHeight, Math.min(maxHeight, calculatedHeight));

            // 记录高度计算信息
            log.debug("动态高度计算: 提供商数量={}, 计算高度={}, 最终高度={}",
                      providerCount, calculatedHeight, finalHeight);

            // 设置滚动面板
            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(editorPane);
            scrollPane.setPreferredSize(new java.awt.Dimension(800, finalHeight));

            // 添加确定按钮
            javax.swing.JButton okButton = new javax.swing.JButton("确定");
            okButton.addActionListener(e -> dialog.dispose());

            javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
            buttonPanel.add(okButton);

            // 设置布局
            dialog.setLayout(new java.awt.BorderLayout());
            dialog.add(scrollPane, java.awt.BorderLayout.CENTER);
            dialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

            // 设置对话框属性
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });
    }

    /**
     * 处理文档生成任务
     * <p>
     * 该方法负责处理一个文档生成任务，包括设置任务状态、检查是否跳过、生成文档、插入文档以及处理异常。
     * 如果任务被跳过，则更新状态并增加跳过计数。如果生成文档失败或发生异常，则更新任务状态为失败并记录错误信息。
     * 如果任务成功完成，则更新状态为完成并增加完成计数。
     *
     * @param task 要处理的文档生成任务对象
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

    /**
     * 计算删除操作的结束位置
     * <p>
     * 根据给定的文档对象和结束偏移量，计算删除操作的实际结束位置。该方法会处理换行符，包括Windows风格的\r\n换行符。
     *
     * @param document  文档对象，用于获取文本内容和长度
     * @param endOffset 初始的结束偏移量
     * @return 调整后的删除结束位置
     */
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

        /**
         * 计算并返回总任务数
         * <p>
         * 将已完成、失败和跳过的任务数量相加，得到总任务数
         *
         * @return 总任务数
         */
        public int getTotal() {
            return completed + failed + skipped;
        }

        /**
         * 返回对象的字符串表示形式
         * <p>
         * 以格式化字符串的形式展示对象的完成数、失败数、跳过数和总计数
         *
         * @return 对象的字符串表示
         */
        @NotNull
        @Override
        public String toString() {
            return String.format("完成: %d, 失败: %d, 跳过: %d, 总计: %d",
                                 completed, failed, skipped, getTotal());
        }
    }
}

