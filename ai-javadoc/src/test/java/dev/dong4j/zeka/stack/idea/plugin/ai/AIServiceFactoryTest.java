package dev.dong4j.zeka.stack.idea.plugin.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AIServiceFactory 单元测试
 */
@DisplayName("AIServiceFactory 单元测试")
public class AIServiceFactoryTest {

    private SettingsState settings;

    @BeforeEach
    void setUp() {
        settings = new SettingsState();
    }

    @Test
    @DisplayName("测试创建千问提供商")
    void testCreateProvider_qianwen() {
        settings.aiProvider = "qianwen";
        settings.modelName = "qwen-max";
        settings.baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        settings.apiKey = "test-api-key";

        AIServiceProvider provider = AIServiceFactory.createProvider(settings);

        assertThat(provider).isNotNull();
        assertThat(provider).isInstanceOf(QianWenProvider.class);
        assertThat(provider.getProviderId()).isEqualTo("qianwen");
    }

    @Test
    @DisplayName("测试创建 Ollama 提供商")
    void testCreateProvider_ollama() {
        settings.aiProvider = "ollama";
        settings.modelName = "llama2";
        settings.baseUrl = "http://localhost:11434";
        settings.apiKey = "";

        AIServiceProvider provider = AIServiceFactory.createProvider(settings);

        assertThat(provider).isNotNull();
        assertThat(provider).isInstanceOf(OllamaProvider.class);
        assertThat(provider.getProviderId()).isEqualTo("ollama");
    }

    @Test
    @DisplayName("测试创建不支持的提供商")
    void testCreateProvider_unsupported() {
        settings.aiProvider = "unknown-provider";

        assertThatThrownBy(() -> AIServiceFactory.createProvider(settings))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported AI provider: unknown-provider");
    }

    @Test
    @DisplayName("测试创建提供商 - null 值")
    void testCreateProvider_nullProvider() {
        settings.aiProvider = null;

        assertThatThrownBy(() -> AIServiceFactory.createProvider(settings))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported AI provider: null");
    }

    @Test
    @DisplayName("测试获取支持的提供商列表")
    void testGetSupportedProviders() {
        Set<String> providers = AIServiceFactory.getSupportedProviders();

        assertThat(providers).isNotNull();
        assertThat(providers).isNotEmpty();
        assertThat(providers).contains("qianwen", "ollama");
    }

    @Test
    @DisplayName("测试检查提供商是否支持 - 千问")
    void testIsProviderSupported_qianwen() {
        assertThat(AIServiceFactory.isProviderSupported("qianwen")).isTrue();
    }

    @Test
    @DisplayName("测试检查提供商是否支持 - Ollama")
    void testIsProviderSupported_ollama() {
        assertThat(AIServiceFactory.isProviderSupported("ollama")).isTrue();
    }

    @Test
    @DisplayName("测试检查提供商是否支持 - 不支持的提供商")
    void testIsProviderSupported_unsupported() {
        assertThat(AIServiceFactory.isProviderSupported("openai")).isFalse();
        assertThat(AIServiceFactory.isProviderSupported("claude")).isFalse();
        assertThat(AIServiceFactory.isProviderSupported("unknown")).isFalse();
    }

    @Test
    @DisplayName("测试检查提供商是否支持 - null 值")
    void testIsProviderSupported_null() {
        assertThat(AIServiceFactory.isProviderSupported(null)).isFalse();
    }

    @Test
    @DisplayName("测试检查提供商是否支持 - 空字符串")
    void testIsProviderSupported_emptyString() {
        assertThat(AIServiceFactory.isProviderSupported("")).isFalse();
    }

    @Test
    @DisplayName("测试获取提供商名称 - 千问")
    void testGetProviderName_qianwen() {
        String name = AIServiceFactory.getProviderName("qianwen");
        assertThat(name).isNotNull();
        assertThat(name).isNotEmpty();
    }

    @Test
    @DisplayName("测试获取提供商名称 - Ollama")
    void testGetProviderName_ollama() {
        String name = AIServiceFactory.getProviderName("ollama");
        assertThat(name).isNotNull();
        assertThat(name).isNotEmpty();
    }

