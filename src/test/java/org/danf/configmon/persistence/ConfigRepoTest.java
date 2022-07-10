package org.danf.configmon.persistence;


import lombok.SneakyThrows;
import org.danf.configmon.model.FlowLogConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ExtendWith(MockitoExtension.class)
public class ConfigRepoTest {

    @Autowired
    private FlowLogConfigRepository configRepo;

    @Test
    @SneakyThrows
    public void testMonitorCycle() {
        var expected = FlowLogConfig.builder()
            .awsAccountId("customer_aws_account_id")
            .lastUpdated(new Date())
            .lastChecked(new Date())
            .logDestinationIAMPolicyArn("arn:aws:iam::aws:policy/custom/DestinationTrustPolicy")
            .logDestinationIAMRoleArn("arn:aws:iam::123456789123:role/CWOtoKinesisFirehoseRole")
            .logGroupName("my_log_group")
            .region("us-east-1")
            .subscriptionFilterName("firehose_subscription_filter")
            .targetKinesisStreamArn("arn:aws:firehose:us-east-1:123456789123:deliverystream/cw-logs-to-s3")
            .targetS3BucketArn("arn:aws:s3:::danf.cw-logs-to-s3")
            .build();
        configRepo.saveAndFlush(expected);

        var actual = configRepo.getReferenceById(1L);
        assertThat(actual).isEqualTo(expected);
        Thread.sleep(500); // just to make sure date is less than

        var configs = configRepo.findByLastCheckedLessThanEqual(new Date());
        assertThat(configs).hasSize(1);
    }
}
