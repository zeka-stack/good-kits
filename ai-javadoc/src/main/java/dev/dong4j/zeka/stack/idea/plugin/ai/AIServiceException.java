package dev.dong4j.zeka.stack.idea.plugin.ai;

/**
 * AI 服务异常
 *
 * <p>当 AI 服务调用过程中发生错误时抛出此异常。
 *
 * @author dong4j
 * @version 1.0.0
 */
public class AIServiceException extends Exception {

    /**
     * 错误代码
     */
    private final ErrorCode errorCode;

    /**
     * 错误代码枚举
     */
    public enum ErrorCode {
        /** API Key 无效或缺失 */
        INVALID_API_KEY,
        /** 网络连接失败 */
        NETWORK_ERROR,
        /** 请求超时 */
        TIMEOUT,
        /** API 限流 */
        RATE_LIMIT,
        /** 服务不可用 */
        SERVICE_UNAVAILABLE,
        /** 无效的响应 */
        INVALID_RESPONSE,
        /** 配置错误 */
        CONFIGURATION_ERROR,
        /** 未知错误 */
        UNKNOWN_ERROR
    }

    /**
     * 构造一个AIServiceException对象，使用指定的错误信息和未知错误码
     *
     * @param message 错误信息
     */
    public AIServiceException(String message) {
        this(message, ErrorCode.UNKNOWN_ERROR);
    }

    /**
     * 构造一个AIServiceException对象
     * <p>
     * 初始化异常信息和错误码
     *
     * @param message   异常的详细信息
     * @param errorCode 错误码
     */
    public AIServiceException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造一个AIServiceException异常对象
     * <p>
     * 使用指定的错误信息、错误码和原因构建异常对象
     *
     * @param message 异常的详细信息说明
     * @param cause   引起当前异常的底层异常
     */
    public AIServiceException(String message, Throwable cause) {
        this(message, ErrorCode.UNKNOWN_ERROR, cause);
    }

    /**
     * 构造一个AIServiceException异常对象
     * <p>
     * 初始化异常信息、错误码以及异常原因
     *
     * @param message   异常的详细信息说明
     * @param errorCode 错误码，用于标识异常类型
     * @param cause     引起当前异常的底层异常
     */
    public AIServiceException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码信息
     * <p>
     * 返回当前系统或操作的错误码
     *
     * @return 错误码对象
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 判断是否应该重试
     *
     * @return 如果错误是临时性的可以重试，返回 true
     */
    public boolean isRetryable() {
        return errorCode == ErrorCode.NETWORK_ERROR
               || errorCode == ErrorCode.TIMEOUT
               || errorCode == ErrorCode.RATE_LIMIT
               || errorCode == ErrorCode.SERVICE_UNAVAILABLE;
    }
}

