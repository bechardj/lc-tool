package us.jbec.lct.services;

import org.springframework.stereotype.Service;
import us.jbec.lct.models.database.Project;
import us.jbec.lct.repositories.ProjectRepository;

/**
 * Service for interacting with projects. Currently, this service only returns the default project
 */
@Service
public class ProjectService {

    private final String DEFAULT_PROJECT_NAME = "UCONN_LYRASIS_2021";


    private final ProjectRepository projectRepository;

    /**
     * Service for interacting with projects. Currently, this service only returns the default project
     * @param projectRepository autowired parameter
     */
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Get the default project
     * @return the default project
     */
    public Project getDefaultProject() {
        var defaultProject = projectRepository.selectProjectByName(DEFAULT_PROJECT_NAME);
        if (defaultProject.isEmpty()) {
            var initializedProject = new Project();
            initializedProject.setName(DEFAULT_PROJECT_NAME);
            projectRepository.save(initializedProject);
            return initializedProject;
        } else {
            return defaultProject.get();
        }
    }
}
