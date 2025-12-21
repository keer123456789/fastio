package com.keer.fastio.common.exception;

import com.keer.fastio.common.enums.ExceptionErrorMsg;

/**
 * @author 张经伦
 * @date 2025/12/14 20:08
 * @description:
 */
public class ServiceException extends RuntimeException {
    private final String errorCode;

    public ServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServiceException(ExceptionErrorMsg exceptionErrorMsg) {
        super(exceptionErrorMsg.getMsg());
        this.errorCode = exceptionErrorMsg.getCode();
    }

    public ServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;

    }
    public ServiceException( Throwable cause) {
        super(cause);
        this.errorCode = "unknown";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
