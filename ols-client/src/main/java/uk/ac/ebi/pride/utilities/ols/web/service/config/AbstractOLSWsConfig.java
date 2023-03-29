package uk.ac.ebi.pride.utilities.ols.web.service.config;


/**
 * @author ypriverol
 *
 */
public abstract class AbstractOLSWsConfig {

    private String hostName;
    private String protocol;

    public AbstractOLSWsConfig(String protocol, String hostName) {
        this.hostName = hostName;
        this.protocol = protocol;
    }

    public String getHostName() {
        return hostName;
    }


    public String getProtocol() {
        return protocol;
    }

}
