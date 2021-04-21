package com.sdp.sp.back.utils;

import com.sdp.sp.back.models.AuthorizedUser;
import com.sdp.sp.backend_shared.models.PersistedUser;
import org.springframework.security.core.Authentication;

import java.util.Set;

public class SDPUtils {

    public static final String DEFAULT_CODE = "NONE";

    /**
     * Retrieve Persisted User from Spring Security Authentication Object
     * @param authorization Authorization object to retrieve PersistedUser from
     * @return Retrieved PersistedUser
     */
    public static PersistedUser getPersistedUserFromAuthentication(Authentication authorization) {
        AuthorizedUser authorizedUser = (AuthorizedUser) authorization.getPrincipal();
        return authorizedUser.getPersistedUser();
    }

    /**
     * Remove default codes from string sets if at least one non-default code exists
     * @param stringSet set to clean up
     * @return cleaned string set
     */
    public static Set<String> cleanDefaultCodesStringSet(Set<String> stringSet) {
        if (stringSet.stream().anyMatch(code -> !code.equals(DEFAULT_CODE))) {
            stringSet.remove(DEFAULT_CODE);
        }
        return stringSet;
    }
}
