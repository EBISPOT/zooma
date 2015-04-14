package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A convenience class for creating a HSQL database to store user details in, if it doesn't already exist.
 *
 * @author Tony Burdett
 * @date 03/03/14
 */
public class UserDatabaseInitializer {
    // select u.ID, u.EMAIL, u.FIRSTNAME, u.SURNAME, u.FULLNAME, u.APIKEY from USERS u
    // select r.ID, r.DATASOURCE, r.ROLE from ROLES r
    public static final String CREATE_USERS_SEQUENCE =
            "create sequence if not exists ZOOMA_SEQ_USERS start with 1 increment by 1";
    public static final String CREATE_ROLES_SEQUENCE =
            "create sequence if not exists ZOOMA_SEQ_ROLES start with 1 increment by 1";
    public static final String CREATE_USERS =
            "create table if not exists USERS (" +
                    "ID INTEGER NOT NULL, " +
                    "EMAIL VARCHAR (100), " +
                    "FIRSTNAME VARCHAR (100), " +
                    "SURNAME VARCHAR (100), " +
                    "FULLNAME VARCHAR (100), " +
                    "APIKEY VARCHAR (150), " +
                    "constraint ZOOMA_USERS_PK primary key (ID) )";
    public static final String CREATE_ROLES =
            "create table if not exists ROLES (" +
                    "ID INTEGER NOT NULL, " +
                    "DATASOURCE VARCHAR (100), " +
                    "ROLE VARCHAR (100), " +
                    "constraint ZOOMA_ROLES_PK primary key (ID) )";
    public static final String CREATE_USERS_ROLES =
            "create table if not exists USERS_ROLES (" +
                    "USER_ID INTEGER NOT NULL, " +
                    "ROLE_ID INTEGER NOT NULL, " +
                    "constraint ZOOMA_USERS_ROLES_USER_FK foreign key (USER_ID) references USERS (ID), " +
                    "constraint ZOOMA_USERS_ROLES_ROLE_FK foreign key (ROLE_ID) references ROLES (ID) )";

    private String driverClassName;
    private String url;
    private String username;
    private String password;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void init() {
        // create study table in the HSQL database if they do not already exist
        try {
            // load driver
            Class.forName(getDriverClassName());

            // get connection and create tables
            getLog().debug("Initializing database...");
            Connection connection = DriverManager.getConnection(getUrl() + ";hsqldb.write_delay=false");
            // create sequence
            getLog().debug("Creating sequences...");
            try {
                connection.prepareStatement(CREATE_USERS_SEQUENCE).execute();
                connection.prepareStatement(CREATE_ROLES_SEQUENCE).execute();
            }
            catch (SQLException e) {
                // sequence already exists
                getLog().debug("Skipping sequence creation - failed to create a sequence (" + e.getMessage() + ")", e);
            }
            // create user table
            getLog().debug("Creating tables...");
            connection.prepareStatement(CREATE_USERS).execute();
            // create role table
            connection.prepareStatement(CREATE_ROLES).execute();
            // create join tbale
            connection.prepareStatement(CREATE_USERS_ROLES).execute();
            getLog().debug("...database initialization complete");
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load HSQL driver", e);
        }
        catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Unable to connect to " + getUrl(), e);
        }
    }

    public void destroy() {
        // send shutdown signal to HSQL database
        try {
            Connection connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
            connection.prepareStatement("SHUTDOWN;").execute();
        }
        catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Unable to connect to " + getUrl(), e);
        }
    }
}
