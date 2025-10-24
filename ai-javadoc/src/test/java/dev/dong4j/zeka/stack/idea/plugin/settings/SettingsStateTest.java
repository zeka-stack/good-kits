package dev.dong4j.zeka.stack.idea.plugin.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.dong4j.zeka.stack.idea.plugin.ai.AIProviderType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SettingsState 单元测试
 */
@DisplayName("SettingsState 单元测试")
public class SettingsStateTest {

    private SettingsState settings;

    @BeforeEach
    void setUp() {
        settings = new SettingsState();
    }

    @Test
    @DisplayName("测试默认配置值")
    void testDefaultValues() {
        assertThat(settings.aiProvider).isEqualTo(AIProviderType.QIANWEN.getProviderId());
        assertThat(settings.modelName).isEqualTo("qwen3-8b");
        assertThat(settings.baseUrl).isEqualTo("https://dashscope.aliyuncs.com/compatible-mode/v1");
        assertThat(settings.apiKey).isEmpty();

        // 功能配置
        assertThat(settings.generateForClass).isTrue();
        assertThat(settings.generateForMethod).isTrue();
        assertThat(settings.generateForField).isFalse();
        assertThat(settings.skipExisting).isTrue();

        // 高级配置
        assertThat(settings.maxRetries).isEqualTo(3);
        assertThat(settings.timeout).isEqualTo(30000);
        assertThat(settings.waitDuration).isEqualTo(5000);
        assertThat(settings.temperature).isEqualTo(0.1);
        assertThat(settings.maxTokens).isEqualTo(1000);
        assertThat(settings.topP).isEqualTo(0.9);
        assertThat(settings.topK).isEqualTo(50);
        assertThat(settings.presencePenalty).isEqualTo(0.0);
        assertThat(settings.concurrency).isEqualTo(3);
        assertThat(settings.verboseLogging).isFalse();

        // 支持的语言
        assertThat(settings.supportedLanguages).containsOnly("java");
    }

    @Test
    @DisplayName("测试配置验证 - 有效配置")
    void testIsValid_withValidConfiguration() {
        settings.aiProvider = AIProviderType.QIANWEN.getProviderId();
        settings.modelName = "qwen-max";
        settings.baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        settings.apiKey = "valid-api-key";

        assertThat(settings.isValid()).isTrue();
    }

    @Test
    @DisplayName("测试配置验证 - 缺少 AI Provider")
    void testIsValid_withMissingAIProvider() {
        settings.aiProvider = "";
        settings.modelName = "qwen-max";
        settings.baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        settings.apiKey = "valid-api-key";

        assertThat(settings.isValid()).isFalse();
    }

    @Test
    @DisplayName("测试配置验证 - 缺少模型名称")
    void testIsValid_withMissingModelName() {
        settings.aiProvider = AIProviderType.QIANWEN.getProviderId();
        settings.modelName = "";
        settings.baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        settings.apiKey = "valid-api-key";

        assertThat(settings.isValid()).isFalse();
    }

    @Test
    @DisplayName("测试配置验证 - 缺少 Base URL")
    void testIsValid_withMissingBaseUrl() {
        settings.aiProvider = AIProviderType.QIANWEN.getProviderId();
        settings.modelName = "qwen-max";
        settings.baseUrl = "";
        settings.apiKey = "valid-api-key";

        assertThat(settings.isValid()).isFalse();
    }

    @Test
    @DisplayName("测试配置验证 - 缺少 API Key（需要时）")
    void testIsValid_withMissingApiKey_whenRequired() {
        settings.aiProvider = AIProviderType.QIANWEN.getProviderId(); // 需要 API Key
        settings.modelName = "qwen-max";
        settings.baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        settings.apiKey = "";

        assertThat(settings.isValid()).isFalse();
    }

