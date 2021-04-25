package us.jbec.lct.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import us.jbec.lct.models.database.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuthorizedUser implements UserDetails {

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

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
