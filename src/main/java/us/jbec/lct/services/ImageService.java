package us.jbec.lct.services;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.LCToolException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Service for retrieving Base64 encoded images by uuid
 */
@Component
public class ImageService {

    Logger LOG = LoggerFactory.getLogger(ImageService.class);

    private final PrimaryImageIO primaryImageIO;

    /**
     * Service for retrieving Base64 encoded images by uuid
     * @param primaryImageIO autowired parameter
     */
    public ImageService(PrimaryImageIO primaryImageIO) {
        this.primaryImageIO = primaryImageIO;
    }

    /**
     * Retrieve a base 64 encoded image corresponding to the provided document UUID
     * @param uuid UUID of document to retrieve the corresponding image of
     * @return Base64 encoded image
     * @throws IOException
     */
    @Deprecated
    public String getBase64EncodedImageById(String uuid) throws IOException {
        var optionalImage = primaryImageIO.getImageByUuid(uuid);
        if (optionalImage.isPresent()) {
            var image = optionalImage.get();
            var in = new FileInputStream(image);
            return Base64.getEncoder().encodeToString(IOUtils.toByteArray(in));
        } else {
            LOG.error("Image not found!");
            throw new LCToolException("Image not found!");
        }
    }

    /**
     * Retrieve an image corresponding to the provided document UUID
     * @param uuid UUID of document to retrieve the corresponding image of
     * @return image
     */

    public byte[] getImageById(String uuid) throws IOException {
        var optionalImage = primaryImageIO.getImageByUuid(uuid);
        if (optionalImage.isPresent()) {
            try (var in = new FileInputStream(optionalImage.get())) {
                return IOUtils.toByteArray(in);
            }
        } else {
            LOG.error("Image not found!");
            throw new LCToolException("Image not found!");
        }
    }
}
