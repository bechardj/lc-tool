package us.jbec.lct.controllers.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import us.jbec.lct.models.DynamicTextType;
import us.jbec.lct.models.database.DynamicText;
import us.jbec.lct.models.database.User;
import us.jbec.lct.services.DynamicTextService;
import us.jbec.lct.services.UserService;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Web Controller for handling general page views
 */
@Controller
public class PrimaryController {

    Logger LOG = LoggerFactory.getLogger(PrimaryController.class);

    private final DynamicTextService dynamicTextService;
    private final UserService userService;

    public PrimaryController(DynamicTextService dynamicTextService,
                             UserService userService) {
        this.dynamicTextService = dynamicTextService;
        this.userService = userService;
    }

    /**
     * Help page endpoint
     * @return help page view
     */
    @GetMapping("help")
    public String help(){
        return "help";
    }

    /**
     * License page
     * @return License page view
     */
    @GetMapping("license")
    public String license(){
        return "license";
    }

    /**
     * Shortcuts endpoint
     * @return shortcuts view
     */
    @GetMapping("shortcuts")
    public String shortcuts(){
        return "shortcuts";
    }

    /**
     * Root endpoint
     * @return Home page view
     */
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        var maintenance = dynamicTextService.retrieveDynamicText("maintenance");
        var user = (User) session.getAttribute("user");
        maintenance.ifPresent(s -> model.addAttribute("maintenance", s));
        var releaseNotes = dynamicTextService.retrieveDynamicTextByType(DynamicTextType.RELEASE_NOTES);
        if (null != user && !releaseNotes.isEmpty()) {
            List<DynamicText> displayableReleaseNotes;
                    var max = userService.retrieveUserPrefs(user)
                    .getAcknowledgedDynamicText().get(DynamicTextType.RELEASE_NOTES);
            if (max == null) {
                displayableReleaseNotes = releaseNotes;
            } else {
                displayableReleaseNotes = releaseNotes.stream()
                        .filter(t -> t.getSortOrder() > max)
                        .toList();
            }
            if (!displayableReleaseNotes.isEmpty()) {
                model.addAttribute("releaseNotes", displayableReleaseNotes);
                model.addAttribute("releaseNotesDismiss", displayableReleaseNotes.get(0).getSortOrder());
            }
        }
        return "home";
    }

    /**
     * Login endpoint
     * @return Login view
     */
    @GetMapping("login")
    public String login() {
        return "login";
    }

    /**
     * Request Invite endpoint
     * @return Invite Request view
     */
    @GetMapping("requestInvite")
    public String requestInvite() {
        return "requestInvite";
    }


    /**
     * Request Invite endpoint
     * @return Invite Request view
     */
    @GetMapping("/release/{version}")
    public String releaseNotes(@PathVariable String version) {
        return "/release/" + version;
    }
}
