package us.jbec.lct.controllers.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.models.LCToolResponse;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.util.LCToolUtils;

import java.io.IOException;

/**
 * Controller for interacting with image jobs
 */
@RestController
public class JobController {

    Logger LOG = LoggerFactory.getLogger(JobController.class);

    private final CloudCaptureDocumentService cloudCaptureDocumentService;

    /**
     * Controller for interacting with image jobs
     * @param cloudCaptureDocumentService autowired parameter
     */
    public JobController(CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    /**
     * Retrieves an image job by uuid
     * @param uuid uuid of image job to retrieve
     * @return retrieved image job
     * @throws JsonProcessingException
     */
    @GetMapping(value = "/getJob")
    public @ResponseBody DocumentCaptureData getJob(@RequestParam String uuid) throws JsonProcessingException,
            CloneNotSupportedException {
        LOG.info("Received request for job: {}", uuid);
        try {
            return DocumentCaptureData.flatten(cloudCaptureDocumentService.getDocumentCaptureDataByUuid(uuid));
        } catch (Exception e) {
            LOG.error("An error occurred while getting image job!", e);
            throw e;
        }
    }

    @PostMapping(value = "/sec/api/saveDoc", consumes= { "application/json" })
    public LCToolResponse saveDoc(Authentication authentication, @RequestBody DocumentCaptureData documentCaptureData) throws IOException {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        return new LCToolResponse(false, "Saved!");
    }
}
