package dev.dong4j.zeka.stack.idea.plugin.ai;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * AI 服务提供商类型枚举
 *
 * <p>统一管理所有支持的 AI 服务提供商配置信息。
 * 包括提供商标识符、显示名称、默认配置等。
 * 使用枚举可以避免字符串比较，提高代码的可维护性和类型安全性。
 *
 * <p>设计原则：
 * <ul>
 *   <li>单一职责：每个枚举值代表一个提供商</li>
 *   <li>配置集中：所有提供商配置信息集中管理</li>
 *   <li>易于扩展：新增提供商只需添加枚举值</li>
 *   <li>类型安全：避免字符串比较的错误</li>
 * </ul>
 *
 * <p>支持的提供商：
 * <ul>
 *   <li>QIANWEN: 通义千问服务</li>
 *   <li>OLLAMA: Ollama 本地服务</li>
 *   <li>CUSTOM: 自定义服务（OpenAI 兼容）</li>
 * </ul>
 *
 * @author dong4j
 * @version 1.0.0
 * @since 1.0.0
 */
public enum AIProviderType {

    /**
     * 通义千问服务提供商
     *
     * <p>阿里云提供的 AI 服务，支持多种模型。
     * 需要 API Key 进行身份验证。
     *
     * <p>特点：
     * <ul>
     *   <li>中文支持优秀</li>
     *   <li>模型性能稳定</li>
     *   <li>需要 API Key</li>
     *   <li>支持多种模型规格</li>
     * </ul>
     */
    QIANWEN(
        "qianwen",
        "通义千问 (QianWen)",
        "https://dashscope.aliyuncs.com/compatible-mode/v1",
        "qwen3-8b",
        true,
        Arrays.asList(
            "qwen3-32b",
            "qwen3-14b",
            "qwen3-8b",
            "qwen3-4b"
                     )
    ),

    /**
     * Ollama 本地服务提供商
     *
     * <p>本地运行的开源大语言模型服务。
     * 不需要 API Key，所有处理都在本地进行。
     *
     * <p>特点：
     * <ul>
     *   <li>数据隐私：本地处理</li>
     *   <li>无 API 成本</li>
     *   <li>离线可用</li>
     *   <li>支持多种开源模型</li>
     * </ul>
     */
    OLLAMA(
        "ollama",
        "Ollama (本地)",
        "http://localhost:11434/v1",
        "qwen:7b",
        false,
        Arrays.asList(
            "qwen:7b",
            "qwen:14b",
            "codellama:7b",
            "deepseek-coder:6.7b",
            "llama2:7b",
            "mistral:7b"
                     )
    ),

    /**
     * 自定义服务提供商
     *
     * <p>兼容 OpenAI API 的自定义服务提供商。
     * 支持任何提供 OpenAI 兼容 API 的服务。
     *
     * <p>特点：
     * <ul>
     *   <li>兼容 OpenAI API</li>
     *   <li>支持多种第三方服务</li>
     *   <li>需要 API Key</li>
     *   <li>高度可定制</li>
     * </ul>
     */
    CUSTOM(
        "custom",
        "自定义服务 (OpenAI 兼容)",
        "https://api.openai.com/v1",
        "gpt-3.5-turbo",
        true,
        Arrays.asList(
            "gpt-3.5-turbo",
            "gpt-3.5-turbo-16k",
            "gpt-4",
            "gpt-4-turbo",
            "gpt-4-turbo-preview",
            "gpt-4-32k",
            "gpt-35-turbo",
            "claude-3-sonnet",
            "claude-3-opus",
            "gemini-pro"
                     )
    );

    /**
     * 提供商标识符
     *
     * <p>用于配置存储和识别的唯一标识符。
     * 与原有的字符串标识符保持兼容。
     */
    private final String providerId;

    /**
     * 提供商显示名称
     *
     * <p>在 UI 界面中显示的用户友好名称。
     * 支持中文显示，便于用户理解。
     */
    private final String displayName;

    /**
     * 默认 Base URL
     *
     * <p>该提供商的默认 API 服务地址。
     * 用户可以在设置中修改此地址。
     */
    private final String defaultBaseUrl;

    /**
     * 默认模型名称
     *
     * <p>该提供商推荐的默认模型。
     * 通常是性能和效果平衡的最佳选择。
     */
    private final String defaultModel;

    /**
     * 是否需要 API Key
     *
     * <p>标识该提供商是否需要 API Key 进行身份验证。
     * 某些本地服务（如 Ollama）不需要 API Key。
     */
    private final boolean requiresApiKey;

    /**
     * 支持的模型列表
     *
     * <p>该提供商支持的模型名称列表。
     * 包含推荐的常用模型，用户也可以使用其他支持的模型。
     */
    private final List<String> supportedModels;

    /**
     * 构造函数
     *
     * @param providerId      提供商标识符
     * @param displayName     显示名称
     * @param defaultBaseUrl  默认 Base URL
     * @param defaultModel    默认模型
     * @param requiresApiKey  是否需要 API Key
     * @param supportedModels 支持的模型列表
     */
    AIProviderType(@NotNull String providerId,
                   @NotNull String displayName,
                   @NotNull String defaultBaseUrl,
                   @NotNull String defaultModel,
                   boolean requiresApiKey,
                   @NotNull List<String> supportedModels) {
        this.providerId = providerId;
        this.displayName = displayName;
        this.defaultBaseUrl = defaultBaseUrl;
        this.defaultModel = defaultModel;
        this.requiresApiKey = requiresApiKey;
        this.supportedModels = supportedModels;
    }

    /**
     * 获取提供商标识符
     *
     * @return 提供商标识符
     */
    @NotNull
    public String getProviderId() {
        return providerId;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取默认 Base URL
     *
     * @return 默认 Base URL
     */
    @NotNull
    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    /**
     * 获取默认模型
     *
     * @return 默认模型名称
     */
    @NotNull
    public String getDefaultModel() {
        return defaultModel;
    }

    /**
     * 是否需要 API Key
     *
     * @return 如果需要 API Key 返回 true
     */
    public boolean requiresApiKey() {
        return requiresApiKey;
    }

    /**
     * 获取支持的模型列表
     *
     * @return 支持的模型列表
     */
    @NotNull
    public List<String> getSupportedModels() {
        return supportedModels;
    }

    /**
     * 根据提供商标识符获取枚举值
     *
     * <p>用于从配置中读取的字符串标识符转换为枚举值。
     * 提供向后兼容性支持。
     *
     * @param providerId 提供商标识符
     * @return 对应的枚举值，如果未找到返回 null
     */
    @org.jetbrains.annotations.Nullable
    public static AIProviderType fromProviderId(@NotNull String providerId) {
        for (AIProviderType type : values()) {
            if (type.providerId.equals(providerId)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查提供商标识符是否有效
     *
     * @param providerId 提供商标识符
     * @return 如果有效返回 true
     */
    public static boolean isValidProviderId(@NotNull String providerId) {
        return fromProviderId(providerId) != null;
    }

    /**
     * 获取所有提供商的标识符列表
     *
     * @return 所有提供商的标识符列表
     */
    @NotNull
    public static List<String> getAllProviderIds() {
        return Arrays.stream(values())
            .map(AIProviderType::getProviderId)
            .toList();
    }

    /**
     * 获取所有提供商的显示名称列表
     *
     * @return 所有提供商的显示名称列表
     */
    @NotNull
    public static List<String> getAllDisplayNames() {
        return Arrays.stream(values())
            .map(AIProviderType::getDisplayName)
            .toList();
    }
}
