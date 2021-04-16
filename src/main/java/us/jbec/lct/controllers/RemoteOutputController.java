package us.jbec.lct.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;
import us.jbec.lct.services.ZipOutputService;


@Controller
@Profile("remote")
public class RemoteOutputController {
    private final ZipOutputService zipOutputService;

    public RemoteOutputController(ZipOutputService zipOutputService) {
        this.zipOutputService = zipOutputService;
    }

    @GetMapping("/bulkOutput/latest")
    public RedirectView getLatestZip() {
        var zipUri = zipOutputService.getLatestZipUri();
        if (zipUri.isEmpty()) {
            throw new RuntimeException("No Zips Available!");
        }
        var redirectView = new RedirectView();
        redirectView.setUrl(zipUri.get());
        return redirectView;
    }
}
