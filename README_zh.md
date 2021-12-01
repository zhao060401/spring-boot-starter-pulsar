# Spring Boot starter for [Apache Pulsar](https://pulsar.apache.org/)

## 介绍
主要基于Pulsar官方SDK，适配Spring Boot调用方式修改。参考[rocketmq-spring](https://github.com/apache/rocketmq-spring) 和 [pulsar-java-spring-boot-starter](https://github.com/majusko/pulsar-java-spring-boot-starter) 感谢

## 快速开启
### 添加maven依赖
TODO
#### 添加仓库地址到pom.xml里

#### 添加依赖


### 创建消息类
```java
public class ConsumerData {
    private Integer id;
    private String data;
    private Long arriveTime;

    public ConsumerData() {
    }

    public ConsumerData(Integer id, String data, Long arriveTime) {
        this.id = id;
        this.data = data;
        this.arriveTime = arriveTime;
    }
}
```
### 创建生产者
``` java
@Autowired
PulsarTemplate pulsarTemplate;
    
public void sendMsg6() {
    String topic = "topic-six";
    String data = "{\"id\":6,\"data\":\"test6\",\"arriveTime\":1638181398}";
    MessageId send = pulsarTemplate.send(topic, PulsarSerialization.STRING, data);
}
```
### 创建消费者
```java
@Service
@PulsarMessageListener(topic = "topic-six", maxRedeliverCount = 3)
public class ConsumersTest6 implements PulsarListener<ConsumerData> {
    private static final Logger log = LoggerFactory.getLogger(ConsumersTest6.class);
    @Override
    public void onMessage(ConsumerData message) {
        log.info("consumer6 message:{}",message.toString());
    }
}
```
### 必须配置
```properties
pulsar.service-url=pulsar://127.0.0.1:6650
pulsar.namespace=pulsar_test
pulsar.tenant=test
```
## 配置
### 默认配置
```properties
#PulsarClient
pulsar.service-url=pulsar://localhost:6650
pulsar.io-threads=10
pulsar.listener-threads=10
pulsar.enable-tcp-no-delay=false
pulsar.keep-alive-interval-sec=20
pulsar.connection-timeout-sec=10
pulsar.operation-timeout-sec=15
pulsar.starting-backoff-interval-ms=100
pulsar.max-backoff-interval-sec=10
pulsar.consumer-name-delimiter=
pulsar.namespace=default
pulsar.tenant=public

#Consumer
pulsar.consumer.default.dead-letter-policy-max-redeliver-count=-1
pulsar.consumer.default.ack-timeout-ms=3000
pulsar.consume-thread-min=20
pulsar.consume-thread-max=20
```
### TLS链接配置
```properties
pulsar.service-url=pulsar+ssl://localhost:6651
pulsar.tlsTrustCertsFilePath=/etc/pulsar/tls/ca.crt
pulsar.tlsCiphers=TLS_DH_RSA_WITH_AES_256_GCM_SHA384,TLS_DH_RSA_WITH_AES_256_CBC_SHA
pulsar.tlsProtocols=TLSv1.3,TLSv1.2
pulsar.allowTlsInsecureConnection=false
pulsar.enableTlsHostnameVerification=false

pulsar.tlsTrustStorePassword=brokerpw
pulsar.tlsTrustStorePath=/var/private/tls/broker.truststore.jks
pulsar.tlsTrustStoreType=JKS

pulsar.useKeyStoreTls=false
```

### Pulsar client 授权 (不会同时生效)
```properties
# TLS
pulsar.tls-auth-cert-file-path=/etc/pulsar/tls/cert.cert.pem
pulsar.tls-auth-key-file-path=/etc/pulsar/tls/key.key-pk8.pem

#Token based
pulsar.token-auth-value=43th4398gh340gf34gj349gh304ghryj34fh

#OAuth2 based
pulsar.oauth2-issuer-url=https://accounts.google.com
pulsar.oauth2-credentials-url=file:/path/to/file
pulsar.oauth2-audience=https://broker.example.com
```

## 补充
### PulsarMessageListener
- 可以读取配置文件的配置，比如设置为consumerName = "${my.custom.consumer.name}"
- topic可配置为匹配类型，比如"my_test.*",对应默认死信队列定义为{TopicName}-{Subscription}-DLQ，因为设置为匹配，此时消费者也会收到私信队列的消息，将会导致不停的重试。 已将默认死信定义为 deadLetter_{TopicName2}_{Subscription}-DLQ。此TopicName2中移除了" \* "与" .\* "
- 消费者处理默认在线程池中，官方默认一个消费者永远在一个线程中，参考"numListenerThreads"字段中的解释" For a given consumer, the listener is always invoked from the same thread to ensure ordering. If you want multiple threads to process a single topic, you need to create a shared subscription and multiple consumers for this subscription"。 现在默认线程池20,提高消费者处理效率
### PulsarTemplate
- 这个过度封装开发，可直接引用PulsarClient进行处理
- 没有继承AbstractMessageSendingTemplate，暂不知道怎么处理

