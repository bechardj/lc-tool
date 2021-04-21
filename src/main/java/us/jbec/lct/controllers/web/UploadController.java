package us.jbec.lct.controllers.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import us.jbec.lct.models.database.User;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.services.IngestService;
import us.jbec.lct.util.LCToolUtils;

import java.io.IOException;

public class UploadController {

    Logger LOG = LoggerFactory.getLogger(UploadController.class);

    private final IngestService ingestService;

    private final CloudCaptureDocumentService cloudCaptureDocumentService;

    public UploadController(IngestService ingestService, CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.ingestService = ingestService;
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    @PostMapping("/secure/image/upload")
    public String imageUpload(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        User user = LCToolUtils.getUserFromAuthentication(authentication);
        ingestService.ingest(user, file);
        return "help";
    }

    @PostMapping("/secure/job/upload")
    public String jobUpload(Model model, Authentication authentication, @RequestParam("file") MultipartFile file, @RequestParam String id) throws IOException {
        User user = LCToolUtils.getUserFromAuthentication(authentication);
        cloudCaptureDocumentService.saveCloudCaptureDocument(user.getFirebaseIdentifier(), file, id);
        var owns = cloudCaptureDocumentService.userOwnsDocument(user.getFirebaseIdentifier(), id);
        LOG.info("Opening document with id: {}", id);
        model.addAttribute("imageId", id);
        model.addAttribute("editable", owns);
        return "capture";
    }
}
