package org.danf.configmon.validate.flowlogs;

import org.danf.configmon.aws.client.AwsClientProvider;
import org.danf.configmon.model.FlowLogConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AwsFlowLogsConfigValidatorTest {

    @MockBean
    private AwsClientProvider clientProvider;
    @Mock
    private CloudWatchLogsClient client;
    @Mock
    private DescribeSubscriptionFiltersResponse response;

    private final static FlowLogConfig localConfig = FlowLogConfig.builder()
            .subscriptionFilterName("good")
            .targetKinesisStreamArn("good_arn")
            .build();

    private AutoCloseable openMocks;
    private AwsFlowLogsConfigValidator validator;

    @BeforeEach
    public void setup() {
        openMocks = MockitoAnnotations.openMocks(this);
        validator = new AwsFlowLogsConfigValidator(clientProvider);
    }

    @AfterEach
    public void clean() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("goodConfigsSource")
    public void testGoodConfigValidation(List<SubscriptionFilter> filtersList) {
        // Given
        when(response.subscriptionFilters()).thenReturn(filtersList);
        when(client.describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class))).thenReturn(response);
        when(clientProvider.makeClient(any(), any(), any())).thenReturn(client);

        // When
        var validationResult = validator.validate(localConfig);

        // Then
        assertThat(validationResult.isValid()).isTrue();
    }

    private static Stream<List<SubscriptionFilter>> goodConfigsSource() {
        return Stream.of(
            List.of(SubscriptionFilter.builder().filterName("not_what_you're_looking_for").destinationArn("invalid_arn").build(),
                    SubscriptionFilter.builder().filterName("good").destinationArn("good_arn").build()),
            List.of(SubscriptionFilter.builder().filterName("nope1").destinationArn("nope1_arn").build(),
                    SubscriptionFilter.builder().filterName("nope2").destinationArn("nope2_arn").build(),
                    SubscriptionFilter.builder().filterName("good").destinationArn("good_arn").build()),
            List.of(SubscriptionFilter.builder().filterName("good").destinationArn("good_arn").build()));
    }

    @ParameterizedTest
    @MethodSource("badConfigsSource")
    public void testBadConfigValidation(List<SubscriptionFilter> filtersList) {
        // Given
        when(response.subscriptionFilters()).thenReturn(filtersList);
        when(client.describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class))).thenReturn(response);
        when(clientProvider.makeClient(any(), any(), any())).thenReturn(client);

        // When
        var validationResult = validator.validate(localConfig);

        // Then
        assertThat(validationResult.isValid()).isFalse();
    }

    private static Stream<List<SubscriptionFilter>> badConfigsSource() {
        return Stream.of(
            List.of(SubscriptionFilter.builder().filterName("not_what_you're_looking_for").destinationArn("invalid_arn").build(),
                    SubscriptionFilter.builder().filterName("good").destinationArn("invalid_arn").build()),
            List.of(SubscriptionFilter.builder().filterName("nope1").destinationArn("nope1_arn").build(),
                    SubscriptionFilter.builder().filterName("nope2").destinationArn("nope2_arn").build(),
                    SubscriptionFilter.builder().filterName("good").destinationArn("invalid_arn").build()),
            List.of(SubscriptionFilter.builder().filterName("good").destinationArn("invalid_arn").build()));
    }
}
