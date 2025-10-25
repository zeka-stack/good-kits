package com.example.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.example.plugin.service.ExampleService;
import com.example.plugin.util.NotificationUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 项目视图右键菜单触发的示例 Action
 */
public class ExampleSelectionAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile[] selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        if (project == null) {
            NotificationUtil.showError(project, "No project found");
            return;
        }

        if (selectedFiles == null || selectedFiles.length == 0) {
            NotificationUtil.showError(project, "No files selected");
            return;
        }

        // 执行示例服务
        ExampleService exampleService = ExampleService.getInstance(project);
        String result = exampleService.processSelection(selectedFiles);
        
        NotificationUtil.showInfo(project, "Selection Action executed for " + selectedFiles.length + " files - " + result);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile[] selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        
        // 只有在有项目和选中文件时才启用
        e.getPresentation().setEnabled(project != null && selectedFiles != null && selectedFiles.length > 0);
    }
}
