package us.jbec.lct.controllers.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@RestController
public class FirebaseProxyController {

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/__/auth/**")
    public ResponseEntity<String> firebaseProxy(HttpServletRequest request) {
        String path = request.getRequestURI();
        ResponseEntity<String> response = restTemplate.getForEntity("https://lc-tool.firebaseapp.com" + path, String.class);
        return response;
    }

}
