package us.jbec.lct.controllers.web;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

/**
 * Controller for handling user logout
 */
@Controller
public class LogoutController {

    /**
     * Perform user logout by clearing security context
     * @param req servlet request to clear security context from
     * @return logout view
     */
    @GetMapping("/logout")
    public String doLogout(HttpServletRequest req) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null)
        {
            securityContext.setAuthentication(null);
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.removeAttribute(SPRING_SECURITY_CONTEXT_KEY);
                session.removeAttribute("user");
                session.removeAttribute("prefs");
            }
        }
        return "logout";
    }
}
