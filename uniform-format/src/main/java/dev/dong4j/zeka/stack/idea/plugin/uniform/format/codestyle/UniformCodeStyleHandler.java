package dev.dong4j.zeka.stack.idea.plugin.uniform.format.codestyle;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;

import org.jetbrains.annotations.NotNull;

import dev.dong4j.zeka.stack.idea.plugin.uniform.format.settings.UniformFormatSettingsState;
import dev.dong4j.zeka.stack.idea.plugin.uniform.format.util.StatisticsUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 统一代码风格处理器
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
@Slf4j
public class UniformCodeStyleHandler implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        try {
            UniformFormatSettingsState settings = UniformFormatSettingsState.getInstance();
            if (!settings.isEnableCodeStyle()) {
                log.info("Code style disabled, skipping configuration");
                return;
            }

            // 提供统一代码风格方案
            UniformCodeStyleSchemeProvider.provideUniformCodeStyleScheme(project);

            // 报告使用统计
            StatisticsUtil.reportCodeStyleUsage();

        } catch (Exception e) {
            log.error("Failed to configure uniform code style for project: {}", project.getName(), e);
        }
    }

}