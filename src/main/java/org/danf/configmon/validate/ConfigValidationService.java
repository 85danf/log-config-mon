package org.danf.configmon.validate;

import lombok.extern.slf4j.Slf4j;
import org.danf.configmon.model.Config;
import org.danf.configmon.model.ConfigType;
import org.danf.configmon.model.ConfigValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkPojo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ConfigValidationService {

    private final Map<ConfigType, AwsConfigValidator<Config, SdkPojo>> configValidators;

    @Autowired
    public ConfigValidationService(List<AwsConfigValidator> configValidators) {
        this.configValidators = configValidators
            .stream()
            .collect(Collectors.toMap(AwsConfigValidator::type, configValidator -> configValidator));
    }

    public List<String> validate(Config config) {
        List<String> validationErrors = List.of();
        try {
            var validationResult = validateConfig(config);
            if (!validationResult.isValid()) {
                validationErrors = validationResult.getValidationErrors();
            }
        } catch (Exception e) {
            log.error("Error encountered while trying to validate config", e);
        }
        return validationErrors;
    }

    private ConfigValidationResult validateConfig(Config config) {
        return Optional.ofNullable(configValidators.get(config.type()))
            .map(awsConfigValidator -> awsConfigValidator.validate(config))
            .orElse(ConfigValidationResult.builder().build());
    }
}
