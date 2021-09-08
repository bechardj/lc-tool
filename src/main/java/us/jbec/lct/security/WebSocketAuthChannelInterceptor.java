package us.jbec.lct.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Authenticates WebSocket before allowing connection to be made.
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private String BEARER_TOKEN = "token";

    final Logger LOG = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final AuthenticationFilter authenticationFilter;

    public WebSocketAuthChannelInterceptor(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assert accessor != null;
        if(accessor.getCommand() == StompCommand.CONNECT) {
            final String token = accessor.getFirstNativeHeader(BEARER_TOKEN);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("", token);
            Authentication authentication = authenticationFilter.attemptAuthentication(authenticationToken);
            AuthorizedUser user = ((AuthorizedUser) authentication.getPrincipal());
            LOG.info("User {} established WebSocket", user.getUser().getFirebaseEmail());
            accessor.setUser(user);
        }
        return message;

    }

}
