package us.jbec.lyrasis.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import us.jbec.lyrasis.models.ImageJob;
import us.jbec.lyrasis.models.ImageJobFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("!remote")
public class RemoteSubmissionService {

    Logger LOG = LoggerFactory.getLogger(RemoteSubmissionService.class);

    @Value("${lyrasis.api.email}")
    private String API_KEY;

    @Value("${lyrasis.api.endpoint}")
    private String REMOTE_BASE_URL;

    private String ENDPOINT = "/remoteJob?apiKey=";

    private final JobService jobService;

    public RemoteSubmissionService(JobService jobService) {
        this.jobService = jobService;
    }

    public void submitJobsToRemote(List<ImageJob> imageJob) {
        LOG.info("Submitting to remote server: {}", REMOTE_BASE_URL);
        WebClient.create()
                .post()
                .uri(REMOTE_BASE_URL + ENDPOINT + API_KEY)
                .body(BodyInserters.fromValue(imageJob))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .block();
    }

    @Scheduled(fixedDelayString = "${lyrasis.api.sync.frequency}")
    public void syncAllImageJobs() {
        LOG.info("Syncing with remote");
        submitJobsToRemote(jobService.getAllImageJobFilesSorted().stream()
                .map(ImageJobFile::getImageJob)
                .collect(Collectors.toList()));
    }


}
