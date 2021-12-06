package us.jbec.lct.models;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Model for representing CaptureDataStatistics shown on the home page
 */
public class CaptureDataStatistics {

    private LocalDateTime dateGenerated;

    /**
     * Map of all label count
     */
    private Map<String, Integer> labelFrequency;

    /**
     * Map of non-alphabetic to label count
     */
    private Map<String, Integer> otherFrequency;

    /**
     * Map of uppercase letter to label count
     */
    private Map<String, Integer> upperFrequency;

    /**
     * Map of lowercase letter to label count
     */
    private Map<String, Integer> lowerFrequency;

    /**
     * Map of user email to label count
     */
    private Map<String, Integer> userCounts;

    private Integer pagesWithData;
    private Integer pagesMarkedCompleted;
    private Integer totalCaptured;

    public CaptureDataStatistics() {
        labelFrequency = new HashMap<>();
        otherFrequency = new HashMap<>();
        upperFrequency = new HashMap<>();
        lowerFrequency = new HashMap<>();
        userCounts = new HashMap<>();
        totalCaptured = 0;
    }

    /**
     * Return statistics results with sensitive information masked (i.e. user counts)
     * @return masked results
     */
    public CaptureDataStatistics maskedStatistics() {
        var masked = new CaptureDataStatistics();
        masked.setLabelFrequency(labelFrequency);
        masked.setOtherFrequency(otherFrequency);
        masked.setUpperFrequency(upperFrequency);
        masked.setLowerFrequency(lowerFrequency);
        masked.setTotalCaptured(totalCaptured);
        masked.setPagesWithData(pagesWithData);
        masked.setPagesMarkedCompleted(pagesMarkedCompleted);
        masked.setDateGenerated(dateGenerated);
        return masked;
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

    /**
     * Add Label to statistics, and update the appropriate associated counts (lowercase, uppercase, other)
     * @param label
     */
    public void addLabelFrequency(String label) {
        totalCaptured++;
        addLabelToMap(labelFrequency, label);
        addSpecificTypeFrequency(label);
    }

    private void addSpecificTypeFrequency(String label) {
        if (!StringUtils.isAlpha(label) || StringUtils.length(label) > 1) {
            addLabelToMap(otherFrequency, label);
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

    public Map<String, Integer> getOtherFrequency() {
        return otherFrequency;
    }

    public void setOtherFrequency(Map<String, Integer> otherFrequency) {
        this.otherFrequency = otherFrequency;
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

    public Map<String, Integer> getUserCounts() {
        return userCounts;
    }

    public void setUserCounts(Map<String, Integer> userCounts) {
        this.userCounts = userCounts;
    }

    /**
     * Add to the provided users label capture count, starting at 0 if the user has no count
     * @param user the label of the user to add to the count of
     * @param count how much to add to the count
     */
    public void addUserCount(String user, Integer count) {
        if (!userCounts.containsKey(user)) {
            userCounts.put(user, 0);
        }
        var currentCount = userCounts.get(user);
        userCounts.put(user, currentCount + count);
    }
}
