package uk.ac.ebi.spot.zooma.model;

import java.util.Collection;

/**
 * A known user of the ZOOMA system.  ZOOMA users should always be available for requests generated from a server
 * session in a web application, but if you are using ZOOMA as a programmatic library you will have to authenticate
 * against an account before using any protected functions.
 * <p/>
 * ZOOMA users contain some lightweight details, including the users first and surname, email address and their API key
 * (for generating requests that require authentication).
 *
 * @author Tony Burdett
 * @date 28/01/14
 */
public interface ZoomaUser {
    String getUsername();

    String getEmail();

    String getFirstname();

    String getSurname();

    String getFullName();

    String getApiKey();

    Collection<String> getRoles();
}

