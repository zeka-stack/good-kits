package dev.dong4j.zeka.stack.idea.plugin.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import dev.dong4j.zeka.stack.idea.plugin.service.DocumentationGenerationService;
import dev.dong4j.zeka.stack.idea.plugin.task.DocumentationTask;
import dev.dong4j.zeka.stack.idea.plugin.task.TaskCollector;
import dev.dong4j.zeka.stack.idea.plugin.task.TaskExecutor;
import dev.dong4j.zeka.stack.idea.plugin.util.JavaDocBundle;
import dev.dong4j.zeka.stack.idea.plugin.util.NotificationUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 为选中的文件/目录生成 JavaDoc
 *
 * <p>在项目视图右键菜单中提供，支持：
 * <ul>
 *   <li>单个文件：为该文件生成文档</li>
 *   <li>多个文件：为所有选中的文件生成文档</li>
 *   <li>目录：为目录下所有 Java 文件生成文档</li>
 * </ul>
 *
 * @author dong4j
 * @version 1.0.0
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
public class GenerateJavaDocForSelectionAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        if (project == null || files == null || files.length == 0) {
            return;
        }

        log.info("为 {} 个文件/目录生成 JavaDoc", files.length);

        // 收集任务
        TaskCollector collector = new TaskCollector(project);
        List<DocumentationTask> tasks = new ArrayList<>();

        for (VirtualFile file : files) {
            if (file.isDirectory()) {
                tasks.addAll(collector.collectFromDirectory(file));
            } else if (isJavaFile(file)) {
                tasks.addAll(collector.collectFromVirtualFile(file));
            }
        }

        // 使用文档生成服务处理任务
        DocumentationGenerationService service = new DocumentationGenerationService();
        if (service.checkEmptyTasks(project, tasks, JavaDocBundle.message("notification.no.task.selection"))) {
            return;
        }

        // 确认是否继续（如果任务很多）
        if (tasks.size() > 50) {
            int result = Messages.showYesNoDialog(
                project,
                JavaDocBundle.message("confirmation.batch.generation.message", tasks.size()),
                JavaDocBundle.message("confirmation.batch.generation.title"),
                Messages.getQuestionIcon()
                                                 );

            if (result != Messages.YES) {
                return;
            }
        }

        // 使用服务生成文档，带自定义完成回调
        service.generateDocumentation(project, tasks, "选中文件", stats -> {
            showCompletionMessage(project, stats);
        });
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 在后台线程中执行 update，避免阻塞 EDT
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        boolean enabled = files != null && files.length > 0 && hasJavaFiles(files);
        e.getPresentation().setEnabled(enabled);
        e.getPresentation().setText(JavaDocBundle.message("action.generate.javadoc"));
        e.getPresentation().setDescription(JavaDocBundle.message("action.generate.javadoc.selection.description"));
    }

    private boolean hasJavaFiles(VirtualFile[] files) {
        for (VirtualFile file : files) {
            if (file.isDirectory() || isJavaFile(file)) {
                return true;
            }
        }
        return false;
    }

    private boolean isJavaFile(VirtualFile file) {
        return "java".equalsIgnoreCase(file.getExtension());
    }

    private void showCompletionMessage(Project project, TaskExecutor.TaskStatistics stats) {
        ApplicationManager.getApplication().invokeLater(() -> {
            String content = JavaDocBundle.message("notification.target.completion.format",
                                                   JavaDocBundle.message("notification.generation.complete"),
                                                   stats.completed(),
                                                   stats.failed(),
                                                   stats.skipped());
            NotificationUtil.notifyInfo(project, JavaDocBundle.message("notification.title"), content);
        });
    }
}

