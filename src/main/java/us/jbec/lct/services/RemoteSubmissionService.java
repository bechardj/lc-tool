package us.jbec.lct.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("!remote")
public class RemoteSubmissionService {

    Logger LOG = LoggerFactory.getLogger(RemoteSubmissionService.class);

    @Value("${lct.api.email}")
    private String API_KEY;

    @Value("${lct.api.endpoint}")
    private String REMOTE_BASE_URL;

    private String ENDPOINT = "/remoteJob?apiKey=";

    private final JobService jobService;
    private final ObjectMapper objectMapper;

    public RemoteSubmissionService(JobService jobService,
                                   ObjectMapper objectMapper) {
        this.jobService = jobService;
        this.objectMapper = objectMapper;
    }

    public void submitJobsToRemote(List<ImageJob> imageJob) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(REMOTE_BASE_URL + ENDPOINT + API_KEY))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(imageJob)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Scheduled(fixedDelayString = "${lct.api.sync.frequency}")
    public void syncAllImageJobs() {
        try {
            LOG.info("Submitting to remote server: {}", REMOTE_BASE_URL);
            List<ImageJob> imageJobs = jobService.getAllImageJobFilesSorted().stream()
                    .map(ImageJobFile::getImageJob)
                    .collect(Collectors.toList());
            // This is to alleviate large POST requests but provide better performance then one at a time
            // TODO: maybe we just one to do this one by one since it's async anyways?
            // TODO: also, maybe keep track of last successful submit time vs modified time
            List<List<ImageJob>> sublists = ListUtils.partition(imageJobs, 5);
            for (List<ImageJob> sublist : sublists) {
                submitJobsToRemote(sublist);
            }
        } catch (Exception e) {
            LOG.error("Failed to sync jobs with remote: {}", e.getMessage());
        }

    }

    @EventListener
    @Async
    public void imageJobEventListener(ImageJob imageJob) {
        try {
            LOG.info("Submitting to remote server: {}", REMOTE_BASE_URL);
            submitJobsToRemote(Collections.singletonList(imageJob));
        } catch (Exception e) {
            LOG.error("Failed to sync job {} with remote: {}", imageJob.getId(), e.getMessage());
        }
    }


}
