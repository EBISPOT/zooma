package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import uk.ac.ebi.fgpt.zooma.exception.UnrecognisedUserOrTypeException;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUser;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUserImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A JDBC-based service for storing and retrieving ZOOMA user details in an SQL database.
 *
 * @author Tony Burdett
 * @date 28/02/14
 */
public class JDBCApiKeyStore implements ApiKeyStore {
    public static final String USERS_SEQUENCE_SELECT =
            "call next value for ZOOMA_SEQ_USERS";
    public static final String ROLES_SEQUENCE_SELECT =
            "call next value for ZOOMA_SEQ_ROLES";

    // select u.ID, u.EMAIL, u.FIRSTNAME, u.SURNAME, u.FULLNAME, u.APIKEY from USERS u
    // select r.ID, r.DATASOURCE, r.ROLE from ROLES r
    public static final String USER_SELECT =
            "select u.ID, u.EMAIL, u.FIRSTNAME, u.SURNAME, u.FULLNAME, u.APIKEY from USERS u ";
    public static final String USER_ID_SELECT_BY_EMAIL =
            "select u.ID from USERS u where u.EMAIL = ?";
    public static final String USER_SELECT_BY_EMAIL = USER_SELECT +
            "where u.email = ?";
    public static final String USER_SELECT_BY_API_KEY = USER_SELECT +
            "where u.APIKEY = ?";

    public static final String USER_INSERT =
            "insert into USERS (ID, EMAIL, FIRSTNAME, SURNAME, FULLNAME, APIKEY) " +
                    "values (?, ?, ?, ?, ?, ?)";
    public static final String USER_UPDATE =
            "update USERS " +
                    "set EMAIL = ?, FIRSTNAME = ?, SURNAME = ?, FULLNAME = ?, APIKEY = ? " +
                    "where ID = ?";

    public static final String ROLE_SELECT =
            "select r.ID, r.DATASOURCE, r.ROLE from ROLES r ";
    public static final String ROLE_ID_SELECT_BY_DATASOURCE_AND_ROLE =
            "select r.ID from ROLES r " +
                    "where r.DATASOURCE = ? " +
                    "and r.ROLE = ? ";
    public static final String ROLE_SELECT_BY_USER = ROLE_SELECT +
            "join USERS_ROLES ur on r.ID = ur.ROLE_ID " +
            "join USERS u on ur.USER_ID = u.ID " +
            "where u.ID = ?";

    public static final String ROLE_INSERT =
            "insert into ROLES (ID, DATASOURCE, ROLE) " +
                    "values (?, ?, ?)";

    public static final String USER_ROLE_COUNT =
            "select count(*) from USERS_ROLES where USER_ID = ? and ROLE_ID = ?";
    public static final String USER_ROLE_INSERT =
            "insert into USERS_ROLES (USER_ID, ROLE_ID) values (?, ?)";

    private JdbcTemplate jdbcTemplate;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails getDetailsByUsername(String username) {
        // username in zooma is just the users email address
        try {
            ZoomaUser user = getJdbcTemplate().queryForObject(USER_SELECT_BY_EMAIL,
                                                              new Object[]{username},
                                                              new UserMapper());
            if (user instanceof UserDetails) {
                return (UserDetails) user;
            }
            else {
                throw new UnrecognisedUserOrTypeException(
                        "Found a user, but it is of an unsupported type (" + user.getClass().getName() + ")");
            }
        }
        catch (IncorrectResultSizeDataAccessException e) {
            getLog().debug("No unique user for email '" + username + "' (" + e.getMessage() + "); returning null user");
            return null;
        }
    }

    @Override
    public UserDetails getDetailsByApiKey(String apiKey) {
        try {
            ZoomaUser user = getJdbcTemplate().queryForObject(USER_SELECT_BY_API_KEY,
                                                              new Object[]{apiKey},
                                                              new UserMapper());
            if (user instanceof UserDetails) {
                return (UserDetails) user;
            }
            else {
                throw new UnrecognisedUserOrTypeException(
                        "Found a user, but it is of an unsupported type (" + user.getClass().getName() + ")");
            }
        }
        catch (IncorrectResultSizeDataAccessException e) {
            getLog().debug("No unique user for apiKey '" + apiKey + "' (" + e.getMessage() + "); returning null user");
            return null;
        }
    }

    @Override
    public void storeUserDetails(UserDetails user) {
        ZoomaUser zoomaUser = null;
        if (user instanceof ZoomaUser) {
            zoomaUser = (ZoomaUser) user;
        }
        else {
            throw new UnrecognisedUserOrTypeException("Authenticated user (details: " + user.toString() + ") " +
                                                            "is of an unrecognised type " +
                                                            "(" + user.getClass().getName() + ")");
        }

        // store roles
        Set<Integer> roleIDs = new HashSet<>();
        for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
            roleIDs.add(getOrCreateRole(grantedAuthority));
        }

