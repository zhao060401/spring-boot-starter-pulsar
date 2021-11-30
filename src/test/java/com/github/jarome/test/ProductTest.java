package com.github.jarome.test;

import com.github.jarome.annotation.PulsarTemplate;
import com.github.jarome.constant.PulsarSerialization;
import com.github.jarome.test.data.ConsumerData;
import com.github.jarome.test.data.ConsumerInfo;
import org.apache.pulsar.client.api.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author zhaojianqiang
 * @date 2021/11/29 18:18
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductTest {
    @Autowired
    PulsarClient pulsarClient;

    @Test
    public void sendMsg() {
        String topic = "persistent://test/pulsar_test/topic-one";
        try {
            Producer<String> producer = pulsarClient.newProducer(Schema.STRING).topic(topic).create();
            //ConsumerData data = new ConsumerData(1, "测试信息", Timestamp.valueOf(LocalDateTime.now()).getTime());
            String data = "{\"id\":1,\"data\":\"test1\",\"arriveTime\":1638181398}";
            MessageId send = producer.send(data);
            System.out.println("result" + send.toString());
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sendMsg2() {
        String topic = "persistent://test/pulsar_test/topic-two";
        try {
            Producer<byte[]> producer = pulsarClient.newProducer().topic(topic).create();
            //ConsumerData data = new ConsumerData(1, "测试信息", Timestamp.valueOf(LocalDateTime.now()).getTime());
            String data = "{\"id\":2,\"data\":\"test2\",\"arriveTime\":1638181398}";
            MessageId send = producer.send(data.getBytes(StandardCharsets.UTF_8));
            System.out.println("result" + send.toString());
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sendMsg3() {
        String topic = "persistent://test/pulsar_test/topic-three";
        try {
            Producer<ConsumerData> producer = pulsarClient.newProducer(Schema.JSON(ConsumerData.class)).topic(topic).create();
            ConsumerData data = new ConsumerData(3, "test3", Timestamp.valueOf(LocalDateTime.now()).getTime());
            MessageId send = producer.send(data);
            System.out.println("result" + send.toString());
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void sendMsg4() {
        String topic = "persistent://test/pulsar_test/topic-four";
        try {
            Producer<ConsumerData> producer = pulsarClient.newProducer(Schema.AVRO(ConsumerData.class)).topic(topic).create();
            ConsumerData data = new ConsumerData(4, "test4", Timestamp.valueOf(LocalDateTime.now()).getTime());
            MessageId send = producer.send(data);
            System.out.println("result" + send.toString());
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sendMsg5() {
        String topic = "persistent://test/pulsar_test/topic-five";
        try {
            Producer producer = pulsarClient.newProducer(Schema.STRING).topic(topic).create();
            ConsumerData data = new ConsumerData(5, "test5", Timestamp.valueOf(LocalDateTime.now()).getTime());
            ConsumerInfo<ConsumerData> consumerInfo = new ConsumerInfo(1, "泛型", data);
/*            MessageId send = producer.send(JsonUtil.toJson(consumerInfo));
            System.out.println("result" + send.toString());*/
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    PulsarTemplate pulsarTemplate;
    @Test
    public void sendMsg6() {
        String topic = "topic-six";
        String data = "{\"id\":6,\"data\":\"test6\",\"arriveTime\":1638181398}";
        MessageId send = pulsarTemplate.send(topic, PulsarSerialization.STRING, data);
        System.out.println(send);
    }
}
