package us.jbec.lct.controllers.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import us.jbec.lct.models.UserInvitation;
import us.jbec.lct.services.UserService;
import us.jbec.lct.util.LCToolUtils;

import javax.validation.Valid;
import java.util.stream.Collectors;

/**
 * Controller for processing user invitations
 */
@Controller
public class InvitationController {

    Logger LOG = LoggerFactory.getLogger(InvitationController.class);

    private final UserService userService;

    public InvitationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handle invitation view
     * @param authentication
     * @param model
     * @return view name
     */
    @GetMapping("/secure/invite")
    public String invite(Authentication authentication, Model model) {
        model.addAttribute("userInvitation", new UserInvitation());
        model.addAttribute("existingInvites", userService.getInvitations());
        return "invite";
    }

    /**
     * Process a submitted invite
     *
     * @param authentication
     * @param userInvitation
     * @param bindingResult
     * @param redirectAttributes
     * @return ModelAndView
     */
    @PostMapping("/secure/invite")
    public ModelAndView inviteSubmit(Authentication authentication,
                               @Valid UserInvitation userInvitation,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        if (!bindingResult.hasErrors() && userService.userExistsByEmail(userInvitation.getEmail())) {
            var error = new ObjectError("email", "User Already Has An Account.");
            bindingResult.addError(error);
        }
        if (!bindingResult.hasErrors()) {
            userService.inviteUser(user.getFirebaseIdentifier(), userInvitation);
            redirectAttributes.addFlashAttribute("success", true);
        } else {
            redirectAttributes.addFlashAttribute("validationErrors", bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\n")));
        }
        return new ModelAndView("redirect:/secure/invite");
    }

    /**
     * Delete an invitation
     * @param authentication
     * @param id invitation id to delete
     * @param redirectAttributes
     * @return ModelAndView
     */
    @GetMapping("/secure/invite/delete")
    public ModelAndView inviteDelete(Authentication authentication,
                               @RequestParam Long id,
                               RedirectAttributes redirectAttributes) {
        userService.deleteInvitation(id);
        redirectAttributes.addFlashAttribute("deleteSuccess", true);
        return new ModelAndView("redirect:/secure/invite");
    }

}
