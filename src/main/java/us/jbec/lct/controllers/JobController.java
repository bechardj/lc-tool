package us.jbec.lct.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.models.CropsDestination;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.services.ExclusiveActionService;
import us.jbec.lct.services.JobService;
import us.jbec.lct.services.RemoteSubmissionService;

import java.io.IOException;
import java.util.Collections;

@RestController
@Profile("!remote")
public class JobController {

    Logger LOG = LoggerFactory.getLogger(JobController.class);

    private final JobService jobService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ExclusiveActionService exclusiveActionService;

    public JobController(JobService jobService, ApplicationEventPublisher applicationEventPublisher, ExclusiveActionService exclusiveActionService) {
        this.jobService = jobService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.exclusiveActionService = exclusiveActionService;
    }

    @GetMapping(value = "/getJob")
    public @ResponseBody ImageJob getJob(@RequestParam String id) {
        LOG.info("Received request for job: {}", id);
        try {
            return jobService.getImageJob(id);
        } catch (Exception e) {
            LOG.error("An error occurred while getting image job!", e);
            throw e;
        }
    }

    @PostMapping(value = "/saveJob", consumes= { "application/json" })
    public @ResponseBody void saveJob(@RequestBody ImageJob imageJob) throws IOException {
        LOG.info("Received request to save job.");
        try {
            exclusiveActionService.acquireExclusiveActionLock(JobController.class,  imageJob.getId());
            jobService.processImageJobWithFile(imageJob, CropsDestination.PAGE);
            applicationEventPublisher.publishEvent(imageJob);
        } catch (Exception e) {
            LOG.error("An error occurred while saving image job!", e);
            throw e;
        } finally {
            exclusiveActionService.releaseExclusiveActionLock(JobController.class, imageJob.getId());
        }
    }

    @GetMapping("/exportAll")
    public void exportAll() throws IOException {
        jobService.processAllImageJobCrops(CropsDestination.BULK);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        System.out.println("Returning HTTP 400 Bad Request\n" +  e.toString());
    }
}
