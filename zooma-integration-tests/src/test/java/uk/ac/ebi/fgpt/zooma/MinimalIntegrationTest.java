package uk.ac.ebi.fgpt.zooma;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.env.ZoomaEnv;
import uk.ac.ebi.fgpt.zooma.env.ZoomaHome;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled
public class MinimalIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(MinimalIntegrationTest.class);

    private static final File currentDirectory = new File("");
    private static final File logFile = new File(currentDirectory.getAbsolutePath() + "/log.txt");
    private static File temporaryZoomaHomeDirectory;
    private static File zoomaConfigDirectory;
    private static File zoomaLoadersDirectory;
    private static String startVirtuosoCommand;
    private static String stopVirtuosoCommand;


    @BeforeAll
    static void initAll() {
        try {
            setupEnvironment();
        } catch (Exception e) {
            fail(e.getMessage(), e);
        }
        ZoomaEnv.configureZOOMAEnvironment();
        ZoomaHome.checkInstall();


    }

    private static void setupEnvironment() throws IOException, InterruptedException {
        String temporaryZoomaHome = currentDirectory.getAbsolutePath() + "/tmp-zooma";
        logger.trace("temporaryZoomaHome = " + temporaryZoomaHome);
        setupZoomaHomeDirectory(temporaryZoomaHome);

        copyTestConfiguration(currentDirectory.getAbsolutePath() + "/src/test/resources");
        setCommandLineFiles();
        executeCommand(startVirtuosoCommand);
    }

    private static void setupZoomaHomeDirectory(String temporaryZoomaHome) throws IOException {
        System.setProperty("zooma.home", temporaryZoomaHome);
        temporaryZoomaHomeDirectory = new File(temporaryZoomaHome);
        FileUtils.forceMkdir(temporaryZoomaHomeDirectory);

        zoomaConfigDirectory = new File(temporaryZoomaHome + "/config");
        zoomaLoadersDirectory = new File(temporaryZoomaHome + "/loaders");
        FileUtils.forceMkdir(zoomaConfigDirectory);
        FileUtils.forceMkdir(zoomaLoadersDirectory);
    }

    private static void copyTestConfiguration(String testResourcePath) throws IOException {
        File testZoomaPropertiesFile = new File(testResourcePath + "/minimal-zooma.properties");
        File zoomaPropertiesFile = new File (zoomaConfigDirectory.getAbsolutePath() + "/zooma.properties");
        FileUtils.copyFile(testZoomaPropertiesFile, zoomaPropertiesFile);

        File testDoaAnnotationsFile = new File(testResourcePath + "/minimal-zooma-annotation-dao.xml");
        File testZoomaDoaAnnotationsFile = new File(zoomaLoadersDirectory.getAbsolutePath() + "/zooma-annotation-dao.xml");
        FileUtils.copyFile(testDoaAnnotationsFile, testZoomaDoaAnnotationsFile);
    }


    private static void setCommandLineFiles() {
        logger.trace("currentDirectory = " + currentDirectory.getAbsolutePath());
        Path currentPath = Paths.get(currentDirectory.getAbsolutePath());
        logger.trace("currentPath = " + currentPath);
        logger.trace("currentPath.getParent = " + currentPath.getParent().toString());
        String zoomaSourcePath = currentPath.getParent().toString();
        logger.trace("zoomaSourceDirectory = " + zoomaSourcePath);
        startVirtuosoCommand = zoomaSourcePath + "/zooma-builder-app/src/main/bin/virtuoso-start.sh";
        stopVirtuosoCommand = zoomaSourcePath + "/zooma-builder-app/src/main/bin/virtuoso-stop.sh";
    }

    private static void executeCommand(String commandToExecute) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(commandToExecute);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
        Process process = processBuilder.start();
        process.getOutputStream().flush();
        int exitCode = process.waitFor();
        process.getOutputStream().close();
        assertEquals(0, exitCode, "No errors should be detected");
    }


    @Test
    void succeedingTest() {

    }



//    @AfterAll
//    static void tearDownAll() {
//        File temporaryZoomaHomeDirectory = new File(temporaryZoomaHome);
//        try {
//            FileUtils.deleteDirectory(temporaryZoomaHomeDirectory);
//        } catch (IOException ioe) {
//            Assertions.fail(ioe.getMessage(), ioe);
//        }
//    }
}
