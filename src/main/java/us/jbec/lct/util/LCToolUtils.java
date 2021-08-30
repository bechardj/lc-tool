package us.jbec.lct.util;


import org.springframework.messaging.Message;
import org.springframework.security.core.Authentication;
import us.jbec.lct.models.database.User;
import us.jbec.lct.security.AuthorizedUser;

import javax.servlet.http.HttpSession;

/**
 * General utility class
 */
public class LCToolUtils {

    /**
     * Retrieve User from Spring Security Authentication Object
     * @param authorization Authorization object to retrieve User from
     * @return Retrieved User
     */
    public static User getUserFromAuthentication(Authentication authorization) {
        AuthorizedUser authorizedUser = (AuthorizedUser) authorization.getPrincipal();
        return authorizedUser.getUser();
    }

    /**
     * Retrieve User from Session Attribute
     * @param session HttpSession object to retrieve User from
     * @return Retrieved User
     */
    public static User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    public static User getUserFromMessage(Message<?> message) {
        var authorizedUser = ((AuthorizedUser) message.getHeaders().get("simpUser"));
        if (authorizedUser == null) {
            return null;
        } else {
            return authorizedUser.getUser();
        }
    }
}
