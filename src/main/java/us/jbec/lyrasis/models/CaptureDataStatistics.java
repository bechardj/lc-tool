package us.jbec.lyrasis.models;

import org.apache.commons.lang.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CaptureDataStatistics {
    private LocalDateTime dateGenerated;
    private Map<String, Integer> labelFrequency;
    private Map<String, Integer> punctuationFrequency;
    private Map<String, Integer> upperFrequency;
    private Map<String, Integer> lowerFrequency;
    private Integer pagesWithData;
    private Integer pagesMarkedCompleted;
    private Integer totalCaptured;

    public CaptureDataStatistics() {
        labelFrequency = new HashMap<>();
        punctuationFrequency = new HashMap<>();
        upperFrequency = new HashMap<>();
        lowerFrequency = new HashMap<>();
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
        addLabelToMap(labelFrequency, label);
        addSpecificTypeFrequency(label);
    }

    private void addSpecificTypeFrequency(String label) {
        if (!StringUtils.isAlpha(label) || StringUtils.length(label) > 1) {
            addLabelToMap(punctuationFrequency, label);
        } else if (StringUtils.isAlpha(label) && StringUtils.length(label) == 1) {
            if (StringUtils.isAllLowerCase(label)) {
                addLabelToMap(lowerFrequency, label);
            } else if (StringUtils.isAllUpperCase(label)) {
                addLabelToMap(upperFrequency, label);
            }
        }
    }

    private void addLabelToMap(Map<String, Integer> map, String label) {
        if (map.containsKey(label)) {
            int currentCount = map.get(label);
            map.put(label, currentCount + 1);
        } else {
            map.put(label, 1);
        }

    }

    public int getLabelFrequency(String label) {
        return labelFrequency.getOrDefault(label, 0);
    }

    public Map<String, Integer> getPunctuationFrequency() {
        return punctuationFrequency;
    }

    public void setPunctuationFrequency(Map<String, Integer> punctuationFrequency) {
        this.punctuationFrequency = punctuationFrequency;
    }

    public Map<String, Integer> getUpperFrequency() {
        return upperFrequency;
    }

    public void setUpperFrequency(Map<String, Integer> upperFrequency) {
        this.upperFrequency = upperFrequency;
    }

    public Map<String, Integer> getLowerFrequency() {
        return lowerFrequency;
    }

    public void setLowerFrequency(Map<String, Integer> lowerFrequency) {
        this.lowerFrequency = lowerFrequency;
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
