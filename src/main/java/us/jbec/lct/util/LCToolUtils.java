package us.jbec.lct.util;


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
        User user = (User) session.getAttribute("user");
        return user;
    }
}
