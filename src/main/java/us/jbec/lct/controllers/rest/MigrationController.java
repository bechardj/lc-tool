package us.jbec.lct.controllers.rest;

import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.models.MigrationRequest;
import us.jbec.lct.services.MigrationService;

import java.io.IOException;

/**
 * Controller for handling migration of images from standalone client application to cloud instance
 */
@RestController
@Deprecated
public class MigrationController {

    private final MigrationService migrationService;

    /**
     * Controller for handling migration of images from standalone client application to cloud instance
     * @param migrationService autowired parameter
     */
    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    /**
     * Process migration requests
     * @param migrationRequest migration request to process
     * @param first whether or not this is the first image being migrated
     * @throws IOException
     * @throws FirebaseAuthException
     */
    @PostMapping("/migrate")
    public void migrate(@RequestBody MigrationRequest migrationRequest, @RequestParam("first") boolean first) throws IOException, FirebaseAuthException {
        migrationService.migrate(migrationRequest, first);
    }
}
