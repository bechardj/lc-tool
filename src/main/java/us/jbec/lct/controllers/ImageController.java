package us.jbec.lct.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.services.ImageService;

import java.io.IOException;

@RestController
@Profile("!remote")
public class ImageController {

    Logger LOG = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }


    @GetMapping(value = "/getImage")
    public @ResponseBody String getJobImage(@RequestParam String id) throws IOException {
        LOG.info("Received request for job associated with id: {}", id);
        try {
            return imageService.getImageById(id);
        } catch (Exception e) {
            LOG.error("An error occurred while getting image!", e);
            throw e;
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        System.out.println("Returning HTTP 400 Bad Request\n" +  e.toString());
    }
}
