package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUserImpl;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A service which accepts any OpenID user, "registering" new users in a map so they can be welcomed back to the site on
 * subsequent logins.
 *
 * @author Tony Burdett
 * @date 14/10/13
 */
public class ApiKeyGeneratingUserService
        implements UserDetailsService, AuthenticationUserDetailsService<OpenIDAuthenticationToken> {
    private static final List<GrantedAuthority> DEFAULT_AUTHORITIES = AuthorityUtils.createAuthorityList("ROLE_EDITOR");

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

    /**
     * Implementation of {@code UserDetailsService}. We only need this to satisfy the {@code RememberMeServices}
     * requirements.
     */
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        getLog().debug("Attempting to load user details with id = " + id);
        UserDetails user = getKeyStore().getDetailsByUsername(id);
        if (user == null) {
            throw new UsernameNotFoundException(id);
        }
        return user;
    }

    /**
     * Implementation of {@code AuthenticationUserDetailsService} which allows full access to the submitted {@code
     * Authentication} object. Used by the OpenIDAuthenticationProvider.
     */
    public UserDetails loadUserDetails(OpenIDAuthenticationToken token) {
        UserDetails user = null;

        String id = token.getIdentityUrl();
        String email = null;
        String firstName = null;
        String surname = null;
        String fullName = null;
        getLog().debug("ID for openID token: " + id + ". Retrieve/create user...");

        // collect attributes from OpenID info
        List<OpenIDAttribute> attributes = token.getAttributes();
        for (OpenIDAttribute attribute : attributes) {
            if (attribute.getName().equals("email")) {
                email = attribute.getValues().get(0);

                // can we retrieve user by email?
                user = getKeyStore().getDetailsByUsername(email);
                if (user != null) {
                    break;
                }
            }
            if (attribute.getName().equals("firstname")) {
                firstName = attribute.getValues().get(0);
            }
            if (attribute.getName().equals("lastname")) {
                surname = attribute.getValues().get(0);
            }
            if (attribute.getName().equals("fullname")) {
                fullName = attribute.getValues().get(0);
            }
        }

        // if no fullname retrieved, generate
        if (fullName == null) {
            StringBuilder sb = new StringBuilder();
            if (firstName != null) {
                sb.append(firstName);
            }
            if (surname != null) {
                sb.append(" ").append(surname);
            }
            fullName = sb.toString();
        }

        // did we manage to recover a user by email?
        if (user == null) {
            getLog().debug("No known user, creating a new one...");
            String apiKey = generateApiKey(email, fullName);
            user = new ZoomaUserImpl(firstName, surname, fullName, email, apiKey, DEFAULT_AUTHORITIES);
            getLog().debug("Created new user:\nDetails:\t" + user.toString() + "\nAPI Key:\t" + apiKey);
            getKeyStore().storeUserDetails(user);
        }
        return user;
    }

    private String generateApiKey(String email, String fullName) {
        return ZoomaUtils.generateHashEncodedID(email,
                                                fullName,
                                                new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(new Date()));
    }
}
