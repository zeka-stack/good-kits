package dev.dong4j.zeka.stack.idea.plugin.ai;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;
import dev.dong4j.zeka.stack.idea.plugin.util.JavaDocBundle;

/**
 * AI 服务工厂
 *
 * <p>负责创建和管理 AI 服务提供商实例。
 * 使用工厂模式和注册机制，实现提供商的动态创建和管理。
 * 通过将提供商的创建逻辑集中在这个工厂类中，
 * 提高了代码的可维护性和扩展性。
 *
 * <p>设计模式应用：
 * <ul>
 *   <li>工厂模式：统一创建不同类型的 AI 服务提供商</li>
 *   <li>注册模式：通过静态映射管理提供商类型</li>
 *   <li>单例模式：确保工厂类只有一个实例</li>
 * </ul>
 *
 * <p>扩展性说明：
 * 添加新的 AI 服务提供商只需：
 * <ol>
 *   <li>实现 AIServiceProvider 接口</li>
 *   <li>在静态初始化块中注册提供商</li>
 *   <li>无需修改现有代码</li>
 * </ol>
 *
 * <p>使用示例：
 * <pre>
 * SettingsState settings = SettingsState.getInstance();
 * AIServiceProvider provider = AIServiceFactory.createProvider(settings);
 * String doc = provider.generateDocumentation(code, type, "java");
 * </pre>
 *
 * @author dong4j
 * @version 1.0.0
 * @since 1.0.0
 */
public class AIServiceFactory {

    private static final Map<String, Class<? extends AIServiceProvider>> PROVIDERS = new HashMap<>();

    static {
        // 注册所有支持的提供商
        PROVIDERS.put("qianwen", QianWenProvider.class);
        PROVIDERS.put("ollama", OllamaProvider.class);
        // 未来可以添加更多提供商
        // PROVIDERS.put("openai", OpenAIProvider.class);
        // PROVIDERS.put("claude", ClaudeProvider.class);
    }

    /**
     * 根据配置创建服务提供商实例
     *
     * <p>根据用户配置的提供商 ID 创建相应的 AI 服务提供商实例。
     * 通过反射机制动态创建实例，确保了良好的扩展性。
     *
     * <p>创置流程：
     * <ol>
     *   <li>从配置中获取提供商 ID</li>
     *   <li>检查配置是否已通过验证</li>
     *   <li>查找对应的提供商类</li>
     *   <li>通过反射创建实例</li>
     *   <li>返回创建的实例或 null</li>
     * </ol>
     *
     * <p>错误处理：
     * <ul>
     *   <li>配置未通过验证：记录日志并返回 null</li>
     *   <li>不支持的提供商：记录日志并返回 null</li>
     *   <li>创建失败：记录日志并返回 null</li>
     * </ul>
     *
     * @param settings 配置状态，包含用户选择的提供商和其他配置
     * @return AI 服务提供商实例，创建失败返回 null
     */
    @org.jetbrains.annotations.Nullable
    public static AIServiceProvider createProvider(@NotNull SettingsState settings) {
        String providerId = settings.aiProvider;

        // 检查配置是否已通过验证
        if (!settings.configurationVerified) {
            String error = JavaDocBundle.message("error.configuration.not.verified");
            com.intellij.openapi.diagnostic.Logger.getInstance(AIServiceFactory.class).warn(error);
            return null;
        }

        Class<? extends AIServiceProvider> providerClass = PROVIDERS.get(providerId);
        if (providerClass == null) {
            String error = "不支持的 AI 提供商: " + providerId + "。当前支持的提供商：qianwen, ollama";
            com.intellij.openapi.diagnostic.Logger.getInstance(AIServiceFactory.class).error(error);
            return null;
        }

        try {
            return providerClass.getDeclaredConstructor(SettingsState.class)
                .newInstance(settings);
        } catch (Exception e) {
            String error = "创建 AI 提供商失败: " + providerId + "。请检查配置是否正确。";
            com.intellij.openapi.diagnostic.Logger.getInstance(AIServiceFactory.class).error(error, e);
            return null;
        }
    }

    /**
     * 获取所有支持的提供商 ID
     *
     * <p>返回当前系统支持的所有 AI 服务提供商的 ID 集合。
     * 主要用于设置界面的下拉列表选项和配置验证。
     *
     * <p>返回内容：
     * <ul>
     *   <li>qianwen: 通义千问提供商</li>
     *   <li>ollama: Ollama 本地模型提供商</li>
     *   <li>未来可能添加更多提供商</li>
     * </ul>
     *
     * @return 提供商 ID 集合，用于标识支持的提供商类型
     */
    @NotNull
    public static Set<String> getSupportedProviders() {
        return PROVIDERS.keySet();
    }

