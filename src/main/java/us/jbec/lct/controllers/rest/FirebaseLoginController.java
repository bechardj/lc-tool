package us.jbec.lct.controllers.rest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.security.AuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

/**
 * Controller for handling Firebase Authentication
 */
@RestController
public class FirebaseLoginController {

    private final AuthenticationFilter authenticationFilter;

    /**
     * Controller for handling Firebase Authentication
     * @param authenticationFilter autowired parameter
     */
    public FirebaseLoginController(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    /**
     * Authenticate the current session using a provided Firebase token
     * @param req servlet request containing the session to authenticate
     * @param token Firebase token to use for authentication
     */
    @PostMapping("/firebaseLogin")
    public void doLogin(HttpServletRequest req, @RequestBody String token) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("", token);
        Authentication authentication = authenticationFilter.attemptAuthentication(authenticationToken);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        HttpSession session = req.getSession(true);
        session.removeAttribute(SPRING_SECURITY_CONTEXT_KEY);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);
    }
}
