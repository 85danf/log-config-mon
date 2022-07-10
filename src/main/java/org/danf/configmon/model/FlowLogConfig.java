package org.danf.configmon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "configs")
public class FlowLogConfig implements Config {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id;
    String awsAccountId;
    String region;
    String targetS3BucketArn;
    String targetKinesisStreamArn;
    String logDestinationIAMRoleArn;
    String logDestinationIAMPolicyArn;
    String logGroupName;
    String subscriptionFilterName;
    Date created;
    Date lastUpdated;
    Date lastChecked;

    @Override
    public ConfigType type() {
        return ConfigType.FLOW_LOG;
    }
}
