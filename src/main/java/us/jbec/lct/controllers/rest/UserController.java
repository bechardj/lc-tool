package us.jbec.lct.controllers.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import us.jbec.lct.models.DynamicTextType;
import us.jbec.lct.models.LCToolResponse;
import us.jbec.lct.security.AuthenticationFilter;
import us.jbec.lct.security.AuthorizedUser;
import us.jbec.lct.services.UserService;
import us.jbec.lct.util.LCToolUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

/**
 * Controller for handling Firebase Authentication and other User Functions
 */
@RestController
public class UserController {

    private final AuthenticationFilter authenticationFilter;
    private final UserService userService;

    /**
     * Controller for handling Firebase Authentication
     * @param authenticationFilter autowired parameter
     */
    public UserController(AuthenticationFilter authenticationFilter,
                          UserService userService) {
        this.authenticationFilter = authenticationFilter;
        this.userService = userService;
    }

    /**
     * Authenticate the current session using a provided Firebase token
     * @param req servlet request containing the session to authenticate
     * @param token Firebase token to use for authentication
     */
    @PostMapping("/firebaseLogin")
    public LCToolResponse doLogin(HttpServletRequest req, @RequestBody String token) {
        var response = new LCToolResponse(false, "");
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("", token);
            Authentication authentication = authenticationFilter.attemptAuthentication(authenticationToken);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            HttpSession session = req.getSession(true);
            session.removeAttribute(SPRING_SECURITY_CONTEXT_KEY);
            session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);
            // not particularly efficient but convent for thymeleaf
            var user = ((AuthorizedUser) authentication.getPrincipal()).getUser();
            session.setAttribute("user", user);
            session.setAttribute("prefs", userService.retrieveUserPrefs(user));
        } catch (BadCredentialsException exception) {
            response.setError(true);
            response.setInfo(exception.getMessage());
        }
        return response;
    }

    /**
     * Dismiss release notes
     *
     * @param session
     * @param dismissUpTo sort order of dynamic release up to which the user is dismissing the message
     * @throws JsonProcessingException
     */
    @GetMapping("/user/dismiss/release")
    public void dismissReleaseNotes(HttpSession session, @RequestParam Integer dismissUpTo) throws JsonProcessingException {
        var user = LCToolUtils.getUserFromSession(session);
        var prefs = userService.retrieveUserPrefs(user);
        var acknowledged = prefs.getAcknowledgedDynamicText();
        acknowledged.put(DynamicTextType.RELEASE_NOTES, dismissUpTo);
        userService.updateUserPrefs(user, prefs);
    }
}
