package us.jbec.lct.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import us.jbec.lct.models.ImageJobFile;
import us.jbec.lct.models.ImageJobListing;
import us.jbec.lct.services.IngestService;
import us.jbec.lct.services.JobService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@Profile("!remote")
public class PrimaryController {

    Logger LOG = LoggerFactory.getLogger(PrimaryController.class);

    @Value("${image.bulk.output.path}")
    private String bulkOutputPath;

    private final IngestService ingestService;

    private final JobService jobService;

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
    public String listing(Model model) {
        try {
            ingestService.ingest();
            var imageJobFiles = jobService.getAllImageJobFilesSorted();
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

    @GetMapping("/statistics")
    public String statistics(Model model) {
        return "statistics";
    }

    @GetMapping("/export")
    public String export(Model model) {
        model.addAttribute("path", bulkOutputPath);
        return "export";
    }
}
