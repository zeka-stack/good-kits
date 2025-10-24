package dev.dong4j.zeka.stack.idea.plugin.ai;

import com.intellij.openapi.diagnostic.Logger;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;
import dev.dong4j.zeka.stack.idea.plugin.task.DocumentationTask;

/**
 * OpenAI 兼容的服务提供商抽象类
 *
 * <p>为兼容 OpenAI API 格式的服务提供商提供通用实现。
 * 大多数现代 AI 服务都提供 OpenAI 兼容的 API 接口，
 * 通过继承此类可以快速实现对新提供商的支持。
 *
 * <p>该抽象类实现了 AIServiceProvider 接口的大部分功能，
 * 包括 HTTP 请求处理、错误处理、重试机制等通用逻辑。
 * 子类只需要关注提供商特定的配置和元数据。
 *
 * <p>核心功能：
 * <ul>
 *   <li>HTTP 请求构建和发送</li>
 *   <li>响应解析和错误处理</li>
 *   <li>重试机制和指数退避</li>
 *   <li>日志记录和调试支持</li>
 *   <li>配置验证</li>
 * </ul>
 *
 * <p>子类只需要实现以下方法：
 * <ul>
 *   <li>{@link #getProviderId()} - 获取提供商标识符</li>
 *   <li>{@link #getProviderName()} - 获取提供商显示名称</li>
 *   <li>{@link #getSupportedModels()} - 获取支持的模型列表</li>
 *   <li>{@link #getDefaultModel()} - 获取默认模型</li>
 *   <li>{@link #getDefaultBaseUrl()} - 获取默认 Base URL</li>
 *   <li>{@link #requiresApiKey()} - 是否需要 API Key</li>
 * </ul>
 *
 * @author dong4j
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AICompatibleProvider implements AIServiceProvider {

    private static final Logger LOG = Logger.getInstance(AICompatibleProvider.class);

    protected final RestTemplate restTemplate;
    protected final SettingsState settings;

    protected AICompatibleProvider(SettingsState settings) {
        this.settings = settings;
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings("D")
    @Override
    @NotNull
    public String generateDocumentation(@NotNull String code,
                                        @NotNull DocumentationTask.TaskType type,
                                        @NotNull String language) throws AIServiceException {

        if (settings.verboseLogging) {
            LOG.debug("=== Generate Documentation ===");
            LOG.debug("Type: " + type);
            LOG.debug("Language: " + language);
            LOG.debug("Code Length: " + code.length() + " characters");
            LOG.debug("Code Preview:\n " + truncateForLog(code, 300));
        }

        String prompt = buildPrompt(code, type, language);

        if (settings.verboseLogging) {
            LOG.debug("Built Prompt Length: " + prompt.length() + " characters");
        }

        int attempts = 0;
        AIServiceException lastException = null;

        while (attempts < settings.maxRetries) {
            try {
                if (settings.verboseLogging) {
                    LOG.debug("Attempt " + (attempts + 1) + "/" + settings.maxRetries + " to generate documentation");
                }

                String result = sendRequest(prompt);

                if (settings.verboseLogging) {
                    LOG.debug("Successfully generated documentation on attempt " + (attempts + 1));
                }

                return result;
            } catch (AIServiceException e) {
                lastException = e;
                attempts++;

                if (!e.isRetryable() || attempts >= settings.maxRetries) {
                    LOG.info("Generation failed after " + attempts + " attempts: " + e.getMessage());
                    break;
                }

                // 指数退避
                long waitTime = (long) (settings.waitDuration * Math.pow(2, attempts - 1));
                LOG.warn("Request failed, retrying in " + waitTime + "ms (attempt " + attempts + "/" +
                         settings.maxRetries + "): " + e.getMessage());

                try {
                    TimeUnit.MILLISECONDS.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AIServiceException("Interrupted during retry",
                                                 AIServiceException.ErrorCode.UNKNOWN_ERROR, ie);
                }
            }
        }

        throw lastException != null ? lastException :
              new AIServiceException("Failed after " + attempts + " attempts");
    }

    /**
     * 发送请求到 AI 服务
     *
     * <p>构建完整的 HTTP 请求并发送到 AI 服务，
     * 处理响应并返回生成的文本内容。
     * 这是与 AI 服务交互的核心方法。
     *
     * <p>请求处理流程：
     * <ol>
     *   <li>构建 HTTP 请求头</li>
     *   <li>构建请求体（JSON 格式）</li>
     *   <li>发送 POST 请求到 /chat/completions 端点</li>
     *   <li>处理响应并解析结果</li>
     * </ol>
     *
     * <p>错误处理策略：
     * <ul>
     *   <li>客户端错误（4xx）：转换为相应的 AIServiceException</li>
     *   <li>服务器错误（5xx）：转换为相应的 AIServiceException</li>
     *   <li>网络错误：转换为 NETWORK_ERROR 类型的 AIServiceException</li>
     *   <li>其他异常：转换为 UNKNOWN_ERROR 类型的 AIServiceException</li>
     * </ul>
     *
     * @param prompt 提示词，包含代码和生成指令
     * @return AI 生成的文本内容
     * @throws AIServiceException 当请求失败时抛出，包含详细的错误信息
     * @see #buildHeaders()
     * @see #buildRequestBody(String)
     * @see #parseResponse(String)
     */
    protected String sendRequest(String prompt) throws AIServiceException {
        try {
            HttpHeaders headers = buildHeaders();
            if (headers == null) {
                throw new AIServiceException("Failed to build request headers: API Key is required but not configured",
                                             AIServiceException.ErrorCode.CONFIGURATION_ERROR);
            }

            JSONObject body = buildRequestBody(prompt);
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            String url = settings.baseUrl + "/chat/completions";

            // 调试日志：记录请求信息
            if (settings.verboseLogging) {
                LOG.trace("=== AI Request ===");
                LOG.trace("URL: " + url);
                LOG.trace("Model: " + settings.modelName);
                LOG.trace("Temperature: " + settings.temperature);
                LOG.trace("Max Tokens: " + settings.maxTokens);
                LOG.trace("Headers: " + maskSensitiveHeaders(headers));
                LOG.trace("Request Body: " + truncateForLog(body.toString(), 1000));
                LOG.trace("Prompt Length: " + prompt.length() + " characters");
                // LOG.trace("Prompt (first 500 chars): \n" + truncateForLog(prompt, 500));
            }

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

            // 调试日志：记录响应信息
            if (settings.verboseLogging) {
                LOG.trace("=== AI Response ===");
                LOG.trace("Status Code: " + response.getStatusCode());
                LOG.trace("Response Body: " + truncateForLog(response.getBody(), 2000));
            }

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String result = parseResponse(response.getBody());

                if (settings.verboseLogging) {
                    LOG.trace("Parsed Result Length: " + result.length() + " characters");
                    LOG.trace("Parsed Result:\n" + truncateForLog(result, 1000));
                }

                return result;
            }

            throw new AIServiceException("Invalid response from AI service",
                                         AIServiceException.ErrorCode.INVALID_RESPONSE);

        } catch (HttpClientErrorException e) {
            LOG.info("HTTP Client Error: Status=" + e.getStatusCode() + ", Body=" + e.getResponseBodyAsString());
            throw handleClientError(e);
        } catch (HttpServerErrorException e) {
            LOG.info("HTTP Server Error: Status=" + e.getStatusCode() + ", Body=" + e.getResponseBodyAsString());
            throw handleServerError(e);
        } catch (ResourceAccessException e) {
            LOG.info("Network Error: " + e.getMessage());
            throw new AIServiceException("Network error: " + e.getMessage(),
                                         AIServiceException.ErrorCode.NETWORK_ERROR, e);
        } catch (Exception e) {
            LOG.info("Unexpected error during AI service call", e);
            throw new AIServiceException("Unexpected error: " + e.getMessage(),
                                         AIServiceException.ErrorCode.UNKNOWN_ERROR, e);
        }
    }

    /**
     * 脱敏敏感请求头（隐藏 API Key）
     *
     * <p>在记录日志时，避免将敏感信息（如 API Key）
     * 直接输出到日志中，防止信息泄露。
     * 将 Authorization 头的值替换为脱敏标记。
     *
     * <p>安全考虑：
     * <ul>
     *   <li>只处理 Authorization 头</li>
     *   <li>保留其他头信息不变</li>
     *   <li>使用固定长度的脱敏标记</li>
     * </ul>
     *
     * @param headers 原始 HTTP 请求头
     * @return 脱敏后的请求头字符串表示
     */
    private String maskSensitiveHeaders(HttpHeaders headers) {
        HttpHeaders maskedHeaders = new HttpHeaders();
        headers.forEach((key, values) -> {
            if ("Authorization".equalsIgnoreCase(key)) {
                maskedHeaders.add(key, "Bearer ***masked***");
            } else {
                maskedHeaders.addAll(key, values);
            }
        });
        return maskedHeaders.toString();
    }

    /**
     * 截断长文本用于日志输出
     *
     * <p>为了避免日志文件过大和提高可读性，
     * 对过长的文本进行截断处理。
     * 主要用于记录请求和响应内容。
     *
     * <p>处理逻辑：
     * <ul>
     *   <li>如果文本长度小于等于最大长度，直接返回</li>
     *   <li>如果文本长度大于最大长度，截断并添加提示信息</li>
     *   <li>处理 null 值，返回 "null" 字符串</li>
     * </ul>
     *
     * @param text      待处理的文本
     * @param maxLength 最大长度限制
     * @return 处理后的文本
     */
    private String truncateForLog(String text, int maxLength) {
        if (text == null) {
            return "null";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "... (truncated, total length: " + text.length() + ")";
    }

    /**
     * 构建 HTTP 请求头
     *
     * <p>根据提供商的要求和配置构建 HTTP 请求头。
     * 包括内容类型、认证信息等必要头信息。
     *
     * <p>头信息包括：
     * <ul>
     *   <li>Content-Type: application/json</li>
     *   <li>Authorization: Bearer {API Key}（如果需要）</li>
     * </ul>
     *
     * <p>安全检查：
     * <ul>
     *   <li>如果需要 API Key 但未配置，记录日志并返回 null</li>
     *   <li>API Key 不能为空或只包含空白字符</li>
     * </ul>
     *
     * @return 构建好的 HTTP 请求头，如果配置不正确返回 null
     */
    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (requiresApiKey()) {
            String apiKey = settings.apiKey;
            if (apiKey == null || apiKey.trim().isEmpty()) {
                LOG.debug("API Key is required but not configured for provider: " + getProviderId());
                return null;
            }
            headers.set("Authorization", "Bearer " + apiKey);
        }

        return headers;
    }

    /**
     * 构建请求体
     *
     * <p>根据 OpenAI API 规范构建 JSON 格式的请求体。
     * 包含模型名称、消息内容、温度参数等配置。
     *
     * <p>请求体结构：
     * <pre>
     * {
     *   "model": "模型名称",
     *   "messages": [
     *     {
     *       "role": "user",
     *       "content": "提示词内容"
     *     }
     *   ],
     *   "temperature": 0.1,
     *   "max_tokens": 1000
     * }
     * </pre>
     *
     * @param prompt 提示词内容
     * @return 构建好的 JSON 请求体
     * @see org.json.JSONObject
     */
    protected JSONObject buildRequestBody(String prompt) {
        JSONObject messages = new JSONObject();
        messages.put("role", "user");
        messages.put("content", prompt);

        JSONObject body = new JSONObject();
        body.put("model", settings.modelName);
        body.put("messages", new Object[] {messages});
        body.put("temperature", settings.temperature);
        body.put("max_tokens", settings.maxTokens);

        return body;
    }

    /**
     * 解析 AI 响应
     *
     * <p>解析 AI 服务返回的 JSON 响应，
     * 提取生成的文本内容。
     * 根据 OpenAI API 响应格式进行解析。
     *
     * <p>响应格式：
     * <pre>
     * {
     *   "choices": [
     *     {
     *       "message": {
     *         "content": "生成的文本内容"
     *       }
     *     }
     *   ]
     * }
     * </pre>
     *
     * <p>异常处理：
     * <ul>
     *   <li>JSON 解析失败：记录错误日志并抛出 INVALID_RESPONSE 异常</li>
     *   <li>字段缺失：记录错误日志并抛出 INVALID_RESPONSE 异常</li>
     * </ul>
     *
     * @param responseBody 原始响应体字符串
     * @return 解析出的文本内容
     * @throws AIServiceException 当解析失败时抛出 INVALID_RESPONSE 类型异常
     */
    protected String parseResponse(String responseBody) throws AIServiceException {
        try {
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
        } catch (Exception e) {
            LOG.info("Failed to parse AI response: " + responseBody, e);
            throw new AIServiceException("Failed to parse response",
                                         AIServiceException.ErrorCode.INVALID_RESPONSE, e);
        }
    }

    /**
     * 构建提示词
     *
     * <p>根据代码内容、文档类型和编程语言构建完整的提示词。
     * 通过加载相应的 Prompt 模板并填充代码内容来生成。
     *
     * <p>构建流程：
     * <ol>
     *   <li>根据文档类型加载相应的 Prompt 模板</li>
     *   <li>使用 String.format 将代码内容插入模板</li>
     *   <li>返回完整的提示词</li>
     * </ol>
     *
     * @param code     代码内容
     * @param type     文档类型
     * @param language 编程语言
     * @return 构建好的提示词
     * @see #loadPromptTemplate(DocumentationTask.TaskType, String)
     */
    protected String buildPrompt(String code, DocumentationTask.TaskType type, String language) {
        String template = loadPromptTemplate(type, language);
        return String.format(template, code);
    }

    /**
     * 加载提示词模板
     *
     * <p>从设置中读取用户配置的 Prompt 模板。
     * 用户可以在设置面板中自定义 Prompt，以满足不同的需求。
     * 如果用户没有配置或配置为空，则使用 SettingsState 中定义的默认模板。
     *
     * @param type     文档类型（类、方法、字段、测试方法等）
     * @param language 编程语言
     * @return Prompt 模板字符串，包含 %s 占位符用于代码插入
     */
    protected String loadPromptTemplate(DocumentationTask.TaskType type, String language) {
        String template;
        // 根据文档类型选择相应的模板
        String defaultTemplate = switch (type) {
            case CLASS, INTERFACE, ENUM -> {
                template = settings.classPromptTemplate;
                yield SettingsState.getDefaultClassPromptTemplate();
            }
            case FIELD -> {
                template = settings.fieldPromptTemplate;
                yield SettingsState.getDefaultFieldPromptTemplate();
            }
            case TEST_METHOD -> {
                template = settings.testPromptTemplate;
                yield SettingsState.getDefaultTestPromptTemplate();
            }
            default -> {
                template = settings.methodPromptTemplate;
                yield SettingsState.getDefaultMethodPromptTemplate();
            }
        };


        // 如果用户没有配置或配置为空，使用默认模板
        if (template == null || template.trim().isEmpty()) {
            return defaultTemplate;
        }

        return template;
    }

    /**
     * 处理客户端错误
     *
     * <p>将 HTTP 客户端错误（4xx）转换为相应的 AIServiceException。
     * 根据具体的状态码返回不同类型的异常。
     *
     * <p>错误映射：
     * <ul>
     *   <li>401/403: INVALID_API_KEY - API Key 无效</li>
     *   <li>429: RATE_LIMIT - 请求频率过高</li>
     *   <li>其他 4xx: CONFIGURATION_ERROR - 配置错误</li>
     * </ul>
     *
     * @param e HttpClientErrorException 异常
     * @return 相应的 AIServiceException
     */
    protected AIServiceException handleClientError(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED ||
            e.getStatusCode() == HttpStatus.FORBIDDEN) {
            return new AIServiceException("Invalid API Key",
                                          AIServiceException.ErrorCode.INVALID_API_KEY, e);
        } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            return new AIServiceException("Rate limit exceeded",
                                          AIServiceException.ErrorCode.RATE_LIMIT, e);
        } else {
            return new AIServiceException("Client error: " + e.getStatusText(),
                                          AIServiceException.ErrorCode.CONFIGURATION_ERROR, e);
        }
    }

    /**
     * 处理服务器错误
     *
     * <p>将 HTTP 服务器错误（5xx）转换为相应的 AIServiceException。
     * 统一返回 SERVICE_UNAVAILABLE 类型的异常。
     *
     * <p>处理策略：
     * <ul>
     *   <li>记录详细的错误日志</li>
     *   <li>包装原始异常信息</li>
     *   <li>返回标准的 AIServiceException</li>
     * </ul>
     *
     * @param e HttpServerErrorException 异常
     * @return SERVICE_UNAVAILABLE 类型的 AIServiceException
     */
    protected AIServiceException handleServerError(HttpServerErrorException e) {
        return new AIServiceException("Server error: " + e.getStatusText(),
                                      AIServiceException.ErrorCode.SERVICE_UNAVAILABLE, e);
    }

    @NotNull
    @Override
    public ValidationResult validateConfiguration() {
        try {
            // 发送一个简单的测试请求
            String testPrompt = "Test connection. Please respond with 'OK'.";

            if (settings.verboseLogging) {
                LOG.debug("Starting configuration validation for provider: " + getProviderId());
                LOG.debug("Base URL: " + settings.baseUrl);
                LOG.debug("Model: " + settings.modelName);
            }

            String response = sendRequest(testPrompt);

            if (response != null && !response.isEmpty()) {
                String successMessage = "连接成功！提供商: " + getProviderName() + ", 模型: " + settings.modelName;
                if (settings.verboseLogging) {
                    LOG.debug("Configuration validation successful");
                }
                return ValidationResult.success(successMessage);
            } else {
                String errorMessage = "连接失败：服务返回空响应";
                LOG.warn(errorMessage);
                return ValidationResult.failure(errorMessage, "请检查网络连接和服务状态");
            }
        } catch (AIServiceException e) {
            String errorMessage = "配置验证失败";
            String errorDetails = getDetailedErrorMessage(e);
            LOG.warn("Configuration validation failed: " + errorDetails, e);
            return ValidationResult.failure(errorMessage, errorDetails, e);
        } catch (Exception e) {
            String errorMessage = "配置验证异常";
            String errorDetails = e.getMessage();
            if (errorDetails == null || errorDetails.isEmpty()) {
                errorDetails = e.getClass().getSimpleName();
            }
            LOG.warn("Configuration validation error: " + errorDetails, e);
            return ValidationResult.failure(errorMessage, errorDetails, e);
        }
    }

    /**
     * 获取详细的错误消息
     *
     * @param e AI 服务异常
     * @return 详细的错误消息
     */
    @NotNull
    private String getDetailedErrorMessage(@NotNull AIServiceException e) {
        AIServiceException.ErrorCode errorCode = e.getErrorCode();
        String details = e.getMessage();

        if (errorCode != null) {
            switch (errorCode) {
                case INVALID_API_KEY:
                    return "API Key 无效或已过期。请检查并更新您的 API Key。";
                case RATE_LIMIT:
                    return "请求频率超限。请稍后再试或升级您的服务套餐。";
                case SERVICE_UNAVAILABLE:
                    return "AI 服务暂时不可用。请稍后重试或检查服务状态。";
                case NETWORK_ERROR:
                    return "网络连接失败。请检查网络连接或 Base URL 是否正确。\n详情: " + details;
                case CONFIGURATION_ERROR:
                    return "配置错误。" + details;
                case INVALID_RESPONSE:
                    return "服务返回的数据格式错误。可能是模型名称不正确或服务异常。";
                default:
                    return details;
            }
        }

        return details != null ? details : "未知错误";
    }
}

