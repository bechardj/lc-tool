package us.jbec.lct.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.ImageJobListing;
import us.jbec.lct.models.database.User;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.services.IngestService;
import us.jbec.lct.services.JobService;
import us.jbec.lct.util.LCToolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
//@Profile("!remote")
public class PrimaryController {

    @Value("${image.ingest.path}")
    private String ingestPath;

    Logger LOG = LoggerFactory.getLogger(PrimaryController.class);

    @Value("${image.bulk.output.path}")
    private String bulkOutputPath;

    private final IngestService ingestService;

    private final JobService jobService;

    private final CloudCaptureDocumentService cloudCaptureDocumentService;

    public PrimaryController(IngestService ingestService, JobService jobService, CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.ingestService = ingestService;
        this.jobService = jobService;
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    @GetMapping("/secure/open/document")
    public String openDocument(Authentication authentication, Model model, @RequestParam String id) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        var owns = cloudCaptureDocumentService.userOwnsDocument(user.getFirebaseIdentifier(), id);
        LOG.info("Opening document with id: {}", id);
        model.addAttribute("imageId", id);
        model.addAttribute("editable", owns);
        return "capture";
    }

    @GetMapping("/secure/delete/document")
    public RedirectView deleteDocument(Authentication authentication, Model model, @RequestParam String id) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        var owns = cloudCaptureDocumentService.userOwnsDocument(user.getFirebaseIdentifier(), id);
        if (owns) {
            LOG.info("Deleting document with id: {}", id);
            cloudCaptureDocumentService.markDocumentDeleted(id);
        }
        var redirectView = new RedirectView();
        redirectView.setUrl("/secure/listing");
        return redirectView;
    }

    @GetMapping("help")
    public String help(){
        return "help";
    }

    @GetMapping("shortcuts")
    public String shortcuts(){
        return "shortcuts";
    }


    @GetMapping("/secure/listing")
    public String listing(Authentication authentication, Model model) throws JsonProcessingException {
       var user =  LCToolUtils.getUserFromAuthentication(authentication);
        try {
            var cloudCaptureDocuments = cloudCaptureDocumentService
                    .getCloudCaptureDocumentsByUserIdentifier(user.getFirebaseIdentifier());
            List<ImageJobListing> imageJobListings = new ArrayList<>();
            for (var entry : cloudCaptureDocuments.entrySet()) {
                var cloudCaptureDocument = entry.getKey();
                var imageJob = entry.getValue();
                try {
                    if (!DocumentStatus.DELETED.equals(cloudCaptureDocument.getDocumentStatus())) {
                        ImageJobListing imageJobListing = new ImageJobListing(cloudCaptureDocument, imageJob);
                        imageJobListings.add(imageJobListing);
                    }
                } catch (IOException e) {
                    LOG.error("Error creating image job listing for job file {}", cloudCaptureDocument.getUuid());
                }
            }
            model.addAttribute("listingAll", false);
            model.addAttribute("imageJobListings", imageJobListings);
            return "listing";
        }
        catch (Exception e) {
            LOG.error("An error occurred generating listing!", e);
            throw e;
        }
    }

    @GetMapping("/secure/listing/all")
    public String listingAll(Authentication authentication, Model model) throws JsonProcessingException {
        var user =  LCToolUtils.getUserFromAuthentication(authentication);
        try {
            var cloudCaptureDocuments = cloudCaptureDocumentService
                    .getActiveCloudCaptureDocuments();
            List<ImageJobListing> imageJobListings = new ArrayList<>();
            for (var entry : cloudCaptureDocuments.entrySet()) {
                var cloudCaptureDocument = entry.getKey();
                var imageJob = entry.getValue();
                try {
                    if (!DocumentStatus.DELETED.equals(cloudCaptureDocument.getDocumentStatus())) {
                        ImageJobListing imageJobListing = new ImageJobListing(cloudCaptureDocument, imageJob);
                        imageJobListings.add(imageJobListing);
                    }
                } catch (IOException e) {
                    LOG.error("Error creating image job listing for job file {}", cloudCaptureDocument.getUuid());
                }
            }
            model.addAttribute("imageJobListings", imageJobListings);
            model.addAttribute("listingAll", true);
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

    //TODO: refactor into service layer
    @PostMapping("/secure/image/upload")
    public String imageUpload(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        User user = LCToolUtils.getUserFromAuthentication(authentication);
        ingestService.ingest(user, file);
        return "listing";
    }
}
