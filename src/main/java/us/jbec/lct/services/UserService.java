package us.jbec.lct.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import us.jbec.lct.models.UserRoles;
import us.jbec.lct.models.database.Role;
import us.jbec.lct.models.database.User;
import us.jbec.lct.repositories.RoleRepository;
import us.jbec.lct.repositories.UserRepository;
import us.jbec.lct.security.AuthorizedUser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    Logger LOG = LoggerFactory.getLogger(UserService.class);

    private static final String USER_ROLE = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FirebaseAuth firebaseAuth;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, FirebaseAuth firebaseAuth) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.firebaseAuth = firebaseAuth;
    }

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

    public Optional<User> getUserByFirebaseIdentifier(String identifier) {
        return userRepository.findById(identifier);
    }

    private Set<Role> defaultRoles() {
        // TODO: better handle initial creation here for testing
        var roles = new HashSet<Role>();
        Role userRole;
        var optionalUserRole = roleRepository.findById(UserRoles.USER.getDescription());
        if (optionalUserRole.isEmpty()) {
            userRole = new Role();
            userRole.setRoleName(UserRoles.USER.getDescription());
            roleRepository.save(userRole);
        } else {
            userRole = optionalUserRole.get();
        }
        roles.add(userRole);
        return roles;
    }

}
