package us.jbec.lct.models;

public class UserPrefsForm {

    public UserPrefsForm() {
    }

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
