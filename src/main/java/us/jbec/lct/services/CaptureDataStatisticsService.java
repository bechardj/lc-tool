package us.jbec.lct.services;

import org.springframework.stereotype.Service;
import us.jbec.lct.models.CaptureDataStatistics;
import us.jbec.lct.models.ImageJob;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class CaptureDataStatisticsService {
    public CaptureDataStatistics calculateStatistics(List<ImageJob> imageJobList) {
        var statistics = new CaptureDataStatistics();
        imageJobList.stream()
                .map(ImageJob::getCharacterLabels)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(statistics::addLabelFrequency);
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
