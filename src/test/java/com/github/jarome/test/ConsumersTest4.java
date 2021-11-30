package com.github.jarome.test;

import com.github.jarome.annotation.PulsarMessageListener;
import com.github.jarome.constant.PulsarSerialization;
import com.github.jarome.service.PulsarListener;
import com.github.jarome.test.data.ConsumerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author zhaojianqiang
 * @date 2021/11/29 18:01
 */
@Service
@PulsarMessageListener(topic = "topic-four", maxRedeliverCount = 3, serialization = PulsarSerialization.AVRO)
public class ConsumersTest4 implements PulsarListener<ConsumerData> {
    private static final Logger log = LoggerFactory.getLogger(ConsumersTest4.class);

    @Override
    public void onMessage(ConsumerData message) {
        log.info("consumer4 message:{}", message.toString());
    }
}
