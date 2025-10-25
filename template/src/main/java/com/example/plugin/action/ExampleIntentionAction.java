package com.example.plugin.action;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.example.plugin.service.ExampleService;
import com.example.plugin.util.NotificationUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Intention Action 示例
 * 通过 Option+Enter / Alt+Enter 触发
 */
public class ExampleIntentionAction implements IntentionAction, PriorityAction {

    @Override
    @NotNull
    public String getText() {
        return "Example Intention Action";
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return "Example Plugin";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        // 检查是否应该显示这个 intention
        return file != null && file.getName().endsWith(".java");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        if (file == null) {
            return;
        }

        // 执行示例服务
        ExampleService exampleService = ExampleService.getInstance(project);
        String result = exampleService.processIntention(file);
        
        NotificationUtil.showInfo(project, "Intention Action executed: " + result);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public @NotNull Priority getPriority() {
        return Priority.NORMAL;
    }
}
