package com.github.jarome.annotation;

import com.github.jarome.constant.PulsarSerialization;
import org.apache.pulsar.client.api.SubscriptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PulsarMessageListener {
    /**
     * topic If you need to topicsPattern,the topic must be like ".*" ,such as "my_test.*"
     */
    String topic();

    /**
     * Type of subscription.
     * <p>
     * Shared - This will allow you to have multiple consumers/instances of the application in a cluster with same subscription
     * name and guarantee that the message is read only by one consumer.
     * <p>
     * Exclusive - message will be delivered to every subscription name only once but won't allow to instantiate multiple
     * instances or consumers of the same subscription name. With a default configuration you don't need to worry about horizontal
     * scaling because message will be delivered to each pod in a cluster since in case of exclusive subscription
     * the name is unique per instance and can be nicely used to update state of each pod in case your service
     * is stateful (For example - you need to update in-memory cached configuration for each instance of authorization microservice).
     * <p>
     * By default the type is `Shared` but you can also override the default in `application.properties`.
     * This can be handy in case you are using `Shared` subscription in your application all the time and you
     * don't want to override this value every time you use `@PulsarConsumer`.
     */
    SubscriptionType[] subscriptionType() default {};

    /**
     * (Optional) Consumer names are auto-generated but in case you wish to use your custom consumer names,
     * feel free to override it.
     */
    String consumerName() default "";

    /**
     * (Optional) Subscription names are auto-generated but in case you wish to use your custom subscription names,
     * feel free to override it.
     */
    String subscriptionName() default "";

    /**
     * Maximum number of times that a message will be redelivered before being sent to the dead letter queue.
     * Note: Currently, dead letter topic is enabled only in the shared subscription mode.
     * <p>
     * tip: if the maxRedeliverCount is 3,the consumers will receive 4 times,the first time is the first received.
     * the other 3 is Retry
     */
    int maxRedeliverCount() default -1;

    /**
     * Name of the dead topic where the failing messages will be sent.
     * tip: the default is {TopicName}-{Subscription}-DLQ,
     * but there is a problem,if your topic is "my_test.*",the consumer will receive (maxRedeliverCount+1)*2.
     * so I change it to deadLetter_my_test_subscriptionName-DLQ
     */
    String deadLetterTopic() default "";

    /**
     * Serialization method
     */
    PulsarSerialization serialization() default PulsarSerialization.STRING;
}
