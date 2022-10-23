package us.jbec.lct.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Currently, logs are being filled with Rejected Requests due to malformed URLs. This handler should temporarily reduce the amount
 * of logging generated when these events occur, and determine whether they are the result of bots or legit user requests
 */
@Configuration
public class FirewallConfig {

    @Bean
    public RequestRejectedHandler requestRejectedHandler() {
        return new DebugRejectRequestHandler();
    }

    protected static class DebugRejectRequestHandler extends HttpStatusRequestRejectedHandler {
        final Logger LOG = LoggerFactory.getLogger(DebugRejectRequestHandler.class);

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, RequestRejectedException requestRejectedException) throws IOException {
            LOG.debug("Rejecting request: {}", request.getRequestURL().toString());
            super.handle(request, response, requestRejectedException);
        }

    }
}
