package com.keer.fastio.api.entity;

/**
 * @Author: 张经伦
 * @Date: 2025/12/22  14:23
 * @Description:
 */
public class Result<T> {
    private static final int CODE_OK = 1000;
    private static final int CODE_ERROR = 2000;
    private int code;
    private String msg;
    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static Result ok() {
        return new Result(CODE_OK, "", null);
    }

    public static <T> Result ok(T data) {
        return new Result(CODE_OK, "", data);
    }

    public static Result error(int code, String msg) {
        return new Result(code, msg, null);
    }
}
