package uk.ac.ebi.pride.utilities.ols.web.service.config;

import uk.ac.ebi.pride.utilities.ols.web.service.utils.Constants;

/**
 * This class help to configure the web-service provider that would be used.
 */
public class OLSWsConfigProd extends AbstractOLSWsConfig {

    public OLSWsConfigProd() {
        super(Constants.OLS_PROTOCOL, Constants.OLS_SERVER);
    }
}