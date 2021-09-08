package us.jbec.lct.models;

/**
 * User Preferences form object
 */
public class UserPrefsForm {

    public UserPrefsForm() {
    }

    /**
     * Construct UserPrefsForm object from existing UserPrefs
     * @param userPrefs UserPrefs to construct from
     */
    public UserPrefsForm(UserPrefs userPrefs) {
        this.enableSynchronizedEditing = userPrefs.isEnableSynchronizedEditing();
    }

    boolean enableSynchronizedEditing;

    public boolean isEnableSynchronizedEditing() {
        return enableSynchronizedEditing;
    }

    public void setEnableSynchronizedEditing(boolean enableSynchronizedEditing) {
        this.enableSynchronizedEditing = enableSynchronizedEditing;
    }
}
