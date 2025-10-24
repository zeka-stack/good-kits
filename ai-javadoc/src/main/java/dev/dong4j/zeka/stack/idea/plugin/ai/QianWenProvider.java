package dev.dong4j.zeka.stack.idea.plugin.ai;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;

/**
 * 通义千问服务提供商
 *
 * <p>阿里云提供的大语言模型服务，支持 OpenAI 兼容模式。
 * 作为阿里云的核心 AI 服务，通义千问提供了多种不同规模和用途的模型，
 * 适用于各种场景的文档生成任务。
 *
 * <p>服务特点：
 * <ul>
 *   <li>多种模型选择，满足不同性能和成本需求</li>
 *   <li>支持中文和多语言处理</li>
 *   <li>高准确性和稳定性</li>
 *   <li>完善的文档和社区支持</li>
 * </ul>
 *
 * <p>使用说明：
 * <ol>
 *   <li>在阿里云申请 API Key：<a href="https://dashscope.console.aliyun.com/">DashScope 控制台</a></li>
 *   <li>在设置中配置 API Key</li>
 *   <li>选择或输入模型名称（可输入官方文档中的任何模型）</li>
 * </ol>
 *
 * <p>常用模型（仅供参考，您可以使用任何官方支持的模型）：
 * <ul>
 *   <li>{@code qwen-max} - 最强大的模型（推荐）</li>
 *   <li>{@code qwen-plus} - 性价比较高</li>
 *   <li>{@code qwen-turbo} - 速度最快</li>
 * </ul>
 *
 * <p><b>更多模型：</b> 请参考官方文档获取完整模型列表和详细说明。
 *
 * @author dong4j
 * @version 1.0.0
 * @see <a href="https://help.aliyun.com/zh/dashscope/">通义千问官方文档</a>
 * @see AICompatibleProvider
 * @since 1.0.0
 */
public class QianWenProvider extends AICompatibleProvider {

    private static final String PROVIDER_ID = "qianwen";
    private static final String PROVIDER_NAME = "通义千问 (QianWen)";
    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private static final String DEFAULT_MODEL = "qwen-max";

    /**
     * 常用模型列表（仅作为参考）
     * 通义千问支持更多模型，用户可以根据需要使用官方文档中的任何模型
     *
     * <p>模型选择建议：
     * <ul>
     *   <li>qwen-max: 适用于对质量要求高的场景</li>
     *   <li>qwen-plus: 适用于平衡质量和成本的场景</li>
     *   <li>qwen-turbo: 适用于对速度要求高的场景</li>
     * </ul>
     *
     * <p>注意：模型列表可能随时间更新，建议参考官方文档获取最新信息。
     */
    private static final List<String> SUGGESTED_MODELS = Arrays.asList(
        "qwen-max",          // 最新最强大的模型（推荐）
        "qwen-plus",         // 性价比较高
        "qwen-turbo"         // 速度最快
                                                                      );

    public QianWenProvider(SettingsState settings) {
        super(settings);
    }

    @NotNull
    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @NotNull
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * 返回常用模型列表
     *
     * <p>返回推荐的常用模型列表，供用户在设置界面中选择。
     * 这些模型经过测试，能够很好地支持文档生成任务。
     *
     * <p>注意：这只是常用模型列表，用户可以使用通义千问支持的任何模型。
     * 请参考官方文档获取完整的模型列表。
     *
     * <p>列表内容：
     * <ul>
     *   <li>qwen-max: 最新最强大的模型（推荐）</li>
     *   <li>qwen-plus: 性价比较高</li>
     *   <li>qwen-turbo: 速度最快</li>
     * </ul>
     *
     * @return 常用模型列表（作为参考）
     */
    @NotNull
    @Override
    public List<String> getSupportedModels() {
        return SUGGESTED_MODELS;
    }

    @NotNull
    @Override
    public String getDefaultModel() {
        return DEFAULT_MODEL;
    }

    @NotNull
    @Override
    public String getDefaultBaseUrl() {
        return DEFAULT_BASE_URL;
    }

    @Override
    public boolean requiresApiKey() {
        return true;
    }
}

