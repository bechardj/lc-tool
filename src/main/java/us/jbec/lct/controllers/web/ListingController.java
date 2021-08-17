package us.jbec.lct.controllers.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobListing;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.services.DynamicTextService;
import us.jbec.lct.util.LCToolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling the listing of documents
 */
@Controller
public class ListingController {

    Logger LOG = LoggerFactory.getLogger(ListingController.class);

    private final CloudCaptureDocumentService cloudCaptureDocumentService;
    private final DynamicTextService dynamicTextService;

    /**
     * Controller for handling the listing of documents
     * @param cloudCaptureDocumentService autowired parameter
     * @param dynamicTextService autowired parameter
     */
    public ListingController(CloudCaptureDocumentService cloudCaptureDocumentService,
                             DynamicTextService dynamicTextService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
        this.dynamicTextService = dynamicTextService;
    }

    /**
     * Endpoint for listing all documents
     * @param authentication authentication object
     * @param model view model
     * @return listing view
     * @throws JsonProcessingException
     */
    @GetMapping("/secure/listing/all")
    public String listingAll(Authentication authentication, Model model) throws JsonProcessingException {
        try {
            var maintenance = dynamicTextService.retrieveDynamicText("maintenance");
            maintenance.ifPresent(s -> model.addAttribute("maintenance", s));
            var cloudCaptureDocuments = cloudCaptureDocumentService
                    .getActiveCloudCaptureDocuments();
            var imageJobListings = buildImageJobListings(cloudCaptureDocuments);
            model.addAttribute("imageJobListings", imageJobListings);
            model.addAttribute("listingAll", true);
            return "listing";
        }
        catch (Exception e) {
            LOG.error("An error occurred generating listing!", e);
            throw e;
        }
    }

    /**
     * Endpoint for listing documents owned by a user
     * @param authentication authentication object
     * @param model view model
     * @return listing view
     * @throws JsonProcessingException
     */
    @GetMapping("/secure/listing")
    public String listing(Authentication authentication, Model model) throws JsonProcessingException {
        var user =  LCToolUtils.getUserFromAuthentication(authentication);
        var maintenance = dynamicTextService.retrieveDynamicText("maintenance");
        maintenance.ifPresent(s -> model.addAttribute("maintenance", s));
        try {
            var cloudCaptureDocuments = cloudCaptureDocumentService
                    .getCloudCaptureDocumentsByUserIdentifier(user.getFirebaseIdentifier());
            List<ImageJobListing> imageJobListings = buildImageJobListings(cloudCaptureDocuments);
            model.addAttribute("listingAll", false);
            model.addAttribute("imageJobListings", imageJobListings);
            return "listing";
        }
        catch (Exception e) {
            LOG.error("An error occurred generating listing!", e);
            throw e;
        }
    }

    private List<ImageJobListing> buildImageJobListings(Map<CloudCaptureDocument, ImageJob> cloudCaptureDocuments) {
        List<ImageJobListing> imageJobListings = new ArrayList<>();
        for (var entry : cloudCaptureDocuments.entrySet()) {
            var cloudCaptureDocument = entry.getKey();
            var imageJob = entry.getValue();
            try {
                ImageJobListing imageJobListing = new ImageJobListing(cloudCaptureDocument, imageJob);
                imageJobListings.add(imageJobListing);
            } catch (IOException e) {
                LOG.error("Error creating image job listing for job file {}", cloudCaptureDocument.getUuid());
            }
        }
        return imageJobListings;
    }

}
