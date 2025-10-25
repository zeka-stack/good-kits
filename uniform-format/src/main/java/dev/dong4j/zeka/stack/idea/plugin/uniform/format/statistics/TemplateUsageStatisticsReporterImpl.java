package dev.dong4j.zeka.stack.idea.plugin.uniform.format.statistics;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

/**
 * 模板使用统计报告器实现
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
@Slf4j
public class TemplateUsageStatisticsReporterImpl implements TemplateUsageStatisticReporter {

    private static final Logger LOG = Logger.getInstance(TemplateUsageStatisticsReporterImpl.class);

    @Override
    public void reportUsage(String template) {
        try {
            UsageModel usageModel = new UsageModel();
            usageModel.setTemplateName(template);
            usageModel.setTimestamp(System.currentTimeMillis());
            usageModel.setVersion("1.0.0");
            usageModel.setIde(ApplicationInfo.getInstance().getVersionName());
            usageModel.setIdeVersion(ApplicationInfo.getInstance().getFullVersion());
            usageModel.setPluginVersion("1.0.0");

            // 异步上报统计数据
            CompletableFuture.runAsync(() -> {
                try {
                    // 这里可以实现具体的上报逻辑
                    // 比如发送到服务器、写入本地文件等
                    logUsageStatistics(usageModel);
                } catch (Exception e) {
                    LOG.warn("Failed to report usage statistics", e);
                }
            });

        } catch (Exception e) {
            LOG.warn("Failed to create usage model", e);
        }
    }

    private void logUsageStatistics(UsageModel usageModel) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LOG.info("Template usage statistics: template=" + usageModel.getTemplateName() +
                 ", timestamp=" + timestamp +
                 ", ide=" + usageModel.getIde() +
                 ", ideVersion=" + usageModel.getIdeVersion() +
                 ", pluginVersion=" + usageModel.getPluginVersion());
    }
}
