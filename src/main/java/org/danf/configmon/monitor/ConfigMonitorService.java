package org.danf.configmon.monitor;

import lombok.extern.slf4j.Slf4j;
import org.danf.configmon.model.Config;
import org.danf.configmon.model.FlowLogConfig;
import org.danf.configmon.persistence.FlowLogConfigRepository;
import org.danf.configmon.validate.ConfigValidationService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConfigMonitorService {

    @Value("${monitor.schedule.interval.minutes}")
    private int interval;

    private final ConfigValidationService configValidationService;
    private final FlowLogConfigRepository configRepo;


    @Autowired
    public ConfigMonitorService(ConfigValidationService configValidationService, FlowLogConfigRepository configRepo) {
        this.configValidationService = configValidationService;
        this.configRepo = configRepo;
    }

    /**
     * Possible optimization: to avoid a 'jitter' situation where the configuration consolidator has not yet created configs for
     * a new flow log config but the monitor has already woken up, decides config is missing and calls the consolidator -
     * it would probably be best to set some quiet period for new configurations (can be maintained with {@link FlowLogConfig#getCreated}
     */
    @Scheduled(
        cron = "${monitor.schedule.cron}"
    )
    public void monitorCron() {
        runMonitorCycle();
    }

    public List<String> runMonitorCycle() {
        return configRepo
            .findByLastCheckedLessThanEqual(new DateTime().minusMinutes(interval).toDate())
            .stream()
            .map(this::validateAndUpdate)
            .flatMap(Collection::stream)
            .peek(log::error)
            .collect(Collectors.toList());
    }

    /**
     * Each validated config needs to also {@link FlowLogConfig#setLastChecked}
     */
    private List<String> validateAndUpdate(Config config) {
        var validationResults = configValidationService.validate(config);
        if (configRepo.setLastChecked(config.getId(), new Date()) != 1) {
            log.error("Error setting last checked time for config with id '{}'", config.getId());
        }
        return validationResults;
    }
}
