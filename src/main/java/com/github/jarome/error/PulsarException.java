package com.github.jarome.error;

/**
 * @author zhaojianqiang
 * @date 2021/11/29 17:35
 */
public class PulsarException extends RuntimeException {
    private Integer code;
    private String msg;

    public PulsarException(Throwable cause, Integer code, String msg) {
        super(cause);
        this.code = code;
        this.msg = msg;
    }

    public PulsarException(String msg) {
        this.code = -1;
        this.msg = msg;
    }

    public PulsarException(Throwable cause) {
        super(cause);
    }

    public PulsarException(String msg, Throwable cause) {
        super(msg, cause);
        this.msg = msg;
    }

    public PulsarException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
