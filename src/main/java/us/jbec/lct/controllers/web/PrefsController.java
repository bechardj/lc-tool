package us.jbec.lct.controllers.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import us.jbec.lct.models.UserPrefsForm;
import us.jbec.lct.services.UserService;
import us.jbec.lct.util.LCToolUtils;

import javax.validation.Valid;

/**
 * Controller for processing user preferences
 */
@Controller
public class PrefsController {

    private final UserService userService;

    public PrefsController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handle preferences view
     * @param authentication
     * @param model
     * @return view name
     */
    @GetMapping("/secure/prefs")
    public String invite(Authentication authentication, Model model) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        var prefs = userService.retrieveUserPrefs(user);
        model.addAttribute("userPrefs", new UserPrefsForm(prefs));
        return "prefs";
    }

    /**
     * Process a preferences change
     *
     * @param authentication
     * @param bindingResult
     * @param redirectAttributes
     * @return ModelAndView
     */
    @PostMapping("/secure/prefs")
    public ModelAndView inviteSubmit(Authentication authentication,
                               @Valid UserPrefsForm userPrefsForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) throws JsonProcessingException {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        var prefs = userService.retrieveUserPrefs(user);

        prefs.setEnableSynchronizedEditing(userPrefsForm.isEnableSynchronizedEditing());

        userService.updateUserPrefs(user, prefs);
        redirectAttributes.addFlashAttribute("success", true);

        return new ModelAndView("redirect:/secure/prefs");
    }
}
