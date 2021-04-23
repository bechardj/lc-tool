package us.jbec.lct.controllers.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.services.ImageService;

import java.io.IOException;

/**
 * Controller for handling the retrieval of images
 */
@RestController
public class ImageController {

    Logger LOG = LoggerFactory.getLogger(ImageController.class);

    private final ImageService imageService;

    /**
     * Controller for handling the retrieval of images
     * @param imageService autowired parameter
     */
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Retrieve a Base 64 encoded image by id
     * @param uuid image uuid to retrieve
     * @return base 64 encoded image
     * @throws IOException
     */
    @GetMapping(value = "/getImage")
    public @ResponseBody String getJobImage(@RequestParam String uuid) throws IOException {
        LOG.info("Received request for job associated with id: {}", uuid);
        try {
            return imageService.getBase64EncodedImageById(uuid);
        } catch (Exception e) {
            LOG.error("An error occurred while getting image!", e);
            throw e;
        }
    }

}
