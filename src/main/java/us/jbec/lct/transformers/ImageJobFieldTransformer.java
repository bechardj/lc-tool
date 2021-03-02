package us.jbec.lct.transformers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFields;

/**
 * To maintain interoperability with previous versions of the application, we see if fields are present here, and if not,
 * add them. Older versions will ignore the added fields.
 */
public class ImageJobFieldTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ImageJobFieldTransformer.class);

    /**
     * Transform image job file by adding fields that may be missing from files created in earlier
     * versions of the application
     * @param imageJob ImageJob to transform
     */
    public static void transform(ImageJob imageJob) {
        double version;

        try {
            version = Double.parseDouble(imageJob.getVersion());
        } catch (NumberFormatException e) {
            version = 0;
            LOG.error("Could not parse version, will run through all transformers");
        }
        // for now, we always need to run this.
        transformZeroTwoZeroFields(imageJob);
    }

    /**
     * Transform fields added in release 0.2.0
     * @param imageJob image job to transform
     */
    private static void transformZeroTwoZeroFields(ImageJob imageJob) {
        if (!imageJob.getFields().containsKey(ImageJobFields.NOTES.name())) {
            imageJob.getFields().put(ImageJobFields.NOTES.name(), "");
        }
        imageJob.setVersion("0.2");
    }

}