    @Test
    @DisplayName("测试配置验证 - Ollama 不需要 API Key")
    void testIsValid_withOllama_noApiKeyRequired() {
        settings.aiProvider = AIProviderType.OLLAMA.getProviderId();
        settings.modelName = "llama2";
        settings.baseUrl = "http://localhost:11434";
        settings.apiKey = "";

        assertThat(settings.isValid()).isTrue();
    }

    @Test
    @DisplayName("测试是否需要 API Key")
    void testRequiresApiKey() {
        settings.aiProvider = AIProviderType.QIANWEN.getProviderId();
        assertThat(settings.requiresApiKey()).isTrue();

        settings.aiProvider = AIProviderType.OLLAMA.getProviderId();
        assertThat(settings.requiresApiKey()).isFalse();
    }

    @Test
    @DisplayName("测试语言支持检测")
    void testIsLanguageSupported() {
        assertThat(settings.isLanguageSupported("java")).isTrue();
        assertThat(settings.isLanguageSupported("Java")).isTrue();
        assertThat(settings.isLanguageSupported("JAVA")).isTrue();
        assertThat(settings.isLanguageSupported("kotlin")).isFalse();
        assertThat(settings.isLanguageSupported("python")).isFalse();
    }

    @Test
    @DisplayName("测试重置为默认配置")
    void testResetToDefaults() {
        // 修改配置
        settings.aiProvider = AIProviderType.OLLAMA.getProviderId();
        settings.modelName = "llama2";
        settings.baseUrl = "http://localhost:11434";
        settings.apiKey = "test-key";
        settings.generateForClass = false;
        settings.generateForMethod = false;
        settings.generateForField = true;
        settings.maxRetries = 5;
        settings.temperature = 0.5;
        settings.topP = 0.5;
        settings.topK = 20;
        settings.presencePenalty = 0.5;

        // 重置
        settings.resetToDefaults();

        // 验证
        assertThat(settings.aiProvider).isEqualTo(AIProviderType.QIANWEN.getProviderId());
        assertThat(settings.modelName).isEqualTo("qwen3-8b");
        assertThat(settings.baseUrl).isEqualTo("https://dashscope.aliyuncs.com/compatible-mode/v1");
        assertThat(settings.apiKey).isEmpty();
        assertThat(settings.generateForClass).isTrue();
        assertThat(settings.generateForMethod).isTrue();
        assertThat(settings.generateForField).isFalse();
        assertThat(settings.maxRetries).isEqualTo(3);
        assertThat(settings.temperature).isEqualTo(0.1);
        assertThat(settings.topP).isEqualTo(0.9);
        assertThat(settings.topK).isEqualTo(50);
        assertThat(settings.presencePenalty).isEqualTo(0.0);
    }

    @Test
    @DisplayName("测试配置复制")
    void testCopy() {
        // 设置原始配置
        settings.aiProvider = AIProviderType.OLLAMA.getProviderId();
        settings.modelName = "llama2";
        settings.baseUrl = "http://localhost:11434";
        settings.apiKey = "test-key";
        settings.generateForField = true;
        settings.maxRetries = 5;
        settings.temperature = 0.5;
        settings.topP = 0.5;
        settings.topK = 20;
        settings.presencePenalty = 0.5;

        // 复制
        SettingsState copy = settings.copy();

        // 验证复制的值
        assertThat(copy.aiProvider).isEqualTo(settings.aiProvider);
        assertThat(copy.modelName).isEqualTo(settings.modelName);
        assertThat(copy.baseUrl).isEqualTo(settings.baseUrl);
        assertThat(copy.apiKey).isEqualTo(settings.apiKey);
        assertThat(copy.generateForField).isEqualTo(settings.generateForField);
        assertThat(copy.maxRetries).isEqualTo(settings.maxRetries);
        assertThat(copy.temperature).isEqualTo(settings.temperature);
        assertThat(copy.topP).isEqualTo(settings.topP);
        assertThat(copy.topK).isEqualTo(settings.topK);
        assertThat(copy.presencePenalty).isEqualTo(settings.presencePenalty);

        // 验证是不同的对象
        assertThat(copy).isNotSameAs(settings);

        // 修改副本不影响原始对象
        copy.aiProvider = AIProviderType.QIANWEN.getProviderId();
        assertThat(settings.aiProvider).isEqualTo(AIProviderType.OLLAMA.getProviderId());
    }

