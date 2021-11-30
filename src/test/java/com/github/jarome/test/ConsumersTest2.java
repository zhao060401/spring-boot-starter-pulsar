package com.github.jarome.test;

import com.github.jarome.annotation.PulsarMessageListener;
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
@PulsarMessageListener(topic = "topic-two", maxRedeliverCount = 3)
public class ConsumersTest2 implements PulsarListener<ConsumerData> {
    private static final Logger log = LoggerFactory.getLogger(ConsumersTest2.class);
    @Override
    public void onMessage(ConsumerData message) {
        log.info("consumer2 message:{}",message.toString());
    }
}
