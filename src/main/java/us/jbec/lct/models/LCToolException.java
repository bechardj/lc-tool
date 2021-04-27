package us.jbec.lct.models;

/**
 * Model representing a RuntimeException thrown by the tool during processing
 */
public class LCToolException extends RuntimeException {
    public LCToolException(String exception) {
        super(exception);
    }
}
