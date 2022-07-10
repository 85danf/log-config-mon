package org.danf.configmon.validate;

import org.danf.configmon.aws.client.AwsClientProvider;
import org.danf.configmon.model.FlowLogConfig;
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
public class ConfigValidationServiceTest {

    private final static List<SubscriptionFilter> filtersList = List.of(
        SubscriptionFilter.builder().filterName("not_what_you're_looking_for").destinationArn("invalid_arn").build(),
        SubscriptionFilter.builder().filterName("good").destinationArn("good_arn").build());

    @MockBean
    private AwsClientProvider clientProvider;
    @Autowired
    private ConfigValidationService configValidationService;

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
    }

    @AfterEach
    public void clean() throws Exception {
        openMocks.close();
    }

    @Test
    public void testValidationService() {

        var validationResults = configValidationService.validate(
            FlowLogConfig.builder()
                .subscriptionFilterName("nope")
                .targetKinesisStreamArn("gonna_fail")
                .build());

        assertThat(validationResults).hasSize(1);


        validationResults = configValidationService.validate(
            FlowLogConfig.builder()
                .subscriptionFilterName("good")
                .targetKinesisStreamArn("good_arn")
                .build());

        assertThat(validationResults).hasSize(0);
    }
}
