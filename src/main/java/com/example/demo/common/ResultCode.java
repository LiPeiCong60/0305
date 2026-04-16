package com.example.demo.common;

public enum ResultCode {

    SUCCESS(200, "\u64cd\u4f5c\u6210\u529f"),
    ERROR(500, "\u7cfb\u7edf\u7e41\u5fd9\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5"),
    TOKEN_INVALID(401, "\u767b\u5f55\u51ed\u8bc1\u7f3a\u5931\u6216\u8fc7\u671f\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55"),
    USER_HAS_EXISTED(4001, "\u8be5\u7528\u6237\u540d\u5df2\u5b58\u5728"),
    USER_NOT_EXIST(4002, "\u8be5\u7528\u6237\u4e0d\u5b58\u5728"),
    PASSWORD_ERROR(4003, "\u8d26\u53f7\u6216\u5bc6\u7801\u9519\u8bef");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
