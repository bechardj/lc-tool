package us.jbec.lct.models;

/**
 * When the application ran locally, there were two modes for cropping - one for saving
 * crops every time the user saved for that particular file, in the same directory the job data
 * was stored in, and another for saving the output of all jobs in a bulk directory. This is
 * probably no longer needed
 */
@Deprecated
public enum CropsDestination {
    PAGE,
    BULK
}
