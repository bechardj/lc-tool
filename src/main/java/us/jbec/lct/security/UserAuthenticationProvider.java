package us.jbec.lct.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import us.jbec.lct.services.UserService;

@Component
public class UserAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    final Logger LOG = LoggerFactory.getLogger(UserAuthenticationProvider.class);

    @Autowired
    UserService userService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    /**
     * Retrieve UserDetails by searching the token passed in the
     * UsernamePasswordAuthenticationToken
     */
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();
        try {
            return userService.getAuthorizedUserByToken(token);
        } catch (RuntimeException | FirebaseAuthException e) {
            LOG.error("User authentication failed.");
            throw new UsernameNotFoundException("Invalid Username!");
        }
    }


}
