package us.jbec.lct.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("testLogin")
    public String login() {
        return "login";
    }
}
