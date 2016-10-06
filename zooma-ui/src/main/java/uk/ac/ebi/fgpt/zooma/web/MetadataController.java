package uk.ac.ebi.fgpt.zooma.web;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * A simple controller that returns the status of this web application.  It returns details including version number,
 * build number, release date and uptime
 *
 * @author Tony Burdett
 * @date 06/09/12
 */
@Controller
@RequestMapping("/server/metadata")
public class MetadataController implements InitializingBean {
    private @Value("${version}") String version;
//    private @Value("${build.number}") String buildNumber;
    private @Value("${release.date}") String releaseDate;

    private MetadataBean metadataBean;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody MetadataBean getStatus() {
        return metadataBean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.metadataBean = new MetadataBean(version, releaseDate, System.currentTimeMillis());
    }

    /**
     * A simple javabean that encapsulates some status about the statistics of the service
     *
     * @author Tony Burdett
     * @date 06/09/12
     */
    class MetadataBean {
        private final String version;
//        private final String buildNumber;
        private final String releaseDate;
        private final long startupTime;

        public MetadataBean(String version, String releaseDate, long startupTime) {
            this.version = version;
//            this.buildNumber = buildNumber;
            this.releaseDate = releaseDate;
            this.startupTime = startupTime;
        }

        public String getVersion() {
            return version;
        }

//        public String getBuildNumber() {
//            return buildNumber;
//        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public long getStartupTime() {
            return startupTime;
        }
    }
}
