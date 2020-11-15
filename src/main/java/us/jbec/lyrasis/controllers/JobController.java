package us.jbec.lyrasis.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import us.jbec.lyrasis.models.ImageJob;
import us.jbec.lyrasis.services.ImageService;
import us.jbec.lyrasis.services.JobService;

import java.io.IOException;

@RestController
public class JobController {

    Logger LOG = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
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
        jobService.processImageJob(imageJob);
        } catch (Exception e) {
            LOG.error("An error occurred while saving image job!", e);
            throw e;
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        System.out.println("Returning HTTP 400 Bad Request\n" +  e.toString());
    }
}
