package com.github.jarome.test.data;

/**
 * @author zhaojianqiang
 * @date 2021/11/29 18:02
 */
public class ConsumerData {
    private Integer id;
    private String data;
    private Long arriveTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(Long arriveTime) {
        this.arriveTime = arriveTime;
    }

    public ConsumerData() {
    }

    public ConsumerData(Integer id, String data, Long arriveTime) {
        this.id = id;
        this.data = data;
        this.arriveTime = arriveTime;
    }

    @Override
    public String toString() {
        return "ConsumerData{" +
                "id=" + id +
                ", data='" + data + '\'' +
                ", arriveTime=" + arriveTime +
                '}';
    }
}
