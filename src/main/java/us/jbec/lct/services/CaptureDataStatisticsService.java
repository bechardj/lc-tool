package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import us.jbec.lct.models.CaptureDataStatistics;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.transformers.DocumentCaptureDataTransformer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service for computing capture data statistics
 */
@Service
public class CaptureDataStatisticsService {

    Logger LOG = LoggerFactory.getLogger(CloudCaptureDocumentService.class);

    // TODO: refactor to not need to convert and/or be more efficient so that we don't need to pull back big lobs from db

    private final CloudCaptureDocumentService cloudCaptureDocumentService;

    /**
     * Service for computing capture data statistics
     * @param cloudCaptureDocumentService autowired parameter
     */
    public CaptureDataStatisticsService(CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    /**
     * Calculate statistics for all active cloud capture documents
     * @return computed capture data statistics
     */
    @Cacheable("statistics")
    public CaptureDataStatistics calculateAllStatistics() throws JsonProcessingException {
        LOG.debug("Performing real statistics calculation on all active documents");
        return calculateStatistics(cloudCaptureDocumentService.getActiveCloudCaptureDocumentsMetadata());
    }

    /**
     * Calculate statistics for provided image jobs and cloud capture documents
     * @param cloudCaptureDocuments map containing cloud capture documents and corresponding image job
     * @return computed capture data statistics
     */
    public CaptureDataStatistics calculateStatistics(List<CloudCaptureDocument> cloudCaptureDocuments) throws JsonProcessingException {
        var statistics = new CaptureDataStatistics();

        var completeCount = 0;
        var editedCount = 0;

        for(var doc : cloudCaptureDocuments) {
            var documentCaptureData = cloudCaptureDocumentService.getDocumentCaptureDataByUuidRaw(doc.getUuid());
            var imageJob = DocumentCaptureDataTransformer.apply(documentCaptureData);
            if (imageJob.getCharacterLabels() != null) {
                imageJob.getCharacterLabels().stream()
                        .filter(Objects::nonNull)
                        .forEach(statistics::addLabelFrequency);
                if (doc.getOwner() != null && doc.getOwner().getFirebaseEmail() != null) {
                    statistics.addUserCount(doc.getOwner().getFirebaseEmail(), imageJob.getCharacterLabels().size());
                }
            }
            if (DocumentStatus.COMPLETED == doc.getDocumentStatus() || imageJob.isCompleted()) completeCount++;
            if (imageJob.isEdited()) editedCount++;
        }

        Map<String, Integer> userCounts = new HashMap<>();
        for(var entry : statistics.getUserCounts().entrySet()) {
            if (entry.getValue() > 0) {
                userCounts.put(entry.getKey(), entry.getValue());
            }
        }

        statistics.setUserCounts(userCounts);

        statistics.setPagesWithData(editedCount);
        statistics.setPagesMarkedCompleted(completeCount);
        statistics.setDateGenerated(LocalDateTime.now());
        return statistics;
    }
}
