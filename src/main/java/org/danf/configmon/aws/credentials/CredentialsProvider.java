package org.danf.configmon.aws.credentials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Component
public class CredentialsProvider {

    private static final String SECRET_KEY_FIELD_NAME = "secret_key";
    private static final String SECRET_KEY_ID_FIELD_NAME = "secret_key_id";


    private final SecretsManagerClient secretsManagerClient;

    @Autowired
    public CredentialsProvider(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    public AwsCredentialsProvider getCredentialsForCustomerAccount(String accountId) {
        var secretResponse = secretsManagerClient.getSecretValue(accountCredentialsSecretValueRequest(accountId));
        var secretKey = secretResponse.getValueForField(SECRET_KEY_FIELD_NAME, String.class).orElseThrow();
        var secretKeyId = secretResponse.getValueForField(SECRET_KEY_ID_FIELD_NAME, String.class).orElseThrow();
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(secretKeyId, secretKey));
    }


    private GetSecretValueRequest accountCredentialsSecretValueRequest(String accountId) {
        return GetSecretValueRequest
            .builder()
            .secretId("account_access/" + accountId)
            .build();
    }


}
