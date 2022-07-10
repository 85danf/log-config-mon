package org.danf.configmon.rest;

import lombok.extern.slf4j.Slf4j;
import org.danf.configmon.model.FlowLogConfig;
import org.danf.configmon.monitor.ConfigMonitorService;
import org.danf.configmon.persistence.FlowLogConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * A controller enabling creation of configs, which is out of scope for this service but needed for testing.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1")
public class MonitorController {

    private final FlowLogConfigRepository configRepo;
    private final ConfigMonitorService monitorService;

    @Autowired
    public MonitorController(FlowLogConfigRepository configRepo, ConfigMonitorService monitorService) {
        this.configRepo = configRepo;
        this.monitorService = monitorService;
    }

    @GetMapping(value = "/health")
    public String healthCheck() {
        // Can also implement a sanity that aws credentials are configured correctly, database is reachable.
        return "OK!";
    }

    @PostMapping(
            value = "/config/flowlogs",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public String createConfig(@RequestBody FlowLogConfig toCreate) {
        configRepo.saveAndFlush(toCreate);
        return "OK!";
    }

    @PostMapping(value = "/validate")
    public String validateNow() {
        monitorService.runMonitorCycle();
        return "Monitoring cycle triggered";
    }
}
