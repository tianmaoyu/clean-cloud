package org.clean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@SuppressWarnings("unused")
public class Result<T> {

    /**
     * 消息
     */
    private String message;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    public boolean isSuccess() {
        return this.code == 0 || this.code == 200;
    }
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(CleanCode.RC200.getCode())
                .message(CleanCode.RC200.getDesc())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(CleanCode.RC200.getCode())
                .message(CleanCode.RC200.getDesc())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 自定义消息的成功返回
     */
    public static <T> Result<T> success(T data, String message) {
        return Result.<T>builder()
                .code(CleanCode.RC200.getCode())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败
     */
    public static <T> Result<T> fail(CleanCode cleanCode) {
        return Result.<T>builder()
                .code(cleanCode.getCode())
                .message(cleanCode.getDesc())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败
     */
    public static <T> Result<T> fail(CleanCode cleanCode, String message) {
        return Result.<T>builder()
                .code(cleanCode.getCode())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }


}