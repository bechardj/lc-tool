package us.jbec.lct.controllers.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web Controller for handling general page views
 */
@Controller
public class PrimaryController {

    Logger LOG = LoggerFactory.getLogger(PrimaryController.class);
    public PrimaryController() {
        this.LOG = LOG;
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
    public String statistics() {
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
}
