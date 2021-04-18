package us.jbec.lct.controllers;

import org.springframework.boot.info.BuildProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.security.AuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.ZoneId;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@RestController
public class FirebaseLoginController {

    private final AuthenticationFilter authenticationFilter;

    public FirebaseLoginController(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @PostMapping("/firebaseLogin")
    public String doLogin(HttpServletRequest req, @RequestBody String token) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("", token);
        Authentication authentication = authenticationFilter.attemptAuthentication(authenticationToken);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        HttpSession session = req.getSession(true);
        session.removeAttribute(SPRING_SECURITY_CONTEXT_KEY);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);
        return "success";
    }

    @GetMapping("/devlogout")
    public String doLogout(HttpServletRequest req) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(null);
        HttpSession session = req.getSession(false);
        session.removeAttribute(SPRING_SECURITY_CONTEXT_KEY);
        return "Successfully logged out.";
    }

}
