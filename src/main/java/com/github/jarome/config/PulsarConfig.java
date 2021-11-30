package com.github.jarome.config;

import com.github.jarome.annotation.PulsarMessageListener;
import com.github.jarome.common.ThreadFactoryImpl;
import com.github.jarome.error.PulsarException;
import com.github.jarome.service.PulsarListener;
import com.github.jarome.util.SchemaUtils;
import com.github.jarome.util.UrlBuildService;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.shade.com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.SmartMessageConverter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * refer to org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService
 * org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration
 */
@Configuration
@DependsOn({"pulsarClient", "pulsarProperties", "pulsarMessageConverter"})
public class PulsarConfig implements ApplicationContextAware, SmartInitializingSingleton, EmbeddedValueResolverAware {
    private static final Logger log = LoggerFactory.getLogger(PulsarConfig.class);

    private ConfigurableApplicationContext applicationContext;

    private final UrlBuildService urlBuildService;

    private final ConsumerProperties consumerProperties;

    private final StandardEnvironment environment;

    private final PulsarClient pulsarClient;

    private StringValueResolver stringValueResolver;

    private List<Consumer> consumers;

    private final PulsarProperties pulsarProperties;

    private final ThreadPoolExecutor consumeExecutor;

    private final BlockingQueue<Runnable> consumeRequestQueue;

    private final PulsarMessageConverter pulsarMessageConverter;

    private MessageConverter messageConverter;

