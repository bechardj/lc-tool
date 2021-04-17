package us.jbec.lct.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;
import us.jbec.lct.services.ZipOutputService;

import javax.servlet.http.HttpServletRequest;


@Controller
@Profile("remote")
public class RemoteOutputController {

    @Value("${lct.remote.baseUrl:#{null}}")
    private String baseUrl;

    private final ZipOutputService zipOutputService;

    public RemoteOutputController(ZipOutputService zipOutputService) {
        this.zipOutputService = zipOutputService;
    }

    @GetMapping("/bulkOutput/latest")
    public RedirectView getLatestZip(HttpServletRequest request) {
        var optionalZipUri = zipOutputService.getLatestZipUri();
        if (optionalZipUri.isEmpty()) {
            throw new RuntimeException("No Zips Available!");
        }
        var redirectView = new RedirectView();
        var zipUri = optionalZipUri.get();
        // TODO: this is a temp hack to avoid https errors
        if (baseUrl != null && request.getRequestURL().toString().contains(baseUrl)) {
            zipUri = "https://" + baseUrl + zipUri;
        }
        redirectView.setUrl(zipUri);
        return redirectView;
    }
}
