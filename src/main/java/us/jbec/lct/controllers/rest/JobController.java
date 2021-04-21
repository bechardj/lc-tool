package us.jbec.lct.controllers.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.util.LCToolUtils;

import java.io.IOException;

@RestController
public class JobController {

    Logger LOG = LoggerFactory.getLogger(JobController.class);

    private final CloudCaptureDocumentService cloudCaptureDocumentService;

    public JobController(CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    @GetMapping(value = "/getJob")
    public @ResponseBody ImageJob getJob(@RequestParam String id) throws JsonProcessingException {
        LOG.info("Received request for job: {}", id);
        try {
            return cloudCaptureDocumentService.getImageJobByUuid(id);
        } catch (Exception e) {
            LOG.error("An error occurred while getting image job!", e);
            throw e;
        }
    }

    @PostMapping(value = "/sec/api/saveJob", consumes= { "application/json" })
    public @ResponseBody void saveJob(Authentication authentication,  @RequestBody ImageJob imageJob) throws IOException {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        LOG.info("Received request to save job.");
        try {
            cloudCaptureDocumentService.saveCloudCaptureDocument(user.getFirebaseIdentifier(), imageJob);
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
