package uk.ac.ebi.fgpt.zooma.security;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Allows user details to be retrieved and stored in ZOOMA
 *
 * @author Tony Burdett
 * @date 28/02/14
 */
public interface ApiKeyStore {
    UserDetails getDetailsByUsername(String username);

    UserDetails getDetailsByApiKey(String apiKey);

    void storeUserDetails(UserDetails user);
}
