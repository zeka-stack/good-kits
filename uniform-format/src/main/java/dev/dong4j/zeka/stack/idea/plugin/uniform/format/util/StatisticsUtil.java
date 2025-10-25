package dev.dong4j.zeka.stack.idea.plugin.uniform.format.util;

import dev.dong4j.zeka.stack.idea.plugin.uniform.format.settings.UniformFormatSettingsState;
import dev.dong4j.zeka.stack.idea.plugin.uniform.format.statistics.TemplateUsageStatisticReporter;
import dev.dong4j.zeka.stack.idea.plugin.uniform.format.statistics.TemplateUsageStatisticsReporterImpl;

/**
 * 统计工具类
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
public class StatisticsUtil {

    private static final TemplateUsageStatisticReporter REPORTER = new TemplateUsageStatisticsReporterImpl();

    /**
     * 报告模板使用情况
     *
     * @param templateName 模板名称
     */
    public static void reportTemplateUsage(String templateName) {
        UniformFormatSettingsState settings = UniformFormatSettingsState.getInstance();
        if (settings.isEnableStatistics()) {
            REPORTER.reportUsage(templateName);
        }
    }

    /**
     * 报告文件模板使用情况
     */
    public static void reportFileTemplateUsage() {
        reportTemplateUsage("file-template");
    }

    /**
     * 报告 Live Template 使用情况
     *
     * @param templateName Live Template 名称
     */
    public static void reportLiveTemplateUsage(String templateName) {
        reportTemplateUsage("live-template-" + templateName);
    }

    /**
     * 报告代码风格应用情况
     */
    public static void reportCodeStyleUsage() {
        reportTemplateUsage("code-style");
    }
}
