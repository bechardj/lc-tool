package us.jbec.lct.controllers.web;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Controller
public class LogoutController {

    @GetMapping("/secure/logout")
    public String doLogout(HttpServletRequest req) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(null);
        HttpSession session = req.getSession(false);
        session.removeAttribute(SPRING_SECURITY_CONTEXT_KEY);
        return "logout";
    }
}
