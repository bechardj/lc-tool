package us.jbec.lct.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * User Preferences object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPrefs {

    /**
     * Map a dynamic text type to the highest sort order for which the user has acknowledged / dismissed the message type
     */
    private Map<DynamicTextType, Integer> acknowledgedDynamicText;

    boolean enableSynchronizedEditing;

    public Map<DynamicTextType, Integer> getAcknowledgedDynamicText() {
        if (acknowledgedDynamicText == null) {
            acknowledgedDynamicText = new HashMap<>();
        }
        return acknowledgedDynamicText;
    }

    public void setAcknowledgedDynamicText(Map<DynamicTextType, Integer> acknowledgedDynamicText) {
        this.acknowledgedDynamicText = acknowledgedDynamicText;
    }

    public boolean isEnableSynchronizedEditing() {
        return enableSynchronizedEditing;
    }

    public void setEnableSynchronizedEditing(boolean enableSynchronizedEditing) {
        this.enableSynchronizedEditing = enableSynchronizedEditing;
    }
}
