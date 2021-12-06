package us.jbec.lct.controllers.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.models.CaptureDataStatistics;
import us.jbec.lct.services.CaptureDataStatisticsService;
import us.jbec.lct.util.LCToolUtils;

import javax.servlet.http.HttpSession;

/**
 * Controller for serving capture data statistics
 */
@RestController
public class CaptureDataStatisticsController {

    Logger LOG = LoggerFactory.getLogger(CaptureDataStatisticsController.class);

    private final CaptureDataStatisticsService captureDataStatisticsService;

    /**
     * Controller for serving capture data statistics
     * @param captureDataStatisticsService autowired parameter
     */
    public CaptureDataStatisticsController(CaptureDataStatisticsService captureDataStatisticsService) {
        this.captureDataStatisticsService = captureDataStatisticsService;
    }

    /**
     * Return cumulative capture data statistics
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/statistics")
    public CaptureDataStatistics statistics(HttpSession session) throws JsonProcessingException {
        try {
            var user = LCToolUtils.getUserFromSession(session);
            var stats = captureDataStatisticsService.calculateAllStatistics();
            if (user == null) {
                return stats.maskedStatistics();
            }
            return stats;
        }
        catch (Exception e) {
            LOG.error("An error occurred generating statistics!", e);
            throw e;
        }
    }
}
