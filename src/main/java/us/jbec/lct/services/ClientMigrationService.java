package us.jbec.lct.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFile;
import us.jbec.lct.models.MigrationRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientMigrationService {

    Logger LOG = LoggerFactory.getLogger(ClientMigrationService.class);

    @Value("${lct.api.endpoint}")
    private String REMOTE_BASE_URL;

    private String ENDPOINT = "/migrate?first=";

    private final JobService jobService;
    private final ImageService imageService;

    public ClientMigrationService(JobService jobService, ImageService imageService) {
        this.jobService = jobService;
        this.imageService = imageService;
    }

    public void doMigration(String token) throws IOException {

        LOG.info("Building migration requests... Only edited jobs will be submitted!");

        List<MigrationRequest> migrationRequests = new ArrayList<>();
        List<ImageJobFile> jobsToMigrate = jobService.getAllImageJobFilesSorted().stream()
                .filter(imageJobFile -> imageJobFile.getImageJob().isEdited())
                .collect(Collectors.toList());
        for (ImageJobFile imageJobFile : jobsToMigrate) {

            ImageJob imageJob = imageJobFile.getImageJob();
            File imageFile = imageJobFile.getImageFile();

            MigrationRequest migrationRequest = new MigrationRequest();
            migrationRequest.setImageJob(imageJobFile.getImageJob());
            migrationRequest.setOriginalFileName(imageFile.getName());
            migrationRequest.setEncodedImage(imageService.getImageById(imageJob.getId()));
            migrationRequest.setFirebaseUuid(token);
            migrationRequests.add(migrationRequest);
        }

        LOG.info("Beginning to migrate {} jobs to {}", migrationRequests.size(), REMOTE_BASE_URL);

        for (int i = 0; i < migrationRequests.size(); i++) {
            MigrationRequest migrationRequest = migrationRequests.get(i);
            LOG.info("Migrating job {} of {} with ID: {} to remote...", i+1, migrationRequests.size(), migrationRequest.getImageJob().getId());
            WebClient.create()
                    .post()
                    .uri(REMOTE_BASE_URL + ENDPOINT + (i == 0 ? "true" : "false"))
                    .body(BodyInserters.fromValue(migrationRequest))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .exchange()
                    .block();
        }

        LOG.info("Complete.");

    }

}
