package us.jbec.lct.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import us.jbec.lct.models.database.User;
import us.jbec.lct.repositories.UserRepository;
import us.jbec.lct.security.AuthorizedUser;

@Service
public class UserService {

    Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final FirebaseAuth firebaseAuth;

    public UserService(UserRepository userRepository, FirebaseAuth firebaseAuth) {
        this.userRepository = userRepository;
        this.firebaseAuth = firebaseAuth;
    }

    public AuthorizedUser findByToken(String encodedToken) throws FirebaseAuthException {
        try {
            var token = firebaseAuth.verifyIdToken(encodedToken);
            var optionalUser = userRepository.findById(token.getUid());
            User user;
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                LOG.info("Login successful for existing user {}", user.getFirebaseEmail());
            } else {
                user = new User(token);
                LOG.info("First login for user {}, generating DB rows.", user.getFirebaseEmail());
                userRepository.save(user);
            }
            return new AuthorizedUser(user);

        } catch (FirebaseAuthException e) {
            LOG.error("Bad token provided!");
            throw e;
        }
    }
}
