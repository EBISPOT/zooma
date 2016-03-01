package uk.ac.pride.ols.web.service.config;

/**
 * This class help to configure the web-service provider that would be used.
 */
public class OLSWsConfigProd extends AbstractOLSWsConfig {

    public OLSWsConfigProd() {
        super("http", "www.ebi.ac.uk/ols/beta/");
    }
}
