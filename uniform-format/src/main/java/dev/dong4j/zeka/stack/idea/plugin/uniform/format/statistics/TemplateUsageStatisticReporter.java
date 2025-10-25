package dev.dong4j.zeka.stack.idea.plugin.uniform.format.statistics;

/**
 * 模板使用统计报告器接口
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
public interface TemplateUsageStatisticReporter {

    /**
     * Reports usage statistics of a template.
     *
     * @param template template
     * @since 1.0.0
     */
    void reportUsage(String template);
}
