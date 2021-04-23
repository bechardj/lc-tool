package us.jbec.lct.controllers.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;
import us.jbec.lct.services.ZipOutputService;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for serving output of processing data
 */
@Controller
public class OutputController {

    @Value("${lct.remote.baseUrl:#{null}}")
    private String baseUrl;

    private final ZipOutputService zipOutputService;

    /**
     * Controller for serving output of processing data
     * @param zipOutputService autowired parameter
     */
    public OutputController(ZipOutputService zipOutputService) {
        this.zipOutputService = zipOutputService;
    }

    /**
     * Endpoint to get the latest bulk processing output
     * @param request request to get base URL from
     * @return redirect view to latest zip archive
     */
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
