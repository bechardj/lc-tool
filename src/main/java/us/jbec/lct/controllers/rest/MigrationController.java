package us.jbec.lct.controllers.rest;

import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.models.MigrationRequest;
import us.jbec.lct.services.MigrationService;

import java.io.IOException;

@RestController
public class MigrationController {

    private final MigrationService migrationService;

    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/migrate")
    public void migrate(@RequestBody MigrationRequest migrationRequest, @RequestParam("first") boolean first) throws IOException, FirebaseAuthException {
        migrationService.migrate(migrationRequest, first);
    }
}
