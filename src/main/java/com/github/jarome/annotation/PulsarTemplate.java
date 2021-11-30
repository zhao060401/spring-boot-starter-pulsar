package com.github.jarome.annotation;

import com.github.jarome.constant.PulsarSerialization;
import com.github.jarome.error.PulsarException;
import com.github.jarome.util.SchemaUtils;
import com.github.jarome.util.UrlBuildService;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the PulsarTemplate,but is Over-encapsulation,You can also use PulsarClient.
 * the class refer to  RocketMQTemplate
 */
public class PulsarTemplate implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(PulsarTemplate.class);


    private PulsarClient pulsarClient;

    private UrlBuildService urlBuildService;

    private static final Map<String, Producer> producers = new ConcurrentHashMap<>();

    public MessageId send(String topic, PulsarSerialization serialization, Object msg) {
        try {
            return getProducer(topic, serialization, msg).send(msg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new PulsarException(e);
        }
    }

    public CompletableFuture<MessageId> sendAsync(String topic, PulsarSerialization serialization, Object msg) {
        try {
            return getProducer(topic, serialization, msg).sendAsync(msg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new PulsarException(e);
        }
    }


    public Producer getProducer(String topic, PulsarSerialization serialization, Object msg) {
        Producer producer;
        if (producers.containsKey(topic)) {
            producer = producers.get(topic);
        } else {
            try {
                producer = pulsarClient
                        .newProducer(SchemaUtils.getSchema(serialization, msg.getClass()))
                        .topic(urlBuildService.buildTopicUrl(topic)).create();
            } catch (PulsarClientException e) {
                log.error(e.getMessage(), e);
                throw new PulsarException(e);
            }
            producers.put(topic, producer);
        }
        return producer;
    }


    @Override
    public void destroy() throws Exception {
        List<Producer> producersList = new ArrayList<>(producers.values());
        for (Producer producer : producersList) {
            producer.close();
        }
        if (Objects.nonNull(pulsarClient)) {
            if (!pulsarClient.isClosed()) {
                pulsarClient.close();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public PulsarClient getPulsarClient() {
        return pulsarClient;
    }

    public void setPulsarClient(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
    }

    public UrlBuildService getUrlBuildService() {
        return urlBuildService;
    }

    public void setUrlBuildService(UrlBuildService urlBuildService) {
        this.urlBuildService = urlBuildService;
    }
}
