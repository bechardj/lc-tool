package us.jbec.lct.util;


import org.springframework.security.core.Authentication;
import us.jbec.lct.models.database.User;
import us.jbec.lct.security.AuthorizedUser;

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
}
