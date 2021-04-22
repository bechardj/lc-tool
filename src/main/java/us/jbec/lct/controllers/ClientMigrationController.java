package us.jbec.lct.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.services.ClientMigrationService;

import java.io.IOException;

@RestController
public class ClientMigrationController {

    Logger LOG = LoggerFactory.getLogger(ClientMigrationController.class);

    private final ClientMigrationService clientMigrationService;

    public ClientMigrationController(ClientMigrationService clientMigrationService) {
        this.clientMigrationService = clientMigrationService;
    }

    @PostMapping("/doMigration")
    public void doMigration(@RequestBody String token) throws IOException {
        clientMigrationService.doMigration(token);
    }
}
