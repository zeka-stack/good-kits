package dev.dong4j.zeka.stack.idea.plugin.ai;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;

/**
 * Ollama 服务提供商
 *
 * <p>Ollama 是一个本地运行大语言模型的工具，支持多种开源模型。
 * 不需要 API Key，所有模型在本地运行，数据完全私有。
 * 适合对数据隐私有严格要求或希望离线使用的用户。
 *
 * <p>服务优势：
 * <ul>
 *   <li>数据隐私：所有处理都在本地进行，数据不会上传到外部服务器</li>
 *   <li>无 API 成本：无需支付 API 调用费用</li>
 *   <li>离线可用：网络连接不影响使用</li>
 *   <li>开源模型：支持多种开源大语言模型</li>
 * </ul>
 *
 * <p>使用说明：
 * <ol>
 *   <li>安装 Ollama：<a href="https://ollama.ai">https://ollama.ai</a></li>
 *   <li>拉取模型：{@code ollama pull qwen:7b}</li>
 *   <li>启动服务：{@code ollama serve}</li>
 *   <li>在插件中配置 Base URL（默认：http://localhost:11434/v1）</li>
 *   <li>在设置面板中输入模型名称（可输入任何已安装的模型）</li>
 * </ol>
 *
 * <p><b>查看已安装模型：</b> {@code ollama list}
 *
 * <p>推荐模型（仅供参考，您可以使用任何模型）：
 * <ul>
 *   <li>{@code qwen:7b} - 通义千问 7B，中文支持好</li>
 *   <li>{@code codellama:7b} - 代码专用模型，适合代码文档生成</li>
 *   <li>{@code deepseek-coder:6.7b} - 深度求索代码模型</li>
 *   <li>{@code llama2:7b} - 通用模型</li>
 * </ul>
 *
 * <p>性能考虑：
 * <ul>
 *   <li>模型大小影响内存占用和推理速度</li>
 *   <li>较大的模型通常提供更好的生成质量</li>
 *   <li>需要足够的系统资源支持模型运行</li>
 * </ul>
 *
 * @author dong4j
 * @version 1.0.0
 * @see <a href="https://ollama.ai/library">Ollama 模型库（查看所有可用模型）</a>
 * @see AICompatibleProvider
 * @since 1.0.0
 */
public class OllamaProvider extends AICompatibleProvider {

    private static final String PROVIDER_ID = "ollama";
    private static final String PROVIDER_NAME = "Ollama (本地)";
    private static final String DEFAULT_BASE_URL = "http://localhost:11434/v1";
    private static final String DEFAULT_MODEL = "qwen:7b";

    /**
     * 推荐的常用模型列表（仅作为参考）
     * 用户可以使用任何通过 ollama pull 下载的模型
     *
     * <p>模型分类：
     * <ul>
     *   <li>通义千问系列：优秀的中文处理能力</li>
     *   <li>代码专用模型：针对代码理解和生成优化</li>
     *   <li>通用模型：适用于多种任务的通用模型</li>
     * </ul>
     *
     * <p>选择建议：
     * <ul>
     *   <li>中文文档生成：推荐 qwen 系列</li>
     *   <li>代码文档生成：推荐 codellama 或 deepseek-coder</li>
     *   <li>通用任务：推荐 llama2 或 mistral</li>
     * </ul>
     *
     * <p>注意：模型列表会随 Ollama 更新而变化，
     * 建议查看官方文档获取最新信息。
     */
    private static final List<String> SUGGESTED_MODELS = Arrays.asList(
        // 通义千问系列（推荐用于中文）
        "qwen:7b",
        "qwen:14b",

        // 代码专用模型（推荐用于代码文档生成）
        "codellama:7b",
        "deepseek-coder:6.7b",

        // 通用模型
        "llama2:7b",
        "mistral:7b"
                                                                      );

    public OllamaProvider(SettingsState settings) {
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
     * 返回推荐的模型列表
     *
     * <p>返回推荐的常用模型列表，供用户在设置界面中选择。
     * 这些模型经过测试，能够很好地支持文档生成任务。
     *
     * <p>注意：这只是推荐列表，用户可以使用任何已安装的 Ollama 模型。
     * 可以通过 `ollama list` 命令查看本地已安装的模型。
     *
     * <p>列表内容：
     * <ul>
     *   <li>qwen:7b/14b: 通义千问系列，中文支持好</li>
     *   <li>codellama:7b: 代码专用模型</li>
     *   <li>deepseek-coder:6.7b: 深度求索代码模型</li>
     *   <li>llama2:7b: 通用模型</li>
     *   <li>mistral:7b: 通用模型</li>
     * </ul>
     *
     * @return 推荐的模型列表（作为参考）
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
        // Ollama 本地服务不需要 API Key
        // 所有处理都在本地进行，无需外部认证
        return false;
    }
}

