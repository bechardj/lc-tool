package us.jbec.lyrasis.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import us.jbec.lyrasis.models.ImageJobFile;
import us.jbec.lyrasis.models.ImageJobListing;
import us.jbec.lyrasis.services.IngestService;
import us.jbec.lyrasis.services.JobService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PrimaryController {

    Logger LOG = LoggerFactory.getLogger(PrimaryController.class);

    private IngestService ingestService;

    private JobService jobService;

    public PrimaryController(IngestService ingestService, JobService jobService) {
        this.ingestService = ingestService;
        this.jobService = jobService;
    }

    @GetMapping("/open/document")
    public String openDocument(Model model, @RequestParam String id){
        LOG.info("Opening document with id: {}", id);
        model.addAttribute("imageId", id);
        return "capture";
    }

    @GetMapping("help")
    public String help(){
        return "help";
    }

    @GetMapping("/ingest")
    public void ingest() {
        try {
            ingestService.ingest();
        } catch (Exception e) {
            LOG.error("An error occurred while ingesting!", e);
            throw e;
        }
    }

    @GetMapping("/")
    public String listing(Model model){
        try {
            ingestService.ingest();
            List<ImageJobFile> imageJobFiles = jobService.getAllImageJobsSorted();
            List<ImageJobListing> imageJobListings = new ArrayList<>();
            for (ImageJobFile imageJobFile : imageJobFiles) {
                try {
                    ImageJobListing imageJobListing = new ImageJobListing(imageJobFile);
                    imageJobListings.add(imageJobListing);
                } catch (IOException e) {
                    LOG.error("Error creating image job listing for job file {}", imageJobFile.getImageJob().getId());
                }
            }
            model.addAttribute("imageJobListings", imageJobListings);
            return "listing";
        }
        catch (Exception e) {
            LOG.error("An error occurred generating listing!", e);
            throw e;
        }
    }
}
