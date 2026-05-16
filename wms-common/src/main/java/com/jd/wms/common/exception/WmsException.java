package com.jd.wms.common.exception;

public class WmsException extends RuntimeException {

    private Integer code;

    public WmsException(String message) {
        super(message);
        this.code = 500;
    }

    public WmsException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}