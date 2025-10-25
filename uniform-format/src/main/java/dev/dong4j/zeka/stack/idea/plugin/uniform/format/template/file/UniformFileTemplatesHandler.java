package dev.dong4j.zeka.stack.idea.plugin.uniform.format.template.file;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.impl.FileTemplateConfigurable;
import com.intellij.ide.fileTemplates.impl.FileTemplateManagerImpl;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一文件模板处理器
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
@Slf4j
public class UniformFileTemplatesHandler implements StartupActivity {

    /** HEADER */
    public static final String HEADER = """
        /**
         * ${description}
         *
         * @author %s
         * @version %s
         * @email "mailto:%s@163.com"
         * @date ${YEAR}.${MONTH}.${DAY} ${HOUR}:${MINUTE}
         * @since %s
         */""";

    @Override
    public void runActivity(@NotNull Project project) {
        FileTemplateManager templateManager = FileTemplateManager.getInstance(project);

        FileTemplate defaultTemplate =
            FileTemplateManager.getInstance(project).getDefaultTemplate(FileTemplateManager.FILE_HEADER_TEMPLATE_NAME);

        if (!defaultTemplate.getText().trim().contains("成都返空汇网络技术有限公司")) {
            templateManager.removeTemplate(defaultTemplate);

            String author = getCurrentUserName();
            String version = "1.0.0";

            FileTemplate template = FileTemplateUtil.createTemplate(FileTemplateManager.FILE_HEADER_TEMPLATE_NAME,
                                                                    JavaFileType.DEFAULT_EXTENSION,
                                                                    String.format(HEADER, author, version, author, version),
                                                                    new FileTemplate[0]);

            FileTemplateConfigurable configurable = new FileTemplateConfigurable(project);
            configurable.setTemplate(template, FileTemplateManagerImpl.getInstanceImpl(project).getDefaultTemplateDescription());
            templateManager.setTemplates(FileTemplateManager.INCLUDES_TEMPLATES_CATEGORY, Collections.singletonList(template));
        }

    }

    public static String getCurrentUserName() {
        // 首选标准 Java 属性（跨平台）
        String username = System.getProperty("user.name");

        // 兜底方案：尝试从环境变量中获取
        if (username == null || username.isEmpty()) {
            username = System.getenv("USER");      // macOS / Linux
            if (username == null || username.isEmpty()) {
                username = System.getenv("USERNAME"); // Windows
            }
        }

        // 最后兜底
        return username != null ? username : "unknown";
    }

}