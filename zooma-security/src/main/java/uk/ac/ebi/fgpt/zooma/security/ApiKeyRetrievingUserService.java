package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * A simple service for loading known user details via either their usernmae or their API key.
 *
 * @author Tony Burdett
 * @date 27/10/13
 */
public class ApiKeyRetrievingUserService
        implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    private ApiKeyStore keyStore;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ApiKeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(ApiKeyStore keyStore) {
        this.keyStore = keyStore;
    }


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return getKeyStore().getDetailsByApiKey(s);
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken)
            throws UsernameNotFoundException {
        return getKeyStore().getDetailsByApiKey(preAuthenticatedAuthenticationToken.getPrincipal().toString());
    }
}
