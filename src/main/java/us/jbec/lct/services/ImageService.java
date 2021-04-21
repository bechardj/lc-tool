package us.jbec.lct.services;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.jbec.lct.io.ImageCropsIO;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.ImageJobFile;
import us.jbec.lct.models.LCToolException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

@Component
public class ImageService {

    Logger LOG = LoggerFactory.getLogger(ImageService.class);

    private final PrimaryImageIO primaryImageIO;

    public ImageService(PrimaryImageIO primaryImageIO) {
        this.primaryImageIO = primaryImageIO;
    }

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
}
