package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUser;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple cache for storing the key details of authenticated users in a memory based cache.  Handy for testing
 * environments.
 *
 * @author Tony Burdett
 * @date 27/10/13
 */
public class InMemoryApiKeyStore implements ApiKeyStore {
    private final Map<String, UserDetails> usernameUserMap;
    private final Map<String, UserDetails> apiKeyUserMap;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public InMemoryApiKeyStore() {
        this.usernameUserMap = new HashMap<>();
        this.apiKeyUserMap = new HashMap<>();
    }

    @Override
    public UserDetails getDetailsByUsername(String username) {
        return usernameUserMap.get(username);
    }

    @Override
    public UserDetails getDetailsByApiKey(String apiKey) {
        return apiKeyUserMap.get(apiKey);
    }

    @Override
    public void storeUserDetails(UserDetails user) {
        getLog().debug("Storing user details: " + user);
        usernameUserMap.put(user.getUsername(), user);
        if (user instanceof ZoomaUser) {
            apiKeyUserMap.put(((ZoomaUser) user).getApiKey(), user);
        }
    }
}
