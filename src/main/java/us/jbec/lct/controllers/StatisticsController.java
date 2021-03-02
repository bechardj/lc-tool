package us.jbec.lct.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.models.CaptureDataStatistics;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFile;
import us.jbec.lct.services.CaptureDataStatisticsService;
import us.jbec.lct.services.JobService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Profile("!remote")
public class StatisticsController {

    Logger LOG = LoggerFactory.getLogger(StatisticsController.class);

    private final CaptureDataStatisticsService captureDataStatisticsService;

    private final JobService jobService;

    public StatisticsController(CaptureDataStatisticsService captureDataStatisticsService, JobService jobService) {
        this.captureDataStatisticsService = captureDataStatisticsService;
        this.jobService = jobService;
    }

    @GetMapping("/calculateStatistics")
    public CaptureDataStatistics statistics(Model model) {
        try {
            List<ImageJob> imageJobs = jobService.getAllImageJobFilesSorted().stream()
                    .map(ImageJobFile::getImageJob)
                    .collect(Collectors.toList());
            return captureDataStatisticsService.calculateStatistics(imageJobs);
        }
        catch (Exception e) {
            LOG.error("An error occurred generating statistics!", e);
            throw e;
        }
    }
}
