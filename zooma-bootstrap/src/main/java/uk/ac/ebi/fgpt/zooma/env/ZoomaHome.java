package uk.ac.ebi.fgpt.zooma.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A class to help with Zooma zero-config installation.  This class looks for the existence of a ZOOMA_HOME directory,
 * and if absent can unpack a template configuration into it to minimize the setup required to get Zooma running.
 *
 * @author Tony Burdett
 * @date 06/10/14
 */
public class ZoomaHome {
    private static final ZoomaHome instance = new ZoomaHome();

    public static void checkInstall() {
        instance.validateZoomaHome();
    }

    private ZoomaHome() {

    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Logger getLog() {
        return log;
    }

    /**
     * Checks for the existence of the required configuration files in the specified ZOOMA home directory (as specified
     * by the system property 'zooma.home').  If this directory does not exist or is empty, this method will create a
     * new directory and unpack a default template configuration into ZOOMA home directory to ensure there is a zero
     * config installation process.
     * <p/>
     * The default configuration makes certain assumptions - see the documents associated with the zooma-configurations
     * module for more information.
     */
    private synchronized boolean validateZoomaHome() {
        File zoomaHome = new File(System.getProperty("zooma.home"));
        if (zoomaHome.exists() && zoomaHome.list().length > 0) {
            getLog().info("ZOOMA_HOME already exists at " + zoomaHome.getAbsolutePath());

            if (!zoomaHome.isDirectory()) {
                getLog().error("ZOOMA_HOME is not a directory; cannot validate the contents");
                return false;
            }

            // zooma home already exists - does it have expected layout?
            // check existence of subdirectories
            final String[] requiredFiles = {"config", "loaders"};

            String[] foundRequiredFiles = zoomaHome.list(new FilenameFilter() {
                @Override public boolean accept(File dir, String name) {
                    for (String req : requiredFiles) {
                        if (name.equals(req)) {
                            return true;
                        }
                    }
                    return false;
                }
            });

            String[] otherFiles = zoomaHome.list(new FilenameFilter() {
                @Override public boolean accept(File dir, String name) {
                    for (String req : requiredFiles) {
                        if (!name.equals(req)) {
                            return false;
                        }
                    }
                    return true;
                }
            });

            if (otherFiles.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Unrecognised files found in ZOOMA_HOME - your configuration may not be correct. ");
                sb.append("Unrecognised files are:\n");
                for (String of : otherFiles) {
                    sb.append("\t").append(of).append("\n");
                }
                getLog().warn(sb.toString());
            }

            if (foundRequiredFiles.length == requiredFiles.length) {
                getLog().debug("Layout of existing ZOOMA_HOME looks correct");
                return true;
            }
            else {
                StringBuilder sb = new StringBuilder();
                sb.append("Missing required configuration - ");
                sb.append(zoomaHome.getAbsolutePath()).append(" must include ");
                for (int i = 0; i < requiredFiles.length; i++) {
                    sb.append(requiredFiles[i]);
                    if (i < requiredFiles.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(" but only ");
                for (int i = 0; i < foundRequiredFiles.length; i++) {
                    sb.append(foundRequiredFiles[i]);
                    if (i < foundRequiredFiles.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(" were found");
                getLog().error(sb.toString());
                return false;
            }
        }
        else {
            if (!zoomaHome.exists()) {
                // Directory doesn't exist, as opposed to simply being empty
                getLog().info("No ZOOMA_HOME exists at " + zoomaHome.getAbsolutePath());
            }
            else {
                getLog().info("ZOOMA_HOME exists at " + zoomaHome.getAbsolutePath() + " but no files were found " +
                                      "in this directory");
            }
            unpackTemplate();
            return true;
        }
    }

    private synchronized void unpackTemplate() {
        File zoomaHome = new File(System.getProperty("zooma.home"));

        // no zooma home - unpack template from classpath resource
        String zipFileName = "zooma-home-template.zip";
        URL zipFileResource = getClass().getClassLoader().getResource(zipFileName);
        byte[] buffer = new byte[1_000_000];
        int bytes;

        try {
            if (zipFileResource != null) {
                getLog().info("Unpacking template configuration from '" + zipFileResource.toString() + "' " +
                                      "to '" + zoomaHome.getAbsolutePath() + "'...");
                InputStream in = zipFileResource.openStream();
                ZipInputStream zis = new ZipInputStream(in);
                BufferedInputStream bis = new BufferedInputStream(zis);
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    File extractionFile = new File(zoomaHome, ze.getName());
                    getLog().debug("Inflating " + extractionFile.getAbsolutePath() + "...");
                    if (ze.isDirectory()) {
                        extractionFile.mkdirs();
                        while (bis.read(buffer) != -1) {
                            // read ahead to the end of entry
                        }
                    }
                    else {
                        extractionFile.getParentFile().mkdirs();
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(extractionFile))) {
                            while ((bytes = bis.read(buffer)) != -1) {
                                bos.write(buffer, 0, bytes);
                            }
                        }
                        catch (IOException e) {
                            throw new RuntimeException(
                                    "IO troubles extracting to '" + extractionFile.getAbsolutePath() + "'", e);
                        }
                    }
                }
                getLog().info("New template ZOOMA_HOME installed OK!");
                try {
                    bis.close();
                }
                catch (IOException e) {
                    getLog().warn("Failed to close stream reading from template zip file - " +
                                          "this may cause a memory leak");
                }
            }
            else {
                throw new RuntimeException("Failed to open template zip file '" + zipFileName + "'.  " +
                                                   "This file may not be present on the classpath");
            }
        }
        catch (IOException e) {
            throw new RuntimeException("IO troubles opening zip file '" + zipFileResource + "'", e);
        }
    }
}
