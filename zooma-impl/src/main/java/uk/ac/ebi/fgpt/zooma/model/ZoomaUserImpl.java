package uk.ac.ebi.fgpt.zooma.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A default implementation of both a {@link uk.ac.ebi.fgpt.zooma.model.ZoomaUser} and {@link
 * org.springframework.security.core.userdetails.UserDetails} for performing secure user authentication (using spring
 * security) and providing the information about a user required by ZOOMA
 *
 * @author Tony Burdett
 * @date 28/01/14
 */
public class ZoomaUserImpl extends User implements ZoomaUser {
    private final String firstName;
    private final String surname;
    private final String fullName;

    private final String email;
    private final String apiKey;

    public ZoomaUserImpl(String firstName,
                         String surname,
                         String fullName,
                         String email,
                         String apiKey,
                         Collection<? extends GrantedAuthority> authorities) {
        super(email, "N/A", authorities);
        this.firstName = firstName;
        this.surname = surname;
        this.fullName = fullName;
        this.email = email;
        this.apiKey = apiKey;
    }

    @Override public String getFirstname() {
        return firstName;
    }

    @Override public String getSurname() {
        return surname;
    }

    @Override public String getFullName() {
        return fullName;
    }

    @Override public String getEmail() {
        return email;
    }

    @Override public String getApiKey() {
        return apiKey;
    }

    @Override public Collection<String> getRoles() {
        Set<String> roles = new HashSet<>();
        for (GrantedAuthority authority : getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        return roles;
    }

    @Override
    public String toString() {
        return super.toString() + " {fullname='" + fullName + "'; email='" + email + "'; apiKey='" + apiKey + "'}";
    }

}
