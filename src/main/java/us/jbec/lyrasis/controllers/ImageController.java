package us.jbec.lyrasis.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import us.jbec.lyrasis.models.ImageJob;
import us.jbec.lyrasis.services.ImageService;

import java.io.IOException;

@RestController
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
