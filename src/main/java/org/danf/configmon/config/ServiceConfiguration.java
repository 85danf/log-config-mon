package org.danf.configmon.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;


@Slf4j
@Configuration
public class ServiceConfiguration {

    @Value("${monitor.aws.accessKey.id}")
    private String serviceAccessKeyId;

    @Value("${monitor.aws.accessKey.value}")
    private String serviceAccessKey;

    @Value("${monitor.aws.region}")
    private String awsRegion;

    /**
     * Catches exceptions and maps them to a non-zero return code so that the pod running this app shows as failed on errors.
     */
    @Bean
    public ExitCodeExceptionMapper exitCodeExceptionMapper() {
        return exception -> {
            log.error("Caught unhandled exception:", exception);
            return 1;
        };
    }

    /**
     * Implementation note: if this service was running on a cluster in aws this would not be required.
     * It would have been enough to setup a service account and use the default {@link software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain}
     */
    @Bean
    public AwsCredentials serviceAwsCredentials() {
        return AwsBasicCredentials.create(serviceAccessKeyId, serviceAccessKey);
    }

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient
            .builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(serviceAwsCredentials()))
            .build();
    }
}

