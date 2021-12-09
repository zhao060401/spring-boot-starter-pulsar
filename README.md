# Spring Boot starter for [Apache Pulsar](https://pulsar.apache.org/)

## Introduce
Mainly based on Pulsar official SDK, adapted to Spring Boot. Refer to[rocketmq-spring](https://github.com/apache/rocketmq-spring) 和 [pulsar-java-spring-boot-starter](https://github.com/majusko/pulsar-java-spring-boot-starter) Thanks

## Quick Start
### Add Maven dependency
TODO
#### Add the JitPack repository to your build file

#### Add the dependency

### Prerequisites
JDK 1.8 and above
Maven 3.0 and above
Spring Boot 2.1.6 and above

### Create a message class
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
### Create producer
``` java
@Autowired
PulsarTemplate pulsarTemplate;
    
public void sendMsg6() {
    String topic = "topic-six";
    String data = "{\"id\":6,\"data\":\"test6\",\"arriveTime\":1638181398}";
    MessageId send = pulsarTemplate.send(topic, PulsarSerialization.STRING, data);
}
```
### Create consumers
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
### Minimal Configuration
```properties
pulsar.service-url=pulsar://127.0.0.1:6650
pulsar.namespace=pulsar_test
pulsar.tenant=test
```
## Configuration
### Default configuration
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
### TLS connection configuration
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

### Pulsar client authentication (Only one of the options can be used)
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

## Tips
### PulsarMessageListener
- Can read the configuration of the configuration file,for example: consumerName = "${my.custom.consumer.name}"
- topic can set to regex,for example "my_test.*",The corresponding default dead letter queue is defined as {TopicName}-{Subscription}-DLQ,Because it is set to regex, At this time, consumers will also receive messages from the private message queue,will cause constant retries. The default dead letter has been defined as: deadLetter_{TopicName2}_{Subscription}-DLQ .This TopicName2 remove all" \* " or " .\* "
- Consumer processing is in the thread pool by default,The official default is that a consumer is always in a thread, refer to numListenerThreads explanation is  "For a given consumer, the listener is always invoked from the same thread to ensure ordering. If you want multiple threads to process a single topic, you need to create a shared subscription and multiple consumers for this subscription", Now the default thread pool is 20, which improves consumer processing efficiency
### PulsarTemplate
- This is excessive packaging,Can directly reference PulsarClient for processing
- Did not extends AbstractMessageSendingTemplate, I do not know how to deal with it temporarily

