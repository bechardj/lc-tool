package us.jbec.lct.models;

/**
 * Model representing a RuntimeException thrown by the tool during processing
 */
public class LCToolAuthException extends LCToolException {
    public LCToolAuthException(String exception) {
        super(exception);
    }
}
