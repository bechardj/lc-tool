package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.models.LCToolAuthException;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.UserInvitation;
import us.jbec.lct.models.UserPrefs;
import us.jbec.lct.models.database.InvitationRecord;
import us.jbec.lct.models.database.Role;
import us.jbec.lct.models.database.User;
import us.jbec.lct.repositories.InvitationRepository;
import us.jbec.lct.repositories.RoleRepository;
import us.jbec.lct.repositories.UserRepository;
import us.jbec.lct.security.AuthorizedUser;
import us.jbec.lct.security.UserRoles;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final ProjectService projectService;
    private final FirebaseAuth firebaseAuth;
    private final InvitationRepository invitationRepository;
    private final ObjectMapper objectMapper;

    /**
     * Service for interacting with authenticated users
     * @param userRepository autowired parameter
     * @param roleRepository autowired parameter
     * @param projectService autowired parameter
     * @param firebaseAuth autowired parameter
     * @param invitationRepository autowired parameter
     * @param objectMapper autowired parameter
     */
    public UserService(UserRepository userRepository, RoleRepository roleRepository, ProjectService projectService,
                       FirebaseAuth firebaseAuth, InvitationRepository invitationRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.projectService = projectService;
        this.firebaseAuth = firebaseAuth;
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
        try {
            var token = firebaseAuth.verifyIdToken(encodedToken);
            var optionalUser = getUserByFirebaseIdentifier(token.getUid());
            User user;
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                LOG.info("Login successful for existing user {}", user.getFirebaseEmail());
            } else {
                user = new User(token);
                if (!user.getFirebaseEmail().endsWith("@uconn.edu")) {
                    var invitations = invitationRepository.selectInvitationByEmail(user.getFirebaseEmail());
                    if (invitations.isEmpty()) {
                        LOG.error("User {} Without UConn Account Attempted Login with No Invitation", user.getFirebaseEmail());
                        throw new LCToolAuthException("Bad Domain");
                    } else {
                        LOG.error("Creating user and removing invitations for user {}", user.getFirebaseEmail());
                        invitations.forEach(invitationRepository::delete);
                    }
                }
                user.setRoles(defaultRoles());
                user.setProject(new HashSet<>(Set.of(projectService.getDefaultProject())));
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
     * Check if user exists with provided email
     * @param email email to check
     * @return if the email corresponds to an existing user
     */
    public boolean userExistsByEmail(String email) {
        return !userRepository.findByFirebaseEmail(email).isEmpty();
    }

    /**
     * Retrieve user prefs, returning default user prefs object if none exist or an error occurs
     * @param user user to retrieve prefs for
     * @return user prefs
     */
    public UserPrefs retrieveUserPrefs(User user) {
        try {
            var prefs = user.getUserPrefs();
            if (null == prefs) {
                UserPrefs userPrefs = new UserPrefs();
                userPrefs.setEnableSynchronizedEditing(true);
                updateUserPrefs(user, userPrefs);
            }
            return objectMapper.readValue(user.getUserPrefs(), UserPrefs.class);
        } catch (Exception e) {
            LOG.error("Failed to retrieve/setup User Prefs. Returning Default.");
            return new UserPrefs();
        }
    }

    /**
     * Update the user's prefs
     * @param user user to update the prefs of
     * @param userPrefs updated user prefs
     * @throws JsonProcessingException
     */
    public void updateUserPrefs(User user, UserPrefs userPrefs) throws JsonProcessingException {
        user.setUserPrefs(objectMapper.writeValueAsString(userPrefs));
        userRepository.save(user);
    }

    /**
     * Invite user to application
     * @param userToken inviter's user token
     * @param userInvitation user invitation
     */
    @Transactional
    public void inviteUser(String userToken, UserInvitation userInvitation) {
        Optional<User> optionalUser = getUserByFirebaseIdentifier(userToken);
        if (optionalUser.isPresent()) {
            var user = optionalUser.get();
            LOG.info("Creating invitation for {} requested by: {}", userInvitation.getEmail(), user.getFirebaseEmail());

            var existingInvites = invitationRepository.selectInvitationByEmail(userInvitation.getEmail());
            if (!existingInvites.isEmpty()) {
                LOG.info("Cleaning up existing invites for {}", userInvitation.getEmail());
                existingInvites.forEach(invite -> invitationRepository.deleteById(invite.getId()));
            }

            var invitationRecord = new InvitationRecord();
            invitationRecord.setEmail(userInvitation.getEmail());
            invitationRecord.setRoles(defaultRoles());
            invitationRecord.setRequester(user);
            invitationRepository.save(invitationRecord);
        } else {
            throw new LCToolException("Invalid requester");
        }
    }

    /**
     * Delete invite by ID
     * @param id id to delete
     */
    @Transactional
    public void deleteInvitation(Long id) {
        invitationRepository.deleteById(id);
    }

    /**
     * Get all pending invitations from database
     * @return list of pending UserInvitations
     */
    public List<UserInvitation> getInvitations() {
        var userInvitations = new ArrayList<UserInvitation>();
        invitationRepository.findAll().forEach(invitationRecord -> {
            var userInvitation = new UserInvitation();
            userInvitation.setId(invitationRecord.getId());
            userInvitation.setEmail(invitationRecord.getEmail());
            userInvitation.setRequestedBy(invitationRecord.getRequester().getFirebaseEmail());
            userInvitation.setRequestedRole(invitationRecord.getRoles().stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.joining(", ")));
            userInvitations.add(userInvitation);
        });

        return userInvitations;
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
