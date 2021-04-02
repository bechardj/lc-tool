package us.jbec.lct.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.services.RemoteJobService;

import java.net.http.HttpResponse;
import java.util.List;

@RestController
@Profile("remote")
public class RemoteJobController {

    Logger LOG = LoggerFactory.getLogger(RemoteJobController.class);

    private final RemoteJobService remoteJobService;

    public RemoteJobController(RemoteJobService remoteJobService) {
        this.remoteJobService = remoteJobService;
    }

    @PostMapping("/remoteJob")
    public void remoteJob(@RequestParam String apiKey, @RequestBody List<ImageJob> imageJobs) throws JsonProcessingException {
        LOG.info("Remote Job Received - API KEY [{}]", apiKey);
        if (remoteJobService.validApiKey(apiKey)) {
            remoteJobService.saveRemoteJobs(imageJobs, apiKey);
        } else {
            LOG.error("Invalid Api Key: {}", apiKey);
        }
    }
}
