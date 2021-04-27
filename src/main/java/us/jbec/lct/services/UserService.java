package us.jbec.lct.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.security.UserRoles;
import us.jbec.lct.models.database.Role;
import us.jbec.lct.models.database.User;
import us.jbec.lct.repositories.RoleRepository;
import us.jbec.lct.repositories.UserRepository;
import us.jbec.lct.security.AuthorizedUser;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Service for interacting with authenticated users
 */
@Primary
@Profile("!dev")
@Service
public class UserService {

    Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FirebaseAuth firebaseAuth;

    /**
     * Service for interacting with authenticated users
     * @param userRepository autowired parameter
     * @param roleRepository autowired parameter
     * @param firebaseAuth autowired parameter
     */
    public UserService(UserRepository userRepository, RoleRepository roleRepository, FirebaseAuth firebaseAuth) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.firebaseAuth = firebaseAuth;
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
        try {
            var token = firebaseAuth.verifyIdToken(encodedToken);
            var optionalUser = getUserByFirebaseIdentifier(token.getUid());
            User user;
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                LOG.info("Login successful for existing user {}", user.getFirebaseEmail());
            } else {
                user = new User(token);
                if (!user.getFirebaseEmail().contains("uconn.edu")) {
                    LOG.error("User Without UConn Account Attempted Login");
                    throw new LCToolException("Bad Domain");
                }
                user.setRoles(defaultRoles());
                LOG.info("First login for user {}, generating DB rows.", user.getFirebaseEmail());
                userRepository.save(user);
            }
            return new AuthorizedUser(user);

        } catch (FirebaseAuthException e) {
            LOG.error("Bad token provided!");
            throw e;
        }
    }

    /**
     * Find a User (if one exists) from the database using a decoded firebase UUID
     * @param uuid Decoded firebase UUID to use for the user lookup
     * @return optionally return the user for the given firebase UUID
     */
    public Optional<User> getUserByFirebaseIdentifier(String uuid) {
        return userRepository.findById(uuid);
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
