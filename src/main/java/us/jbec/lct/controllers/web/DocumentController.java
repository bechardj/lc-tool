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
import us.jbec.lct.services.UserService;
import us.jbec.lct.util.LCToolUtils;

/**
 * Controller for displaying and deleting capture documents
 */
@Controller
public class DocumentController {

    Logger LOG = LoggerFactory.getLogger(DocumentController.class);

    private final CloudCaptureDocumentService cloudCaptureDocumentService;
    private final UserService userService;

    /**
     * Controller for displaying and deleting capture documents
     * @param cloudCaptureDocumentService autowired parameter
     */
    public DocumentController(CloudCaptureDocumentService cloudCaptureDocumentService, UserService userService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
        this.userService = userService;
    }

    /**
     * Display a document
     * @param authentication authentication information
     * @param model provided model
     * @param uuid ID of document to open
     * @return view containing capture information
     */
    @GetMapping("/secure/open/document")
    public String openDocument(Authentication authentication, Model model, @RequestParam String uuid) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        var prefs = userService.retrieveUserPrefs(user);
        var editable = cloudCaptureDocumentService.userCanSaveDocument(user.getFirebaseIdentifier(), uuid);
        LOG.info("User {} opening document with id: {} - editable by user: [{}]", user.getFirebaseEmail(), uuid, editable);
        model.addAttribute("imageId", uuid);
        model.addAttribute("editable", editable);
        model.addAttribute("syncOptIn", prefs.isEnableSynchronizedEditing());
        model.addAttribute("email", user.getFirebaseEmail());
        return "capture";
    }

    /**
     * Delete a document
     * @param authentication authentication information
     * @param model provided model
     * @param uuid ID of document to delete
     * @return view containing users documents
     */
    @GetMapping("/secure/delete/document")
    public RedirectView deleteDocument(Authentication authentication, Model model, @RequestParam String uuid) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        var owns = cloudCaptureDocumentService.userOwnsDocument(user.getFirebaseIdentifier(), uuid);
        if (owns) {
            LOG.info("User {} deleting document with id: {}", user.getFirebaseEmail(), uuid);
            cloudCaptureDocumentService.markDocumentDeleted(uuid, true);
        }
        var redirectView = new RedirectView();
        redirectView.setUrl("/secure/listing");
        return redirectView;
    }

    /**
     * Controller for changing whether project-level editing is enabled for a document
     * @param authentication authentication object
     * @param model view model
     * @param uuid uuid of document to change project-level editing setting for
     * @param enable whether project-level editing should be enabled
     * @return Redirect back to listing
     */
    @GetMapping("/secure/editToggle/document")
    public RedirectView toggleProjectLevelEditing(Authentication authentication,
                                                  Model model,
                                                  @RequestParam String uuid,
                                                  @RequestParam boolean enable) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        var owns = cloudCaptureDocumentService.userOwnsDocument(user.getFirebaseIdentifier(), uuid);
        if (owns) {
            LOG.info("User {} deleting document with id: {}", user.getFirebaseEmail(), uuid);
            cloudCaptureDocumentService.toggleProjectLevelEditing(uuid, enable);
        }
        var redirectView = new RedirectView();
        redirectView.setUrl("/secure/listing");
        return redirectView;
    }
}
