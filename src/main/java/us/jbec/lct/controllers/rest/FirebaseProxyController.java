package us.jbec.lct.controllers.rest;

import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class FirebaseProxyController {

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/__/auth/**")
    public ResponseEntity<String> firebaseProxy(HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        query = query != null ? ("?" + query) : "";
        ResponseEntity<String> response = restTemplate.getForEntity("https://lc-tool.firebaseapp.com" + path + query, String.class);
        return response;
    }

    @PostMapping("/__/auth/**")
    public String firebaseProxyPost(HttpServletRequest request,
                                                    @RequestBody(required = false) String body,
                                                    @RequestHeader MultiValueMap<String, String> headers) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        query = query != null ? ("?" + query) : "";
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.postForObject("https://lc-tool.firebaseapp.com" + path + query, httpEntity, String.class);
    }

}