    public PulsarConfig(UrlBuildService urlBuildService, ConsumerProperties consumerProperties, StandardEnvironment environment,
                        PulsarClient pulsarClient, PulsarProperties pulsarProperties, PulsarMessageConverter pulsarMessageConverter) {
        this.urlBuildService = urlBuildService;
        this.consumerProperties = consumerProperties;
        this.environment = environment;
        this.pulsarClient = pulsarClient;
        this.pulsarProperties = pulsarProperties;
        this.pulsarMessageConverter = pulsarMessageConverter;
        this.consumeRequestQueue = new LinkedBlockingQueue<>();
        this.consumeExecutor = new ThreadPoolExecutor(
                this.pulsarProperties.getConsumeThreadMin(),
                this.pulsarProperties.getConsumeThreadMax(),
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.consumeRequestQueue,
                new ThreadFactoryImpl("PulsarConsume_"));
        setMessageConverter(this.pulsarMessageConverter.getMessageConverter());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(PulsarMessageListener.class)
                .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        consumers = beans.entrySet().stream().map(e -> registerContainer(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    /**
     * Register consumers according to configuration
     */
    private Consumer<?> registerContainer(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);
        if (!PulsarListener.class.isAssignableFrom(bean.getClass())) {
            throw new PulsarException(clazz + " don't implement" + PulsarListener.class.getName() + " quick change it");
        }
        Type parameterType = getParameterType(clazz);
        MethodParameter methodParameter = getMethodParameter(clazz, parameterType);
        PulsarListener<Object> pulsarListener = ((PulsarListener<Object>) bean);
        PulsarMessageListener annotation = clazz.getAnnotation(PulsarMessageListener.class);
        try {
            String topicName = this.environment.resolvePlaceholders(annotation.topic());
            String consumerName = this.environment.resolvePlaceholders(annotation.consumerName());
            String subscriptionName = this.environment.resolvePlaceholders(annotation.subscriptionName());
            SubscriptionType subscriptionType = getSubscriptionType(annotation);
            ConsumerBuilder<?> consumerBuilder = pulsarClient
                    .newConsumer(SchemaUtils.getSchema(annotation.serialization(), methodParameter.getParameterType()))
                    .consumerName(urlBuildService.buildPulsarConsumerName(consumerName, bean.getClass().getSimpleName()))
                    .subscriptionName(urlBuildService.buildPulsarSubscriptionName(subscriptionName, bean.getClass().getSimpleName()))
                    .subscriptionType(subscriptionType)
                    .messageListener((consumer, msg) -> {
                        try {
                            invoke(pulsarListener, convertMessage(msg, parameterType, methodParameter));
                            consumer.acknowledge(msg);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            consumer.negativeAcknowledge(msg);
                        }
                    });
            buildTopicName(topicName, consumerBuilder);
            if (consumerProperties.getAckTimeoutMs() > 0) {
                consumerBuilder.ackTimeout(consumerProperties.getAckTimeoutMs(), TimeUnit.MILLISECONDS);
            }
            buildDeadLetterPolicy(annotation, consumerBuilder, topicName, subscriptionName);
            return consumerBuilder.subscribe();
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
            throw new PulsarException("Initialize consumer " + beanName + " fail");
        }
    }

    private Object convertMessage(Message<?> msg, Type parameterType, MethodParameter methodParameter) {
        Object value = msg.getValue();
        boolean isString = value instanceof String;
        if (Objects.equals(parameterType, String.class) && isString) {
            return value;
        }
        if (!isString) {
            //like Schema.JSON
            return value;
        }
        try {
            if (parameterType instanceof Class) {
                return this.getMessageConverter().fromMessage(MessageBuilder.withPayload(value).build(), (Class<?>) parameterType);
            } else {
                //有泛型的
                return ((SmartMessageConverter) this.getMessageConverter()).fromMessage(MessageBuilder.withPayload(value).build(),
                        (Class<?>) ((ParameterizedType) parameterType).getRawType(), methodParameter);
            }
        } catch (Exception e) {
            log.error("convert failed. str:{}, msgType:{}", value, parameterType);
            throw new PulsarException("cannot convert message to " + parameterType, e);
        }
    }

    private void invoke(PulsarListener<Object> pulsarListener, Object obj) throws Exception {
        Future<Exception> submit = consumeExecutor.submit(() -> {
            try {
                pulsarListener.onMessage(obj);
            } catch (Exception e) {
                return e;
            }
            return null;
        });
        Exception exception = submit.get();
        if (exception != null) {
            throw exception;
        }
    }


    private void buildTopicName(String topicName, ConsumerBuilder<?> consumerBuilder) {
        String buildTopicName = urlBuildService.buildTopicUrl(topicName);
        if (topicName.contains("*")) {
            consumerBuilder.topicsPattern(Pattern.compile(buildTopicName));
        } else {
            consumerBuilder.topic(buildTopicName);
        }
    }

    /**
     * Get the input parameter type of the interface
     *
     * @param clazz class
     * @return Types of formal parameters of the interface
     */
    private Type getParameterType(Class<?> clazz) {
        Type matchedGenericInterface = null;
        while (Objects.nonNull(clazz)) {
            Type[] interfaces = clazz.getGenericInterfaces();
            for (Type type : interfaces) {
                if (type instanceof ParameterizedType && (Objects.equals(((ParameterizedType) type).getRawType(), PulsarListener.class))) {
                    matchedGenericInterface = type;
                    break;
                }
            }
            clazz = clazz.getSuperclass();
        }
        if (Objects.isNull(matchedGenericInterface)) {
            return Object.class;
        }
        Type[] actualTypeArguments = ((ParameterizedType) matchedGenericInterface).getActualTypeArguments();
        if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
            return actualTypeArguments[0];
        }
        return Object.class;
    }

    /**
     * Get interface method parameters
     */
    private MethodParameter getMethodParameter(Class<?> targetClass, Type messageType) {
        Class clazz;
        if (messageType instanceof ParameterizedType && messageConverter instanceof SmartMessageConverter) {
            clazz = (Class) ((ParameterizedType) messageType).getRawType();
        } else if (messageType instanceof Class) {
            clazz = (Class) messageType;
        } else {
            throw new PulsarException("parameterType:" + messageType + " of onMessage method is not supported");
        }
        try {
            final Method method = targetClass.getMethod("onMessage", clazz);
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new PulsarException("parameterType:" + messageType + " of onMessage method is not supported");
        }
    }

    /**
     * Register dead letter queue
     */
    public void buildDeadLetterPolicy(PulsarMessageListener annotation, ConsumerBuilder<?> consumerBuilder, String topicName, String subscriptionName) {
        DeadLetterPolicy.DeadLetterPolicyBuilder deadLetterBuilder = null;

        if (consumerProperties.getDeadLetterPolicyMaxRedeliverCount() >= 0) {
            deadLetterBuilder =
                    DeadLetterPolicy.builder().maxRedeliverCount(consumerProperties.getDeadLetterPolicyMaxRedeliverCount());
        }

        if (annotation.maxRedeliverCount() >= 0) {
            deadLetterBuilder =
                    DeadLetterPolicy.builder().maxRedeliverCount(annotation.maxRedeliverCount());
        }

        if (deadLetterBuilder != null) {
            if (!annotation.deadLetterTopic().isEmpty()) {
                deadLetterBuilder.deadLetterTopic(urlBuildService.buildTopicUrl(annotation.deadLetterTopic()));
            } else if (topicName.contains("*")) {
                //Default dead letter topic name is {TopicName}-{Subscription}-DLQ,like topicsPattern will have problem
                deadLetterBuilder.deadLetterTopic(urlBuildService.buildDeadTopicUrl(topicName, subscriptionName));
            }
        }

        if (deadLetterBuilder != null) {
            consumerBuilder.deadLetterPolicy(deadLetterBuilder.build());
        }
    }


    private SubscriptionType getSubscriptionType(PulsarMessageListener annotation) throws PulsarException {
        SubscriptionType subscriptionType = Arrays.stream(annotation.subscriptionType())
                .findFirst().orElse(null);

        if (subscriptionType == null && Strings.isNullOrEmpty(consumerProperties.getSubscriptionType())) {
            subscriptionType = SubscriptionType.Shared;
        } else if (subscriptionType == null && !Strings.isNullOrEmpty(consumerProperties.getSubscriptionType())) {
            try {
                subscriptionType = SubscriptionType.valueOf(consumerProperties.getSubscriptionType());
            } catch (IllegalArgumentException exception) {
                throw new PulsarException("There was unknown SubscriptionType.", exception);
            }
        }

        return subscriptionType;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        this.stringValueResolver = stringValueResolver;
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public MessageConverter getMessageConverter() {
        return messageConverter;
    }

    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
