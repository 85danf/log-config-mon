package org.danf.configmon.monitor;

import org.danf.configmon.aws.client.AwsClientProvider;
import org.danf.configmon.model.FlowLogConfig;
import org.danf.configmon.persistence.FlowLogConfigRepository;
import org.danf.configmon.validate.AwsConfigValidator;
import org.danf.configmon.validate.flowlogs.AwsFlowLogsConfigValidator;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
public class ConfigMonitorServiceTest {

    private final static List<SubscriptionFilter> filtersList = List.of(
        SubscriptionFilter.builder().filterName("not_what_you're_looking_for").destinationArn("invalid_arn").build(),
        SubscriptionFilter.builder().filterName("firehose_subscription_filter").destinationArn("arn:aws:firehose:us-east-1:123456789123:deliverystream/cw-logs-to-s3").build());

    private final static FlowLogConfig config = FlowLogConfig.builder()
            .lastChecked(new DateTime().minusYears(1).toDate())
            .logGroupName("my_log_group")
            .subscriptionFilterName("firehose_subscription_filter")
            .targetKinesisStreamArn("arn:aws:firehose:us-east-1:123456789123:deliverystream/cw-logs-to-s3")
            .build();

    private final static FlowLogConfig notFoundConfig = FlowLogConfig.builder()
            .lastChecked(new DateTime().minusYears(1).toDate())
            .logGroupName("cant_touch_this")
            .subscriptionFilterName("cant_find_me")
            .targetKinesisStreamArn("nope")
            .build();

    private final static FlowLogConfig badConfig = FlowLogConfig.builder()
            .lastChecked(new DateTime().minusYears(1).toDate())
            .logGroupName("meh")
            .subscriptionFilterName("firehose_subscription_filter")
            .targetKinesisStreamArn("this_is_gonna_fail")
            .build();

    @MockBean
    private AwsClientProvider clientProvider;
    @Autowired
    private ConfigMonitorService configMonitorService;
    @Autowired
    private FlowLogConfigRepository configRepo;

    @Mock
    private CloudWatchLogsClient client;
    @Mock
    private DescribeSubscriptionFiltersResponse response;

    private AutoCloseable openMocks;

    @BeforeEach
    public void setup() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(response.subscriptionFilters()).thenReturn(filtersList);
        when(client.describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class))).thenReturn(response);
        when(clientProvider.makeClient(any(), any(), any())).thenReturn(client);
        // sanity
        assertThat(configMonitorService.runMonitorCycle()).hasSize(0);
    }

    @AfterEach
    public void clean() throws Exception {
        openMocks.close();
        configRepo.deleteAll();
    }

    @Test
    public void testMonitorServiceGoodConfig() {
        configRepo.save(config);
        var validationResults = configMonitorService.runMonitorCycle();

        assertThat(validationResults).hasSize(0);
    }

    @Test
    public void testMonitorServiceNotFoundConfig() {
        configRepo.save(notFoundConfig);
        var validationResults = configMonitorService.runMonitorCycle();

        assertThat(validationResults).hasSize(1);
        assertThat(validationResults.get(0)).isEqualTo(AwsConfigValidator.VALIDATION_ERROR_NOT_FOUND);
    }

    @Test
    public void testMonitorServiceBadFoundConfig() {
        configRepo.save(badConfig);
        var validationResults = configMonitorService.runMonitorCycle();

        assertThat(validationResults).hasSize(1);
        assertThat(validationResults.get(0)).isEqualTo(AwsFlowLogsConfigValidator.VALIDATION_ERROR_INVALID_TARGET.formatted(config.getTargetKinesisStreamArn(), badConfig.getTargetKinesisStreamArn()));
    }
}
