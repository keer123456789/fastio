package com.keer.fastio.enums;

/**
 * @author 张经伦
 * @date 2025/12/14 20:10
 * @description:
 */
public enum ExceptionErrorMsg {
    JsonParse("2000", "json transfer error"),


    BucketExists("3100", "bucket already exists"),


    FileCreatFail("3400", "file/dir creation fail"),
    ;

    private String code;
    private String msg;

    private ExceptionErrorMsg(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
