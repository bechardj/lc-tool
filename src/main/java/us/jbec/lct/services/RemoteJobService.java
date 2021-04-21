package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.models.CropsDestination;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ProcessingTimeRecord;
import us.jbec.lct.models.database.RemotelySubmittedJob;
import us.jbec.lct.repositories.ApiKeyRepository;
import us.jbec.lct.repositories.ProcessingTimeRepository;
import us.jbec.lct.repositories.RemoteJobRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Profile("remote")
public class RemoteJobService {

    Logger LOG = LoggerFactory.getLogger(RemoteJobService.class);

    @Value("${lct.remote.export.enabled}")
    private boolean exportEnabled;

    private final RemoteJobRepository remoteJobRepository;
    private final ProcessingTimeRepository processingTimeRepository;
    private final ZipOutputService zipOutputService;
    private final ObjectMapper objectMapper;
    private final JobService jobService;
    private final ApiKeyRepository apiKeyRepository;
    private final IngestService ingestService;

    public RemoteJobService(RemoteJobRepository remoteJobRepository, ProcessingTimeRepository processingTimeRepository, ZipOutputService zipOutputService, ObjectMapper objectMapper, JobService jobService, ApiKeyRepository apiKeyRepository, IngestService ingestService) {
        this.remoteJobRepository = remoteJobRepository;
        this.processingTimeRepository = processingTimeRepository;
        this.zipOutputService = zipOutputService;
        this.objectMapper = objectMapper;
        this.jobService = jobService;
        this.apiKeyRepository = apiKeyRepository;
        this.ingestService = ingestService;
    }

    public boolean validApiKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        return apiKeyRepository.findById(key).isPresent();
    }

    @Transactional
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
        List<RemotelySubmittedJob> existingRemoteJobs = remoteJobRepository.selectJobByKeyAndId(apiKey, imageJob.getId());
        existingRemoteJobs.forEach(remoteJobRepository::delete);
        remoteJobRepository.save(remotelySubmittedJob);
    }

    // TODO: implement better handling of processing time records, currently error scan occur but the record is still created
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

    @Scheduled(fixedDelayString = "${lct.remote.export.frequency}")
    public void exportCurrentRemoteJobs() throws ZipException {
        if (exportEnabled) {
            for (ImageJob imageJob : retrieveCurrentRemoteJobs(false)) {
                try {
                    jobService.processImageJobWithFile(imageJob, CropsDestination.BULK);
                } catch (Exception e) {
                    LOG.error("Could not process image job for job id {}: {}", imageJob.getId(), e);
                    if (imageJob.getId() != null) {
                        //processingTimeRepository.deleteById(imageJob.getId());
                    }
                }
            }
            zipOutputService.updateZipOutput();
            zipOutputService.cleanupZipDirectory();
        } else {
            LOG.info("Skipping export because it is disabled.");
        }
    }

    @Scheduled(fixedDelayString = "${lct.remote.prune.frequency}")
    @Transactional
    public void archiveAndPrune() {
//        LOG.info("Archiving old records");
//        remoteJobRepository.archive();
//        LOG.info("Deleting old records");
//        remoteJobRepository.deleteOldRecords();
    }

}
