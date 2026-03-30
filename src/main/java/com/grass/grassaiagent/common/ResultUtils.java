package com.grass.grassaiagent.common;


import com.grass.grassaiagent.exception.ErrorCode;

/**
 * 响应工具类
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应
     */
    public static <T> R<T> success(T data) {
        return R.ok(data);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static R<?> error(ErrorCode errorCode) {
        return R.fail(errorCode);
    }

    /**
     * 失败
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 响应
     */
    public static R<?> error(int code, String message) {
        return R.fail(code, message);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static R<?> error(ErrorCode errorCode, String message) {
        return R.fail(errorCode.getCode(), message);
    }
}