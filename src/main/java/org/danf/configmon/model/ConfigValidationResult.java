package org.danf.configmon.model;


import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class ConfigValidationResult {

    @Singular
    List<String> validationErrors;

    public boolean isValid() {
        return validationErrors == null || validationErrors.size() == 0;
    }

}
