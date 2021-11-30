package com.github.jarome.test.data;

/**
 * @author zhaojianqiang
 * @date 2021/11/30 13:54
 */
public class ConsumerInfo<T> {
    private Integer id;
    private String esc;
    private T data;

    public ConsumerInfo() {
    }

    public ConsumerInfo(Integer id, String esc, T data) {
        this.id = id;
        this.esc = esc;
        this.data = data;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEsc() {
        return esc;
    }

    public void setEsc(String esc) {
        this.esc = esc;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ConsumerInfo{" +
                "id=" + id +
                ", esc='" + esc + '\'' +
                ", data=" + data +
                '}';
    }
}
