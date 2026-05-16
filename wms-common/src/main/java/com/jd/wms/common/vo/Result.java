package com.jd.wms.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String msg;

    private T data;

    private Long timestamp;

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null, System.currentTimeMillis());
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, System.currentTimeMillis());
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data, System.currentTimeMillis());
    }

    public static <T> Result<T> success(String msg) {
        return new Result<>(200, msg, null, System.currentTimeMillis());
    }

    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null, System.currentTimeMillis());
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null, System.currentTimeMillis());
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null, System.currentTimeMillis());
    }

    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未授权访问", null, System.currentTimeMillis());
    }

    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(401, msg, null, System.currentTimeMillis());
    }

    public static <T> Result<T> forbidden() {
        return new Result<>(403, "禁止访问", null, System.currentTimeMillis());
    }

    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(403, msg, null, System.currentTimeMillis());
    }

    public static <T> Result<T> notFound() {
        return new Result<>(404, "资源不存在", null, System.currentTimeMillis());
    }

    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(400, msg, null, System.currentTimeMillis());
    }

    public static <T> Result<T> serviceUnavailable(String msg) {
        return new Result<>(503, msg, null, System.currentTimeMillis());
    }

}