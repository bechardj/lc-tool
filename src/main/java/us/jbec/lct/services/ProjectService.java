package us.jbec.lct.services;

import org.springframework.stereotype.Service;
import us.jbec.lct.models.database.Project;
import us.jbec.lct.repositories.ProjectRepository;

@Service
public class ProjectService {

    private final Long DEFAULT_PROJECT_ID = 0L;
    private final String DEFAULT_PROJECT_NAME = "UCONN_LYRASIS_2021";


    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

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
