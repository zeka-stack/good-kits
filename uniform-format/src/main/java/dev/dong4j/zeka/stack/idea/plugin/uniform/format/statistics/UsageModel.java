package dev.dong4j.zeka.stack.idea.plugin.uniform.format.statistics;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * 使用统计模型
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
@Data
public class UsageModel implements Serializable {
    /** serialVersionUID */
    @Serial
    private static final long serialVersionUID = 6226996001429878903L;

    /** Template name */
    private String templateName;

    /** Timestamp */
    private Long timestamp;

    /** Version */
    private String version;

    /** Ide */
    private String ide;

    /** Ide version */
    private String ideVersion;

    /** Plugin version */
    private String pluginVersion;
}
