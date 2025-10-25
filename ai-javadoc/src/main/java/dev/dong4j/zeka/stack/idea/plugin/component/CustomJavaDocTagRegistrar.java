package dev.dong4j.zeka.stack.idea.plugin.component;

import com.intellij.codeInspection.InspectionProfile;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.javaDoc.JavadocDeclarationInspection;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;

import org.jetbrains.annotations.NotNull;

/**
 * 在插件启动时自动注册自定义的 JavaDoc 标签
 * <p>
 * 这个组件会在 IntelliJ 启动时自动运行，将 "@date" 和 "@email" 标签添加到
 * JavadocDeclarationInspection 的自定义标签列表中，这样用户就不需要手动添加了。
 *
 * @author dong4j
 * @version 1.0.0
 * @since 1.0.0
 */
public class CustomJavaDocTagRegistrar implements StartupActivity {

    /**
     * 在项目启动时运行，注册自定义的 JavaDoc 标签
     *
     * @param project 启动的项目
     */
    @Override
    public void runActivity(@NotNull Project project) {
        // 在写操作中执行标签注册
        ApplicationManager.getApplication().invokeLater(() -> {
            ApplicationManager.getApplication().runWriteAction(() -> {
                registerCustomTags(project);
            });
        });
    }

    /**
     * 注册自定义标签
     *
     * @param project 项目对象
     */
    private void registerCustomTags(@NotNull Project project) {
        try {
            // 获取项目的检查配置管理器
            ProjectInspectionProfileManager profileManager = ProjectInspectionProfileManager.getInstance(project);

            // 获取当前的检查配置
            InspectionProfile profile = profileManager.getCurrentProfile();

            // 获取 JavadocDeclarationInspection 工具
            InspectionToolWrapper<?, ?> toolWrapper = profile.getInspectionTool("JavadocDeclaration", project);

            if (toolWrapper != null) {
                // 获取实际的检查工具实例
                Object tool = toolWrapper.getTool();

                // 检查是否是 JavadocDeclarationInspection 类型
                if (tool instanceof JavadocDeclarationInspection inspection) {

                    // 注册自定义标签
                    registerAdditionalTag(inspection, "date");
                    registerAdditionalTag(inspection, "email");

                    // 通知配置已更改
                    profileManager.fireProfileChanged();
                }
            }
        } catch (Exception e) {
            // 记录错误但不中断插件启动
            e.printStackTrace();
        }
    }

    /**
     * 注册额外的标签到 Javadoc 检查工具中
     *
     * @param inspection Javadoc 检查工具实例
     * @param tagName    要注册的标签名称
     */
    private void registerAdditionalTag(JavadocDeclarationInspection inspection, String tagName) {
        try {
            // 使用反射调用 registerAdditionalTag 方法
            java.lang.reflect.Method method = JavadocDeclarationInspection.class.getDeclaredMethod("registerAdditionalTag", String.class);
            method.setAccessible(true);
            method.invoke(inspection, tagName);
        } catch (Exception e) {
            // 如果反射调用失败，尝试直接访问字段（备用方案）
            try {
                // 尝试访问 additionalTags 字段
                java.lang.reflect.Field additionalTagsField = JavadocDeclarationInspection.class.getDeclaredField("additionalTags");
                additionalTagsField.setAccessible(true);
                String additionalTags = (String) additionalTagsField.get(inspection);

                // 如果标签不存在，则添加
                if (additionalTags == null || additionalTags.isEmpty()) {
                    additionalTagsField.set(inspection, tagName);
                } else if (!additionalTags.contains(tagName)) {
                    additionalTagsField.set(inspection, additionalTags + "," + tagName);
                }
            } catch (Exception ex) {
                // 如果所有方法都失败，记录错误
                ex.printStackTrace();
            }
        }
    }
}