        // store user
        int userID;
        try {
            userID = getJdbcTemplate().queryForObject(USER_ID_SELECT_BY_EMAIL,
                                                      Integer.class,
                                                      zoomaUser.getEmail());
            // user exists, so
            // update USER set EMAIL = ?, FIRSTNAME = ?, SURNAME = ?, FULLNAME = ?, APIKEY = ?
            getJdbcTemplate().update(USER_UPDATE,
                                     zoomaUser.getEmail(),
                                     zoomaUser.getFirstname(),
                                     zoomaUser.getSurname(),
                                     zoomaUser.getFullName(),
                                     zoomaUser.getApiKey(),
                                     userID);
        }
        catch (IncorrectResultSizeDataAccessException e) {
            // there is no such user in database, so
            // insert into USERS (ID, EMAIL, FIRSTNAME, SURNAME, FULLNAME, APIKEY)
            userID = getJdbcTemplate().queryForObject(USERS_SEQUENCE_SELECT, Integer.class);
            getJdbcTemplate().update(USER_INSERT,
                                     userID,
                                     zoomaUser.getEmail(),
                                     zoomaUser.getFirstname(),
                                     zoomaUser.getSurname(),
                                     zoomaUser.getFullName(),
                                     zoomaUser.getApiKey());
        }

        // finally, link user id to each role id
        for (Integer roleID : roleIDs) {
            linkUserRole(userID, roleID);
        }
    }

    private int getOrCreateRole(GrantedAuthority grantedAuthority) {
        String authority = grantedAuthority.getAuthority();
        String datasource, role;
        String[] parts = authority.split("_");
        if (parts.length == 2) {
            datasource = "ALL";
            role = parts[1];
        }
        else if (parts.length == 3) {
            datasource = parts[1];
            role = parts[2];
        }
        else {
            throw new DataIntegrityViolationException(
                    "Failed to store role - authority string could not be parsed (" + authority + ")");
        }

        int roleID;
        try {
            roleID = getJdbcTemplate().queryForObject(ROLE_ID_SELECT_BY_DATASOURCE_AND_ROLE,
                                                      Integer.class,
                                                      datasource,
                                                      role);
        }
        catch (IncorrectResultSizeDataAccessException e) {
            // there is no such role in database, so create
            // insert into USERS (ID, EMAIL, FIRSTNAME, SURNAME, FULLNAME, APIKEY)
            roleID = getJdbcTemplate().queryForObject(ROLES_SEQUENCE_SELECT, Integer.class);
            getJdbcTemplate().update(ROLE_INSERT,
                                     roleID,
                                     datasource,
                                     role);
        }
        return roleID;
    }

    private void linkUserRole(int userID, int roleID) {
        if (getJdbcTemplate().queryForObject(USER_ROLE_COUNT, Integer.class, userID, roleID) == 0) {
            getJdbcTemplate().update(USER_ROLE_INSERT, userID, roleID);
        }
    }

    /**
     * Maps database rows to ZoomaUser objects
     */
    private class UserMapper implements RowMapper<ZoomaUser> {
        @Override
        public ZoomaUser mapRow(ResultSet resultSet, int i) throws
                SQLException {
            // select u.ID, u.EMAIL, u.FIRSTNAME, u.SURNAME, u.FULLNAME, u.APIKEY from USERS u
            int id = resultSet.getInt(1);
            String email = resultSet.getString(2);
            String firstname = resultSet.getString(3);
            String surname = resultSet.getString(4);
            String fullname = resultSet.getString(5);
            String apikey = resultSet.getString(6);

            Collection<GrantedAuthority> authorities =
                    getJdbcTemplate().query(ROLE_SELECT_BY_USER, new RoleMapper(), id);
            return new ZoomaUserImpl(firstname, surname, fullname, email, apikey, authorities);
        }
    }

    private class RoleMapper implements ResultSetExtractor<Collection<GrantedAuthority>> {
        @Override public Collection<GrantedAuthority> extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            Set<String> roles = new HashSet<>();
            while (resultSet.next()) {
                // select r.ID, r.DATASOURCE, r.ROLE from ROLES r
                String datasource = resultSet.getString(2);
                String role = resultSet.getString(3);
                roles.add(datasource.equals("ALL") ? "ROLE_" + role : "ROLE_" + datasource + "_" + role);
            }
            return AuthorityUtils.createAuthorityList(roles.toArray(new String[roles.size()]));
        }
    }
}
