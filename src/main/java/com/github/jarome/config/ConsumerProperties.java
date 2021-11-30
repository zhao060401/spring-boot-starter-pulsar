package com.github.jarome.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pulsar.consumer.default")
public class ConsumerProperties {
    int deadLetterPolicyMaxRedeliverCount = -1;
    int ackTimeoutMs = 0;
    String subscriptionType = "";

    public int getDeadLetterPolicyMaxRedeliverCount() {
        return deadLetterPolicyMaxRedeliverCount;
    }

    public void setDeadLetterPolicyMaxRedeliverCount(int deadLetterPolicyMaxRedeliverCount) {
        this.deadLetterPolicyMaxRedeliverCount = deadLetterPolicyMaxRedeliverCount;
    }

    public int getAckTimeoutMs() {
        return ackTimeoutMs;
    }

    public void setAckTimeoutMs(int ackTimeoutMs) {
        this.ackTimeoutMs = ackTimeoutMs;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }
}