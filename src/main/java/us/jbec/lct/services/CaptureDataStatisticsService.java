package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import us.jbec.lct.models.CaptureDataStatistics;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.database.CloudCaptureDocument;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CaptureDataStatisticsService {

    private final CloudCaptureDocumentService cloudCaptureDocumentService;

    public CaptureDataStatisticsService(CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    public CaptureDataStatistics calculateAllStatistics() throws JsonProcessingException {
        return calculateStatistics(cloudCaptureDocumentService.getActiveCloudCaptureDocuments());
    }

    public CaptureDataStatistics calculateStatistics(Map<CloudCaptureDocument, ImageJob> cloudCaptureDocuments) {
        var statistics = new CaptureDataStatistics();
        var imageJobList = cloudCaptureDocuments.values().stream().toList();

        HashMap<String, CloudCaptureDocument> docsMap = new HashMap<>();
        cloudCaptureDocuments.keySet().forEach(doc -> docsMap.put(doc.getUuid(), doc));

        for(var entry : cloudCaptureDocuments.entrySet()) {
            var doc = entry.getKey();
            var imageJob = entry.getValue();
            if (imageJob.getCharacterLabels() != null) {
                imageJob.getCharacterLabels().stream()
                        .filter(Objects::nonNull)
                        .forEach(statistics::addLabelFrequency);
                if (doc.getOwner() != null && doc.getOwner().getFirebaseEmail() != null) {
                    statistics.addUserCount(doc.getOwner().getFirebaseEmail(), imageJob.getCharacterLabels().size());
                }
            }
        }

        Map<String, Integer> userCounts = new HashMap<>();
        for(var entry : statistics.getUserCounts().entrySet()) {
            if (entry.getValue() > 0) {
                userCounts.put(entry.getKey(), entry.getValue());
            }
        }

        statistics.setUserCounts(userCounts);

        statistics.setPagesWithData(determineEditedPages(imageJobList));
        statistics.setPagesMarkedCompleted(determineCompletedPages(imageJobList));
        statistics.setDateGenerated(LocalDateTime.now());
        return statistics;
    }
    private int determineEditedPages(List<ImageJob> imageJobs) {
        return (int) imageJobs.stream().filter(ImageJob::isEdited).count();
    }

    private int determineCompletedPages(List<ImageJob> imageJobs) {
        return (int) imageJobs.stream().filter(ImageJob::isCompleted).count();
    }
}
