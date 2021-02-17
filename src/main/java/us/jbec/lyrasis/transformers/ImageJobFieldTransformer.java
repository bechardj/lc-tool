package us.jbec.lyrasis.transformers;

import us.jbec.lyrasis.models.ImageJob;
import us.jbec.lyrasis.models.ImageJobFields;

/**
 * To maintain interoperability with previous versions of the application, we see if fields are present here, and if not,
 * add them. Older versions will ignore the added fields.
 */
public class ImageJobFieldTransformer {

    /**
     * Transform image job file by adding fields that may be missing from files created in earlier
     * versions of the application
     * @param imageJob ImageJob to transform
     */
    public static void transform(ImageJob imageJob) {
        transformZeroTwoZeroFields(imageJob);
    }

    /**
     * Transform fields added in release 0.2.0
     * @param imageJob
     */
    private static void transformZeroTwoZeroFields(ImageJob imageJob) {
        if (!imageJob.getFields().containsKey(ImageJobFields.NOTES.name())) {
            imageJob.getFields().put(ImageJobFields.NOTES.name(), "");
        }
    }

}