    @Test
    @DisplayName("测试获取默认 Prompt 模板")
    void testDefaultPromptTemplates() {
        String classPrompt = SettingsState.getDefaultClassPromptTemplate();
        assertThat(classPrompt).isNotEmpty();
        assertThat(classPrompt).contains("JavaDoc");
        assertThat(classPrompt).contains("%s");

        String methodPrompt = SettingsState.getDefaultMethodPromptTemplate();
        assertThat(methodPrompt).isNotEmpty();
        assertThat(methodPrompt).contains("JavaDoc");
        assertThat(methodPrompt).contains("%s");
        assertThat(methodPrompt).contains("@param");
        assertThat(methodPrompt).contains("@return");

        String fieldPrompt = SettingsState.getDefaultFieldPromptTemplate();
        assertThat(fieldPrompt).isNotEmpty();
        assertThat(fieldPrompt).contains("JavaDoc");
        assertThat(fieldPrompt).contains("%s");

        String testPrompt = SettingsState.getDefaultTestPromptTemplate();
        assertThat(testPrompt).isNotEmpty();
        assertThat(testPrompt).contains("测试");
        assertThat(testPrompt).contains("%s");
    }

    @Test
    @DisplayName("测试 Prompt 模板默认值")
    void testPromptTemplateDefaults() {
        assertThat(settings.classPromptTemplate).isEqualTo(SettingsState.getDefaultClassPromptTemplate());
        assertThat(settings.methodPromptTemplate).isEqualTo(SettingsState.getDefaultMethodPromptTemplate());
        assertThat(settings.fieldPromptTemplate).isEqualTo(SettingsState.getDefaultFieldPromptTemplate());
        assertThat(settings.testPromptTemplate).isEqualTo(SettingsState.getDefaultTestPromptTemplate());
    }

    @Test
    @DisplayName("测试持久化状态")
    void testPersistentState() {
        // 修改配置
        settings.aiProvider = AIProviderType.OLLAMA.getProviderId();
        settings.modelName = "llama2";
        settings.generateForField = true;

        // 获取状态
        SettingsState state = settings.getState();

        // 验证状态是同一个对象
        assertThat(state).isSameAs(settings);
        assertThat(state.aiProvider).isEqualTo(AIProviderType.OLLAMA.getProviderId());
        assertThat(state.modelName).isEqualTo("llama2");
        assertThat(state.generateForField).isTrue();
    }

    @Test
    @DisplayName("测试加载状态")
    void testLoadState() {
        // 创建新的状态
        SettingsState newState = new SettingsState();
        newState.aiProvider = AIProviderType.OLLAMA.getProviderId();
        newState.modelName = "llama2";
        newState.baseUrl = "http://localhost:11434";
        newState.generateForField = true;
        newState.maxRetries = 5;
        newState.topP = 0.5;
        newState.topK = 20;
        newState.presencePenalty = 0.5;

        // 加载状态
        settings.loadState(newState);

        // 验证加载的值
        assertThat(settings.aiProvider).isEqualTo(AIProviderType.OLLAMA.getProviderId());
        assertThat(settings.modelName).isEqualTo("llama2");
        assertThat(settings.baseUrl).isEqualTo("http://localhost:11434");
        assertThat(settings.generateForField).isTrue();
        assertThat(settings.maxRetries).isEqualTo(5);
        assertThat(settings.topP).isEqualTo(0.5);
        assertThat(settings.topK).isEqualTo(20);
        assertThat(settings.presencePenalty).isEqualTo(0.5);
    }
}

