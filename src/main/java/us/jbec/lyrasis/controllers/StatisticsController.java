package us.jbec.lyrasis.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lyrasis.models.CaptureDataStatistics;
import us.jbec.lyrasis.models.ImageJob;
import us.jbec.lyrasis.models.ImageJobFile;
import us.jbec.lyrasis.services.CaptureDataStatisticsService;
import us.jbec.lyrasis.services.JobService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
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
            List<ImageJob> imageJobs = jobService.getAllImageJobsSortedFileName().stream()
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
