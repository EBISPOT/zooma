package uk.ac.ebi.fgpt.zooma.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * A utility class that will download and extract the latest version of the gzipped OMIA database MySQL dump file from a
 * specified location.
 * <p/>
 * The dump file will be written to the location specified and deleted when this bean is destroyed
 *
 * @author Tony Burdett
 * @date 26/07/13
 */
public class OmiaDownloader {
    private Resource omiaLocation;
    private String downloadTo;

    private DataSource dataSource;

    private Path databaseDumpPath;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Resource getOmiaLocation() {
        return omiaLocation;
    }

    public void setOmiaLocation(Resource omiaLocation) {
        this.omiaLocation = omiaLocation;
    }

    public String getDownloadTo() {
        return downloadTo;
    }

    public void setDownloadTo(String downloadTo) {
        this.downloadTo = downloadTo;
    }

    public DataSource getDatasource() {
        return dataSource;
    }

    public void setDatasource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void init() throws Exception {
        File databaseDump = new File(getDownloadTo());
        File dir = databaseDump.getAbsoluteFile().getParentFile();
        if (!dir.exists()) {
            getLog().debug("Creating directory '" + dir.getAbsolutePath() + "'");
            dir.mkdirs();
        }

        databaseDumpPath = databaseDump.toPath();
        getLog().debug("OMIA database dump will be unzipped and downloaded from " +
                               "'" + getOmiaLocation().toString() + "' to '" + databaseDumpPath + "'");

        // download and unzip from omiaLocation
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");

        String proxySettings = (proxyHost != null)
                ? " (using proxy: " + proxyHost + (proxyPort != null ? ":" + proxyPort : "") + ")"
                : "";
        getLog().info("Downloading OMIA database dump from " + getOmiaLocation().getURL() + proxySettings);
        ReadableByteChannel rbc = Channels.newChannel(new GZIPInputStream(getOmiaLocation().getInputStream()));
        FileOutputStream out = new FileOutputStream(databaseDump);
        out.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        getLog().debug("OMIA database was downloaded successfully");
    }

    public void destroy() throws Exception {
        // delete downloaded files
        getLog().debug("Cleaning up database dump from '" + databaseDumpPath + "'");
        Files.delete(databaseDumpPath);
    }
}
