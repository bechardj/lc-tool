package us.jbec.lct.controllers.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.util.LCToolUtils;

/**
 * Controller for displaying and deleting capture documents
 */
@Controller
public class DocumentController {

    Logger LOG = LoggerFactory.getLogger(DocumentController.class);

    private final CloudCaptureDocumentService cloudCaptureDocumentService;

    public DocumentController(CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    /**
     * Display a document
     * @param authentication authentication information
     * @param model provided model
     * @param id ID of document to open
     * @return view containing capture information
     */
    @GetMapping("/secure/open/document")
    public String openDocument(Authentication authentication, Model model, @RequestParam String id) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        var owns = cloudCaptureDocumentService.userOwnsDocument(user.getFirebaseIdentifier(), id);
        LOG.info("User {} opening document with id: {} - owned by user: [{}]", user.getFirebaseEmail(), id, owns);
        model.addAttribute("imageId", id);
        model.addAttribute("editable", owns);
        return "capture";
    }

    /**
     * Delete a document
     * @param authentication authentication information
     * @param model provided model
     * @param id ID of document to delete
     * @return view containing users documents
     */
    @GetMapping("/secure/delete/document")
    public RedirectView deleteDocument(Authentication authentication, Model model, @RequestParam String id) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        var owns = cloudCaptureDocumentService.userOwnsDocument(user.getFirebaseIdentifier(), id);
        if (owns) {
            LOG.info("User {} deleting document with id: {}", user.getFirebaseEmail(), id);
            cloudCaptureDocumentService.markDocumentDeleted(id);
        }
        var redirectView = new RedirectView();
        redirectView.setUrl("/secure/listing");
        return redirectView;
    }
}
