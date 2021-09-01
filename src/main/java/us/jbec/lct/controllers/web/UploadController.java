package us.jbec.lct.controllers.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import us.jbec.lct.models.database.User;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.services.IngestService;
import us.jbec.lct.util.LCToolUtils;

import java.io.IOException;

/**
 * Controller for processing uploads of images and JSON jobs
 */
@Controller
public class UploadController {

    Logger LOG = LoggerFactory.getLogger(UploadController.class);

    private final IngestService ingestService;

    private final CloudCaptureDocumentService cloudCaptureDocumentService;

    /**
     * Controller for processing uploads of images and JSON jobs
     * @param ingestService autowired parameter
     * @param cloudCaptureDocumentService autowired parameter
     */
    public UploadController(IngestService ingestService, CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.ingestService = ingestService;
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    /**
     * Endpoint for processing user image uploads and ingesting them
     * @param authentication authentication object
     * @param file image file to ingest
     * @return success view
     * @throws IOException
     */
    @PostMapping("/secure/image/upload")
    public String imageUpload(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        User user = LCToolUtils.getUserFromAuthentication(authentication);
        ingestService.ingest(user, file);
        return "success";
    }

    /**
     * Endpoint for processing user image job uploads to replace the image job contained in the database
     * @param model provided model
     * @param authentication authentication object
     * @param file file containing image job
     * @param uuid uuid of image job to assign the uploaded image job
     * @return capture view
     * @throws IOException
     */
    @PostMapping("/secure/job/upload")
    public String jobUpload(Model model, Authentication authentication, @RequestParam("file") MultipartFile file, @RequestParam String uuid) throws IOException {
        User user = LCToolUtils.getUserFromAuthentication(authentication);
        cloudCaptureDocumentService.saveUploadedCaptureData(user.getFirebaseIdentifier(), file, uuid);
        var owns = cloudCaptureDocumentService.userOwnsDocument(user.getFirebaseIdentifier(), uuid);
        LOG.info("Opening document with id: {}", uuid);
        model.addAttribute("imageId", uuid);
        model.addAttribute("editable", owns);
        return "capture";
    }
}
