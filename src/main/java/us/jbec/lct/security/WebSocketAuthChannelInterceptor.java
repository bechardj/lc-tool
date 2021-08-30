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
import org.springframework.stereotype.Service;

@Service
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private String BEARER_TOKEN = "token";

    final Logger LOG = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final AuthenticationFilter authenticationFilter;

    public WebSocketAuthChannelInterceptor(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Instantiate an object for retrieving the STOMP headers
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        // Check that the object is not null
        assert accessor != null;
        // If the frame is a CONNECT frame
        if(accessor.getCommand() == StompCommand.CONNECT) {
            LOG.debug("authenticate WS connect");
            final String token = accessor.getFirstNativeHeader(BEARER_TOKEN);
            LOG.debug("Token: {}", token);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("", token);
            Authentication authentication = authenticationFilter.attemptAuthentication(authenticationToken);
            AuthorizedUser user = ((AuthorizedUser) authentication.getPrincipal());
            accessor.setUser(user);
        }

        return message;

    }

}
