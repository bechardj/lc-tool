package us.jbec.lct.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Authentication filter for extracting Firebase tokens and attempting authentication
 */
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    AuthenticationFilter(final RequestMatcher requestMatcher) {
        super(requestMatcher);
    }

    /**
     * Extracts the firebase token from the header and attempt authentication
     * @param httpServletRequest servlet request
     * @param httpServletResponse servlet response
     * @return authentication object
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException {
        String firebaseEncryptedToken = StringUtils.isNotEmpty(httpServletRequest.getHeader(AUTHORIZATION)) ? httpServletRequest.getHeader(AUTHORIZATION) : "";
        firebaseEncryptedToken = StringUtils.removeStart(firebaseEncryptedToken , "Bearer").trim();
        Authentication requestAuthentication = new UsernamePasswordAuthenticationToken("" , firebaseEncryptedToken);
        return attemptAuthentication(requestAuthentication);
    }

    /**
     * Perform authentication directly against the authentication manager using a provided token. Used for performing
     * authentication with a token directly, when authenticating a web-based session with Firebase
     * @param usernamePasswordAuthenticationToken authentication object with credentials set to token
     * @return Authenticated authentication object
     * @throws AuthenticationException
     */
    public Authentication attemptAuthentication(Authentication usernamePasswordAuthenticationToken) throws AuthenticationException {
        return getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

}
