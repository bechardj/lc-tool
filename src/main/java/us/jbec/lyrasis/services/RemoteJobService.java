package us.jbec.lyrasis.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import us.jbec.lyrasis.io.PrimaryImageIO;
import us.jbec.lyrasis.models.CropsDestination;
import us.jbec.lyrasis.models.ImageJob;
import us.jbec.lyrasis.models.ImageJobFile;
import us.jbec.lyrasis.models.ProcessingTimeRecord;
import us.jbec.lyrasis.models.RemotelySubmittedJob;
import us.jbec.lyrasis.repositories.ApiKeyRepository;
import us.jbec.lyrasis.repositories.ProcessingTimeRepository;
import us.jbec.lyrasis.repositories.RemoteJobRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Profile("remote")
public class RemoteJobService {

    Logger LOG = LoggerFactory.getLogger(RemoteJobService.class);

    @Value("${lyrasis.remote.export.enabled}")
    private boolean exportEnabled;

    private final RemoteJobRepository remoteJobRepository;
    private final ProcessingTimeRepository processingTimeRepository;
    private final ObjectMapper objectMapper;
    private final JobService jobService;
    private final ApiKeyRepository apiKeyRepository;
    private final IngestService ingestService;
    private final PrimaryImageIO primaryImageIO;

    public RemoteJobService(RemoteJobRepository remoteJobRepository, ProcessingTimeRepository processingTimeRepository, ObjectMapper objectMapper, JobService jobService, ApiKeyRepository apiKeyRepository, IngestService ingestService, PrimaryImageIO primaryImageIO) {
        this.remoteJobRepository = remoteJobRepository;
        this.processingTimeRepository = processingTimeRepository;
        this.objectMapper = objectMapper;
        this.jobService = jobService;
        this.apiKeyRepository = apiKeyRepository;
        this.ingestService = ingestService;
        this.primaryImageIO = primaryImageIO;
    }

    public boolean validApiKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        return apiKeyRepository.findById(key).isPresent();
    }

    public void saveRemoteJobs(List<ImageJob> imageJobs, String apiKey) throws JsonProcessingException {
        for (ImageJob imageJob : imageJobs) {
            saveRemoteJob(imageJob, apiKey);
        }
    }

    public void saveRemoteJob(ImageJob imageJob, String apiKey) throws JsonProcessingException {
        RemotelySubmittedJob remotelySubmittedJob = new RemotelySubmittedJob();
        remotelySubmittedJob.setJobId(imageJob.getId());
        remotelySubmittedJob.setApiKey(apiKey);
        remotelySubmittedJob.setJson(objectMapper.writeValueAsString(imageJob));
        remoteJobRepository.save(remotelySubmittedJob);
    }

    public List<ImageJob> retrieveCurrentRemoteJobs(boolean onlyNew) {
        List<RemotelySubmittedJob> remoteJobs =  remoteJobRepository.selectNewestJobsBySubmitTime();
        List<ImageJob> imageJobs = new ArrayList<>();
        for(RemotelySubmittedJob remoteJob : remoteJobs) {
            try {
                if (onlyNew && remoteJob.getJobId() != null) {
                    Optional<ProcessingTimeRecord> optionalRecord = processingTimeRepository
                            .findById(remoteJob.getJobId());
                    if (optionalRecord.isPresent()) {
                        ProcessingTimeRecord timeRecord = optionalRecord.get();
                        if (timeRecord.getProcessingTime() != null && remoteJob.getSubmitTime() != null
                            && timeRecord.getProcessingTime().isAfter(remoteJob.getSubmitTime())) {
                            continue;
                        } else {
                            timeRecord.setProcessingTime(LocalDateTime.now());
                            processingTimeRepository.save(timeRecord);
                        }
                    } else {
                        ProcessingTimeRecord timeRecord = new ProcessingTimeRecord();
                        timeRecord.setJobId(remoteJob.getJobId());
                        timeRecord.setProcessingTime(LocalDateTime.now());
                        processingTimeRepository.save(timeRecord);
                    }
                }

                imageJobs.add(objectMapper.readValue(remoteJob.getJson(), ImageJob.class));

            } catch (JsonProcessingException e) {
                LOG.warn("Error reading JSON for remote job id {}, job id {}", remoteJob.getId(), remoteJob.getJobId());
            }
        }
        return imageJobs;
    }

    @Scheduled(fixedDelayString = "${lyrasis.remote.export.frequency}")
    public void exportCurrentRemoteJobs() {
        if (exportEnabled) {
            for (ImageJob imageJob : retrieveCurrentRemoteJobs(true)) {
                try {
                    Optional <ImageJobFile> optionalImageJobFile = primaryImageIO.getImageJobFiles().stream()
                            .filter(imageJobFile -> imageJobFile.getImageJob().getId().equals(imageJob.getId()))
                            .findFirst();
                    if (optionalImageJobFile.isPresent()) {
                        // We need to do this to mock the JSON because the one on disk is not representative
                        // of the content in the database
                        ImageJobFile imageJobFile = new ImageJobFile(optionalImageJobFile.get().getImageFile(), imageJob);
                        jobService.processImageFile(imageJobFile, CropsDestination.BULK);
                    }
                } catch (Exception e) {
                    LOG.error("Could not process image job for job id {}", imageJob.getId());
                }
            }
        } else {
            LOG.info("Skipping export because it is disabled.");
        }
    }

    @Scheduled(fixedDelayString = "${lyrasis.remote.ingest.frequency}")
    public void ingest() {
        ingestService.ingest();
    }

}
