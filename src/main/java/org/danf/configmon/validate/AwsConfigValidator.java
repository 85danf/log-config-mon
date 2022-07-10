package org.danf.configmon.validate;

import org.danf.configmon.model.ConfigType;
import org.danf.configmon.model.ConfigValidationResult;

import java.util.Optional;

public abstract class AwsConfigValidator<LC, RC> {

    public static final String VALIDATION_ERROR_NOT_FOUND = "missing config";

    protected abstract Optional<RC> retrieveConfig(LC locallyStoredConfig);

    protected abstract ConfigValidationResult validateConfig (LC locallyStoredConfig, RC remoteConfig);

    protected abstract ConfigType type();

    public ConfigValidationResult validate(LC locallyStoredConfig) {
        return retrieveConfig(locallyStoredConfig)
            .map(remoteConfig -> validateConfig(locallyStoredConfig, remoteConfig))
            .orElse(ConfigValidationResult.builder().validationError(VALIDATION_ERROR_NOT_FOUND).build());
    }
}
