package us.jbec.lyrasis.models;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CaptureDataStatistics {
    private LocalDateTime dateGenerated;
    private Map<String, Integer> labelFrequency;
    private Integer pagesWithData;
    private Integer pagesMarkedCompleted;
    private Integer totalCaptured;

    public CaptureDataStatistics() {
        labelFrequency = new HashMap<>();
        totalCaptured = 0;
    }

    public LocalDateTime getDateGenerated() {
        return dateGenerated;
    }

    public void setDateGenerated(LocalDateTime dateGenerated) {
        this.dateGenerated = dateGenerated;
    }

    public Map<String, Integer> getLabelFrequency() {
        return new HashMap<>(labelFrequency);
    }

    public void setLabelFrequency(Map<String, Integer> labelFrequency) {
        this.labelFrequency = labelFrequency;
    }

    public void addLabelFrequency(String label) {
        totalCaptured++;
        if (labelFrequency.containsKey(label)) {
            int currentCount = labelFrequency.get(label);
            labelFrequency.put(label, currentCount + 1);
        } else {
            labelFrequency.put(label, 1);
        }
    }

    public int getLabelFrequency(String label) {
        return labelFrequency.getOrDefault(label, 0);
    }

    public Integer getPagesWithData() {
        return pagesWithData;
    }

    public void setPagesWithData(Integer pagesWithData) {
        this.pagesWithData = pagesWithData;
    }

    public Integer getPagesMarkedCompleted() {
        return pagesMarkedCompleted;
    }

    public void setPagesMarkedCompleted(Integer pagesMarkedCompleted) {
        this.pagesMarkedCompleted = pagesMarkedCompleted;
    }

    public Integer getTotalCaptured() {
        return totalCaptured;
    }

    public void setTotalCaptured(Integer totalCaptured) {
        this.totalCaptured = totalCaptured;
    }
}
