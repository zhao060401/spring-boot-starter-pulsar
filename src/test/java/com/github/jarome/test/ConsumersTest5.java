package com.github.jarome.test;

import com.github.jarome.annotation.PulsarMessageListener;
import com.github.jarome.service.PulsarListener;
import com.github.jarome.test.data.ConsumerData;
import com.github.jarome.test.data.ConsumerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author zhaojianqiang
 * @date 2021/11/29 18:01
 */
@Service
@PulsarMessageListener(topic = "topic-five", maxRedeliverCount = 3)
public class ConsumersTest5 implements PulsarListener<ConsumerInfo<ConsumerData>> {
    private static final Logger log = LoggerFactory.getLogger(ConsumersTest5.class);

    @Override
    public void onMessage(ConsumerInfo<ConsumerData> message) {
        log.info("consumer4 message:{}", message.toString());
    }
}
