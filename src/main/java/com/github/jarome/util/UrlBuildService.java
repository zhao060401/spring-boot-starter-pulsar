package com.github.jarome.util;

import com.github.jarome.config.PulsarProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class UrlBuildService {

    @Value("${pulsar.consumerNameDelimiter:}")
    private String consumerNameDelimiter;

    private static final String PERSISTENT_PREFIX = "persistent";
    private static final String NON_PERSISTENT_PREFIX = "non-persistent";
    private static final String DEFAULT_PERSISTENCE = PERSISTENT_PREFIX;
    private static final String CONSUMER_NAME_PREFIX = "consumer";
    private static final String SUBSCRIPTION_NAME_PREFIX = "subscription";
    private static final String DEAD_LETTER_NAME_PREFIX = "deadLetter";

    private final PulsarProperties pulsarProperties;

    private UrlBuildService(PulsarProperties pulsarProperties) {
        this.pulsarProperties = pulsarProperties;
    }

    public String buildTopicUrl(String topic) {
        return DEFAULT_PERSISTENCE + "://" + pulsarProperties.getTenant() + "/" + pulsarProperties.getNamespace() +
                "/" + topic;
    }

    public String buildPulsarConsumerName(String customConsumerName, String generatedConsumerName) {
        if (CheckUtils.isEmpty(customConsumerName)) {
            return CONSUMER_NAME_PREFIX + consumerNameDelimiter + generatedConsumerName;
        }

        return customConsumerName;
    }

    public String buildPulsarSubscriptionName(String customSubscriptionName, String consumerName) {
        if (CheckUtils.isEmpty(customSubscriptionName)) {
            return SUBSCRIPTION_NAME_PREFIX + consumerNameDelimiter + consumerName;
        }

        return customSubscriptionName;
    }

    public String buildConsumerName(Class<?> clazz, Method method) {
        return clazz.getName() + consumerNameDelimiter + method.getName() + Arrays
                .stream(method.getGenericParameterTypes())
                .map(Type::getTypeName)
                .collect(Collectors.joining(consumerNameDelimiter));
    }

    public String buildDeadTopicUrl(String topic, String subscriptionName) {
        String topicName = topic.replaceAll("_.*", "").replace(".*", "");
        return DEFAULT_PERSISTENCE + "://" + pulsarProperties.getTenant() + "/" + pulsarProperties.getNamespace() +
                "/" + DEAD_LETTER_NAME_PREFIX + "-" + topicName + "-" + subscriptionName + "-DLQ";
    }
}
