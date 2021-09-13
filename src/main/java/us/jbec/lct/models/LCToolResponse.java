package us.jbec.lct.models;

/**
 * Generic response object for backend calls
 */
public class LCToolResponse {
    boolean error;
    String info;

    public LCToolResponse(boolean error, String info) {
        this.error = error;
        this.info = info;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