    @Test
    @DisplayName("测试获取提供商名称 - 不支持的提供商")
    void testGetProviderName_unsupported() {
        String name = AIServiceFactory.getProviderName("unknown");
        // 对于不支持的提供商，返回 providerId 本身
        assertThat(name).isEqualTo("unknown");
    }

    @Test
    @DisplayName("测试创建的提供商实例配置正确 - 千问")
    void testCreatedProvider_hasCorrectConfiguration_qianwen() {
        settings.aiProvider = "qianwen";
        settings.modelName = "qwen-max";
        settings.baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        settings.apiKey = "test-api-key";

        AIServiceProvider provider = AIServiceFactory.createProvider(settings);

        assertThat(provider.getProviderId()).isEqualTo("qianwen");
        assertThat(provider.getProviderName()).isNotEmpty();
        assertThat(provider.requiresApiKey()).isTrue();
        assertThat(provider.getDefaultModel()).isNotEmpty();
        assertThat(provider.getDefaultBaseUrl()).isNotEmpty();
        assertThat(provider.getSupportedModels()).isNotEmpty();
    }

    @Test
    @DisplayName("测试创建的提供商实例配置正确 - Ollama")
    void testCreatedProvider_hasCorrectConfiguration_ollama() {
        settings.aiProvider = "ollama";
        settings.modelName = "llama2";
        settings.baseUrl = "http://localhost:11434";
        settings.apiKey = "";

        AIServiceProvider provider = AIServiceFactory.createProvider(settings);

        assertThat(provider.getProviderId()).isEqualTo("ollama");
        assertThat(provider.getProviderName()).isNotEmpty();
        assertThat(provider.requiresApiKey()).isFalse();
        assertThat(provider.getDefaultModel()).isNotEmpty();
        assertThat(provider.getDefaultBaseUrl()).isNotEmpty();
        assertThat(provider.getSupportedModels()).isNotEmpty();
    }

    @Test
    @DisplayName("测试多次创建提供商实例")
    void testCreateMultipleInstances() {
        settings.aiProvider = "qianwen";
        settings.modelName = "qwen-max";
        settings.baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        settings.apiKey = "test-api-key";

        AIServiceProvider provider1 = AIServiceFactory.createProvider(settings);
        AIServiceProvider provider2 = AIServiceFactory.createProvider(settings);

        // 每次创建应该返回新实例
        assertThat(provider1).isNotSameAs(provider2);
        // 但类型应该相同
        assertThat(provider1.getClass()).isEqualTo(provider2.getClass());
    }

    @Test
    @DisplayName("测试切换提供商")
    void testSwitchProviders() {
        // 创建千问提供商
        settings.aiProvider = "qianwen";
        settings.modelName = "qwen-max";
        settings.baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        settings.apiKey = "test-api-key";
        AIServiceProvider qianwenProvider = AIServiceFactory.createProvider(settings);

        assertThat(qianwenProvider.getProviderId()).isEqualTo("qianwen");

        // 切换到 Ollama
        settings.aiProvider = "ollama";
        settings.modelName = "llama2";
        settings.baseUrl = "http://localhost:11434";
        settings.apiKey = "";
        AIServiceProvider ollamaProvider = AIServiceFactory.createProvider(settings);

        assertThat(ollamaProvider.getProviderId()).isEqualTo("ollama");
        assertThat(ollamaProvider.getClass()).isNotEqualTo(qianwenProvider.getClass());
    }

    @Test
    @DisplayName("测试支持的提供商数量")
    void testSupportedProvidersCount() {
        Set<String> providers = AIServiceFactory.getSupportedProviders();
        // 目前支持 qianwen 和 ollama
        assertThat(providers.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("测试获取支持的提供商返回不可变集合")
    void testGetSupportedProviders_returnsSetWithExpectedProviders() {
        Set<String> providers = AIServiceFactory.getSupportedProviders();

        assertThat(providers)
            .isNotNull()
            .contains("qianwen", "ollama");
    }
}

