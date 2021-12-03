package com.github.jarome;

import com.github.jarome.annotation.PulsarTemplate;
import com.github.jarome.config.ConsumerProperties;
import com.github.jarome.config.PulsarProperties;
import com.github.jarome.error.PulsarException;
import com.github.jarome.util.CheckUtils;
import com.github.jarome.util.UrlBuildService;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.auth.oauth2.AuthenticationFactoryOAuth2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Configuration
@ComponentScan
@EnableConfigurationProperties({PulsarProperties.class, ConsumerProperties.class})
public class PulsarAutoConfiguration {

    private final PulsarProperties pulsarProperties;

    public PulsarAutoConfiguration(PulsarProperties pulsarProperties) {
        this.pulsarProperties = pulsarProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public PulsarClient pulsarClient() throws PulsarClientException, PulsarException, MalformedURLException {
        if (!CheckUtils.isEmpty(pulsarProperties.getTlsAuthCertFilePath()) &&
                !CheckUtils.isEmpty(pulsarProperties.getTlsAuthKeyFilePath()) &&
                !CheckUtils.isEmpty(pulsarProperties.getTokenAuthValue())
        ) throw new PulsarException("You cannot use multiple auth options.");

        final ClientBuilder pulsarClientBuilder = PulsarClient.builder()
                .serviceUrl(pulsarProperties.getServiceUrl())
                .ioThreads(pulsarProperties.getIoThreads())
                .listenerThreads(pulsarProperties.getListenerThreads())
                .enableTcpNoDelay(pulsarProperties.isEnableTcpNoDelay())
                .keepAliveInterval(pulsarProperties.getKeepAliveIntervalSec(), TimeUnit.SECONDS)
                .connectionTimeout(pulsarProperties.getConnectionTimeoutSec(), TimeUnit.SECONDS)
                .operationTimeout(pulsarProperties.getOperationTimeoutSec(), TimeUnit.SECONDS)
                .startingBackoffInterval(pulsarProperties.getStartingBackoffIntervalMs(), TimeUnit.MILLISECONDS)
                .maxBackoffInterval(pulsarProperties.getMaxBackoffIntervalSec(), TimeUnit.SECONDS)
                .useKeyStoreTls(pulsarProperties.isUseKeyStoreTls())
                .tlsTrustCertsFilePath(pulsarProperties.getTlsTrustCertsFilePath())
                .tlsCiphers(pulsarProperties.getTlsCiphers())
                .tlsProtocols(pulsarProperties.getTlsProtocols())
                .tlsTrustStorePassword(pulsarProperties.getTlsTrustStorePassword())
                .tlsTrustStorePath(pulsarProperties.getTlsTrustStorePath())
                .tlsTrustStoreType(pulsarProperties.getTlsTrustStoreType())
                .allowTlsInsecureConnection(pulsarProperties.isAllowTlsInsecureConnection())
                .enableTlsHostnameVerification(pulsarProperties.isEnableTlsHostnameVerification());

        if (!CheckUtils.isEmpty(pulsarProperties.getTlsAuthCertFilePath()) &&
                !CheckUtils.isEmpty(pulsarProperties.getTlsAuthKeyFilePath())) {
            pulsarClientBuilder.authentication(AuthenticationFactory
                    .TLS(pulsarProperties.getTlsAuthCertFilePath(), pulsarProperties.getTlsAuthKeyFilePath()));
        }

        if (!CheckUtils.isEmpty(pulsarProperties.getTokenAuthValue())) {
            pulsarClientBuilder.authentication(AuthenticationFactory
                    .token(pulsarProperties.getTokenAuthValue()));
        }

        if (!CheckUtils.isEmpty(pulsarProperties.getOauth2Audience()) &&
                !CheckUtils.isEmpty(pulsarProperties.getOauth2IssuerUrl()) &&
                !CheckUtils.isEmpty(pulsarProperties.getOauth2CredentialsUrl())) {
            final URL issuerUrl = new URL(pulsarProperties.getOauth2IssuerUrl());
            final URL credentialsUrl = new URL(pulsarProperties.getOauth2CredentialsUrl());

            pulsarClientBuilder.authentication(AuthenticationFactoryOAuth2
                    .clientCredentials(issuerUrl, credentialsUrl, pulsarProperties.getOauth2Audience()));
        }
        return pulsarClientBuilder.build();
    }

    @Bean(destroyMethod = "destroy")
    @ConditionalOnBean(PulsarClient.class)
    @ConditionalOnMissingBean(name = "pulsarTemplate")
    public PulsarTemplate pulsarTemplate(PulsarClient pulsarClient, UrlBuildService urlBuildService) {
        PulsarTemplate pulsarTemplate = new PulsarTemplate();
        pulsarTemplate.setPulsarClient(pulsarClient);
        pulsarTemplate.setUrlBuildService(urlBuildService);
        return pulsarTemplate;
    }

}
