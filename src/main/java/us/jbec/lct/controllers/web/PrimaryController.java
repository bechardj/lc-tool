package us.jbec.lct.controllers.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrimaryController {


    Logger LOG = LoggerFactory.getLogger(PrimaryController.class);
    public PrimaryController() {
        this.LOG = LOG;
    }

    @GetMapping("help")
    public String help(){
        return "help";
    }

    @GetMapping("shortcuts")
    public String shortcuts(){
        return "shortcuts";
    }

    @GetMapping("/")
    public String statistics(Model model) {
        return "home";
    }

    @GetMapping("login")
    public String login(Model model) {
        return "login";
    }
}
