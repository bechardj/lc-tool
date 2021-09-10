package us.jbec.lct.upgrade.v2_0_0;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.repositories.UserRepository;
import us.jbec.lct.services.ProjectService;
import us.jbec.lct.upgrade.Upgrade;

import java.util.HashSet;
import java.util.Set;

/**
 * On versions prior to 2.0.0, users were not being assigned to a default project. This upgrade resolves this issue
 * in order to enable project-level editing
 */
@Component
public class DefaultProjectAssigner implements Upgrade {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProjectAssigner.class);

    private final ProjectService projectService;
    private final UserRepository userRepository;

    public DefaultProjectAssigner(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean optional() {
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void execute() throws RuntimeException {
        LOG.info("Start Default Project Assignment from pre-2.0.0");
        for(var user : userRepository.findAll()) {
            if (user.getProject() == null || user.getProject().isEmpty()) {
                LOG.info("Assigning user {} to default project", user.getFirebaseIdentifier());
                user.setProject(new HashSet<>(Set.of(projectService.getDefaultProject())));
                userRepository.save(user);
            }
        }
        LOG.info("Completed default project assignment");
    }
}
