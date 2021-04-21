package us.jbec.lct.controllers;

import com.google.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrimaryRemoteController {

    Logger LOG = LoggerFactory.getLogger(PrimaryRemoteController.class);

    @GetMapping("/")
    public String statistics(Model model) {
        return "statistics";
    }


}
