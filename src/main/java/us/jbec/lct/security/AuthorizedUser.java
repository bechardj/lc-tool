package us.jbec.lct.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import us.jbec.lct.models.database.User;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * POJO to represent an Authorized User of the application for Spring Security purposes.
 * Many classes are overridden or return hard-coded values, because we let Firebase handle
 * the relevant concerns.
 */
public class AuthorizedUser implements UserDetails, Principal, Serializable {

    User user;

    /**
     * POJO to represent an Authorized User of the application for Spring Security purposes.
     *
     * @param user The underlying database object to create the Authorized User from
     */

    public AuthorizedUser(User user) {
        super();
        this.user = user;
    }

    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    /**
     * Use the User Type to represent a GrantedAuthority
     */
    private static class UserAuthority implements GrantedAuthority {

        private String authority;

        public void setAuthority(String authority) {
            this.authority = authority;
        }

        @Override
        public String getAuthority() {
            return authority;
        }

    }

    /**
     * Get authority (User Roles) held by the user
     * @return collection of authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<UserAuthority> authorities = new ArrayList<>();
        for(var role : user.getRoles()) {
            var userAuthority = new UserAuthority();
            userAuthority.setAuthority("ROLE_" + role.getRoleName());
            authorities.add(userAuthority);
        }
        return authorities;
    }

    /**
     * Not used, but overridden
     * @return null
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * Not used, but overridden
     * @return empty string
     */
    @Override
    public String getUsername() {
        return "";
    }

    /**
     * Not used, but overridden
     * @return true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Not used, but overridden
     * @return true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Not used, but overridden
     * @return true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Not used, but overridden
     * @return true
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Return underlying user model
     * @return underlying user model
     */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