    /**
     * 检查提供商是否支持
     *
     * <p>检查指定的提供商 ID 是否在支持列表中。
     * 主要用于配置验证和用户输入检查。
     *
     * <p>使用场景：
     * <ul>
     *   <li>配置保存时验证提供商有效性</li>
     *   <li>UI 显示时检查提供商是否可用</li>
     *   <li>动态功能启用控制</li>
     * </ul>
     *
     * @param providerId 提供商 ID，如 "qianwen", "ollama"
     * @return 如果支持返回 true，否则返回 false
     */
    public static boolean isProviderSupported(@NotNull String providerId) {
        return PROVIDERS.containsKey(providerId);
    }

    /**
     * 获取提供商的显示名称
     *
     * <p>根据提供商 ID 获取对应的显示名称，
     * 用于 UI 界面显示和用户友好提示。
     * 通过创建临时实例调用 getProviderName() 方法获取名称。
     *
     * <p>注意：此方法会创建临时实例，频繁调用可能影响性能。
     * 在未来版本中可以考虑优化实现。
     *
     * <p>返回示例：
     * <ul>
     *   <li>qianwen → "通义千问 (QianWen)"</li>
     *   <li>ollama → "Ollama (本地)"</li>
     * </ul>
     *
     * @param providerId 提供商 ID
     * @return 显示名称，如果获取失败则返回提供商 ID
     */
    @NotNull
    public static String getProviderName(@NotNull String providerId) {
        // 创建临时实例获取名称（未来可以优化）
        try {
            SettingsState tempSettings = new SettingsState();
            tempSettings.aiProvider = providerId;
            // 临时允许创建未验证的提供商用于获取显示名称
            tempSettings.configurationVerified = true;
            AIServiceProvider provider = createProvider(tempSettings);
            if (provider == null) {
                return providerId;
            }
            return provider.getProviderName();
        } catch (Exception e) {
            return providerId;
        }
    }

    /**
     * 获取所有可用的服务提供商实例
     *
     * <p>返回所有已配置且通过验证的 AI 服务提供商实例列表。
     * 此方法用于支持多提供商并行处理场景。
     *
     * <p>可用条件：
     * <ul>
     *   <li>配置已通过验证（configurationVerified = true）</li>
     *   <li>提供商 ID 在支持列表中</li>
     *   <li>能够成功创建提供商实例</li>
     * </ul>
     *
     * <p>当前实现：
     * <ul>
     *   <li>目前系统只支持单个提供商配置</li>
     *   <li>返回的列表最多包含一个元素（当前配置的提供商）</li>
     *   <li>如果当前配置未验证或创建失败，返回空列表</li>
     * </ul>
     *
     * <p>未来扩展：
     * <ul>
     *   <li>支持多个提供商配置的存储</li>
     *   <li>支持并行调用多个提供商</li>
     *   <li>支持负载均衡和故障转移</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>
     * List&lt;AIServiceProvider&gt; providers = AIServiceFactory.getAvailableProviders();
     * if (!providers.isEmpty()) {
     *     // 并行调用多个提供商
     *     providers.parallelStream()
     *         .map(provider -&gt; provider.generateDocumentation(code, type, language))
     *         .findFirst();
     * }
     * </pre>
     *
     * @return 可用的服务提供商列表，如果没有可用的提供商则返回空列表
     * @see #createProvider(SettingsState)
     */
    @NotNull
    public static List<AIServiceProvider> getAvailableProviders() {
        List<AIServiceProvider> providers = new ArrayList<>();

        // 获取当前配置
        SettingsState settings = SettingsState.getInstance();

        // 尝试创建提供商实例
        AIServiceProvider provider = createProvider(settings);
        if (provider != null) {
            providers.add(provider);
        }

        return providers;
    }

    /**
     * 检查是否有可用的服务提供商
     *
     * <p>快速检查当前是否至少有一个可用的 AI 服务提供商。
     * 此方法比 getAvailableProviders() 更高效，因为它只检查而不创建实例。
     *
     * <p>检查条件：
     * <ul>
     *   <li>配置已通过验证</li>
     *   <li>提供商 ID 有效</li>
     * </ul>
     *
     * @return 如果有可用的提供商返回 true，否则返回 false
     * @see #getAvailableProviders()
     */
    public static boolean hasAvailableProvider() {
        SettingsState settings = SettingsState.getInstance();
        return settings.configurationVerified
               && settings.aiProvider != null
               && isProviderSupported(settings.aiProvider);
    }

    /**
     * 获取可用提供商的数量
     *
     * <p>返回当前可用的 AI 服务提供商数量。
     * 用于统计和负载均衡决策。
     *
     * <p>当前实现：
     * <ul>
     *   <li>返回 0 或 1（因为只支持单个提供商配置）</li>
     *   <li>未来扩展后可能返回更大的数值</li>
     * </ul>
     *
     * @return 可用提供商的数量
     * @see #hasAvailableProvider()
     */
    public static int getAvailableProviderCount() {
        return hasAvailableProvider() ? 1 : 0;
    }
}

