package us.jbec.lct.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import us.jbec.lct.models.database.Role;
import us.jbec.lct.models.database.User;
import us.jbec.lct.repositories.InvitationRepository;
import us.jbec.lct.repositories.RoleRepository;
import us.jbec.lct.repositories.UserRepository;
import us.jbec.lct.security.AuthorizedUser;
import us.jbec.lct.security.UserRoles;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

/**
 * Service for interacting with authenticated users in a development environment, constructing a default
 * development user to avoid needing to configure the Firebase Admin SDK
 */
@Service
@Profile("dev")
public class DevelopmentUserService extends UserService {

    Logger LOG = LoggerFactory.getLogger(DevelopmentUserService.class);

    private final String DEV_TOKEN = "DEVELOPER_TOKEN";
    private final String DEV_NAME = "Development User";
    private final String DEV_EMAIL = "developer@lctool.net";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectService projectService;
    private final InvitationRepository invitationRepository;
    private final ObjectMapper objectMapper;

    /**
     * Service for interacting with authenticated users
     * @param userRepository autowired parameter
     * @param roleRepository autowired parameter
     * @param projectService autowired parameter
     * @param invitationRepository autowired parameter
     * @param objectMapper autowired parameter
     */
    public DevelopmentUserService(UserRepository userRepository, RoleRepository roleRepository, ProjectService projectService,
                                  InvitationRepository invitationRepository, ObjectMapper objectMapper) {
        super(userRepository, roleRepository, projectService, null, invitationRepository, objectMapper);
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.projectService = projectService;
        this.invitationRepository = invitationRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Initialize user roles
     */
    @PostConstruct
    private void initializeRoles() {
        for(var userRole : UserRoles.values()){
            if(roleRepository.findById(userRole.getDescription()).isEmpty()) {
                var role = new Role();
                role.setRoleName(userRole.getDescription());
                roleRepository.save(role);
            }
        }
    }

    /**
     * Given an encoded token, lookup the corresponding AuthorizedUser (if one exists). If the user does not already
     * exist, a new user is created and assigned to the default project with the User Role, provided the user's email
     * is an @uconn.edu email
     *
     * @param encodedToken encoded token to decode and to then use for user lookup
     * @return the AuthorizedUser
     * @throws FirebaseAuthException
     */
    public AuthorizedUser getAuthorizedUserByToken(String encodedToken) throws FirebaseAuthException {

        var optionalUser = getUserByFirebaseIdentifier(DEV_TOKEN);
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            LOG.info("Login successful for existing user {}", user.getFirebaseEmail());
        } else {
            user = new User();
            user.setFirebaseIdentifier(DEV_TOKEN);
            user.setFirebaseEmail(DEV_EMAIL);
            user.setFirebaseName(DEV_NAME);
            user.setRoles(defaultRoles());
            user.setProject(new HashSet<>(Set.of(projectService.getDefaultProject())));
            LOG.info("First login for user {}, generating DB rows.", user.getFirebaseEmail());
            userRepository.save(user);
        }
        return new AuthorizedUser(user);

    }

    /**
     * Retrieve default user roles
     * @return Set of default user roles
     */
    private Set<Role> defaultRoles() {
        // TODO: better handle initial creation here for testing
        var roles = new HashSet<Role>();
        roles.add(roleRepository.findById(UserRoles.USER.getDescription()).get());
        return roles;
    }

}
