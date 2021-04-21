package us.jbec.lct.controllers.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.models.CaptureDataStatistics;
import us.jbec.lct.services.CaptureDataStatisticsService;

@RestController
public class RemoteStatisticsController {

    Logger LOG = LoggerFactory.getLogger(RemoteStatisticsController.class);

    private final CaptureDataStatisticsService captureDataStatisticsService;

    public RemoteStatisticsController(CaptureDataStatisticsService captureDataStatisticsService) {
        this.captureDataStatisticsService = captureDataStatisticsService;
    }

    @Cacheable("remoteStatistics")
    @GetMapping("/statistics")
    public CaptureDataStatistics statistics() throws JsonProcessingException {
        try {
            return captureDataStatisticsService.calculateAllStatistics();
        }
        catch (Exception e) {
            LOG.error("An error occurred generating statistics!", e);
            throw e;
        }
    }
}
