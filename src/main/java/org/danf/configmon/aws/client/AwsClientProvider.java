package org.danf.configmon.aws.client;

import org.danf.configmon.aws.credentials.CredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.regions.Region;

@Component
public class AwsClientProvider {

    private final CredentialsProvider credentialsProvider;

    @Autowired
    public AwsClientProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public <C extends SdkClient, B extends AwsClientBuilder<B, C>> C makeClient(B clientBuilder, String accountId, String region) {
        return clientBuilder.credentialsProvider(credentialsProvider.getCredentialsForCustomerAccount(accountId)).region(Region.of(region)).build();
    }
}
