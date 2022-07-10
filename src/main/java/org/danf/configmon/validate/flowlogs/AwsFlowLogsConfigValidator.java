package org.danf.configmon.validate.flowlogs;


import org.danf.configmon.aws.client.AwsClientProvider;
import org.danf.configmon.model.ConfigType;
import org.danf.configmon.model.ConfigValidationResult;
import org.danf.configmon.model.FlowLogConfig;
import org.danf.configmon.validate.AwsConfigValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter;

import java.util.Optional;

@Component
public class AwsFlowLogsConfigValidator extends AwsConfigValidator<FlowLogConfig, SubscriptionFilter> {

    public static final String VALIDATION_ERROR_INVALID_TARGET = "Invalid config detected: remote Subscription target arn '%s' does not match stored one '%s'";

    private final AwsClientProvider clientProvider;

    @Autowired
    public AwsFlowLogsConfigValidator(AwsClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    /**
     * Possible optimization: config retrieval can be split to a different thread pool (or even microservice) to allow
     * horizontal scaling of the retrieval action.
     * Also, separation-of-concerns-wise retrieval is probably better split into a separate component, so this is here mainly due to lack of time.
     */
    @Override
    protected Optional<SubscriptionFilter> retrieveConfig(FlowLogConfig locallyStoredConfig) {
        try (var client = clientProvider.makeClient(CloudWatchLogsClient.builder(), locallyStoredConfig.getAwsAccountId(), locallyStoredConfig.getRegion())) {
            return client.describeSubscriptionFilters(makeDescribeLogFiltersRequest(locallyStoredConfig))
                .subscriptionFilters()
                .stream()
                .filter(subscriptionFilter -> subscriptionFilter.filterName().equals(locallyStoredConfig.getSubscriptionFilterName()))
                .findAny();
        }
    }

    @Override
    protected ConfigValidationResult validateConfig(FlowLogConfig locallyStoredConfig, SubscriptionFilter remoteConfig) {
        var validationResult = ConfigValidationResult.builder();
        if (!remoteConfig.destinationArn().equals(locallyStoredConfig.getTargetKinesisStreamArn())) {
            validationResult.validationError(VALIDATION_ERROR_INVALID_TARGET.formatted(remoteConfig.destinationArn(), locallyStoredConfig.getTargetKinesisStreamArn()));
        }
        return validationResult.build();
    }

    @Override
    protected ConfigType type() {
        return ConfigType.FLOW_LOG;
    }

    private DescribeSubscriptionFiltersRequest makeDescribeLogFiltersRequest(FlowLogConfig locallyStoredConfig) {
        return DescribeSubscriptionFiltersRequest
            .builder()
            .logGroupName(locallyStoredConfig.getLogGroupName())
            .build();
    }
}
