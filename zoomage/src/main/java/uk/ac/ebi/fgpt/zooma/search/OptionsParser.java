package uk.ac.ebi.fgpt.zooma.search;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

/**
 * User: jmcmurry
 * Date: 26/03/2014
 * Utility to parse commandline and properties files with a minimum of ceremony.
 */
public class OptionsParser {

    private static final Logger log = LoggerFactory.getLogger(OptionsParser.class);

    private CommandLineParser parser = new GnuParser();

    private Properties defaults; // configs from properties file
    private String defaultsPrefix = "";    // name of the project (used for logging messages but also the prefix used in the properties file)
    private Options options;
    private HashSet<String> usedPrefixes = new HashSet<>();
    private HashMap<String, String> provisionalCommandLineArgsMap; // provisional map of commandline arguments. May not be as fault-tolerant as the GnuParser
    private int statusCode = 0;
    private HashMap<String, EnhancedOption> enhancedOptions = new HashMap<String, EnhancedOption>();


    public OptionsParser(String[] commandLineArgs, String propertiesFilePath, String propertiesFilePrefix) {
        options = new Options();
        Option helpOption = new Option("h", "help", false, "Print the help");
        options.addOption(helpOption);

        provisionalCommandLineArgsMap = loadCommandLineArgs(commandLineArgs);
        defaults = loadDefaultsFromPropertiesFile(propertiesFilePath, propertiesFilePrefix);
    }


    // private method that handles delegated processing of each option
    private String processOption(String optLongName, boolean optIsRequiredInCommandline, boolean optIsRequired, String optShortName, String optType, String optHelpMsg) {

        String stringValue = null;

        String commandLineArg = parseCommandLineArgs(optShortName);
        String defaultArg = getProperty(optLongName);

        // check command line args that may override defaults.
        if (commandLineArg != null) {
            stringValue = commandLineArg;
        }
        // check defaults
        else if (defaultArg != null) {
            stringValue = defaultArg;
        } else
            getLog().info("No default or commandline argument could be processed for variable: " + "-" + optShortName + " " + optLongName + ": " + optHelpMsg);

        // if the option is required and if neither commandline nor default is specified, log the error.
        if (stringValue == null && optIsRequired) {
            getLog().error("variable: " + "-" + optShortName + " " + optLongName + " is required. ");
            statusCode +=7;
        }

        new EnhancedOption(optLongName, optType, optIsRequiredInCommandline, optIsRequired, optShortName, optHelpMsg, stringValue);

        return stringValue;

    }

    /**
     * Creates an option and returns a provisional boolean value
     * @param optLongName name of the option eg "accession"
     * @param optIsRequiredInCommandline requires that a value be specified in the commandline when run
     * @param optIsRequired requires that a value be specified, whether via commandline or properties file
     * @param optShortName single letter handle used when specifying args from commandline eg. -a
     * @param optHelpMsg message to print to the user as part of help documentation and logging
     * @return provisional boolean value. Finalise the OptionsParser object in order to safely proceed using this value.
     */

    public String processStringOption(String optLongName, boolean optIsRequiredInCommandline, boolean optIsRequired, String optShortName, String optHelpMsg) {

        String stringVal = processOption(optLongName, optIsRequiredInCommandline, optIsRequired, optShortName, "String", optHelpMsg);

        return stringVal;
    }


    /**
     * Creates an option and returns a provisional Integer value
     * @param optLongName name of the option eg "accession"
     * @param optIsRequiredInCommandline requires that a value be specified in the commandline when run
     * @param optIsRequired requires that a value be specified, whether via commandline or properties file
     * @param optShortName single letter handle used when specifying args from commandline eg. -a accession
     * @param optHelpMsg message to print to the user as part of help documentation and logging
     * @return provisional Integer value. Finalise the OptionsParser object in order to safely proceed using this value.
     */
    public Integer processIntOption(String optLongName, boolean optIsRequiredInCommandline, boolean optIsRequired, String optShortName, String optHelpMsg) {

        String stringVal = processOption(optLongName, optIsRequiredInCommandline, optIsRequired, optShortName, "int", optHelpMsg);

        return Integer.parseInt(stringVal);
    }

    /**
     * Creates an option and returns a provisional Float value
     * @param optLongName name of the option eg "accession"
     * @param optIsRequiredInCommandline requires that a value be specified in the commandline when run
     * @param optIsRequired requires that a value be specified, whether via commandline or properties file
     * @param optShortName single letter handle used when specifying args from commandline eg. -a accession
     * @param optHelpMsg message to print to the user as part of help documentation and logging
     * @return provisional Float value. Will return null if String argument is null.
     * Finalise the OptionsParser object in order to safely proceed using this value.
     */
    public Float processFloatOption(String optLongName, boolean optIsRequiredInCommandline, boolean optIsRequired, String optShortName, String optHelpMsg) {

        String stringVal = processOption(optLongName, optIsRequiredInCommandline, optIsRequired, optShortName, "float", optHelpMsg);
        if (stringVal == null) return null;
        return Float.parseFloat(stringVal);
    }

    /**
     * Creates an option and returns a provisional boolean value
     * @param optLongName name of the option
     * @param optIsRequiredInCommandline requires that a value be specified in the commandline when run
     * @param optIsRequired requires that a value be specified, whether via commandline or properties file
     * @param optShortName single letter handle used when specifying args from commandline eg. -a accession
     * @param optHelpMsg message to print to the user as part of help documentation and logging
     * @return provisional boolean value based on whether the string argument starts with t or y (case insensitive).
     * Finalise the OptionsParser object in order to safely proceed using this value.
     */
    public boolean processBooleanOption(String optLongName, boolean optIsRequiredInCommandline, boolean optIsRequired, String optShortName, String optHelpMsg) {

        String stringVal = processOption(optLongName, optIsRequiredInCommandline, optIsRequired, optShortName, "boolean", optHelpMsg);

        return stringVal.startsWith("t") || stringVal.startsWith("y") ||
                stringVal.startsWith("T") || stringVal.startsWith("Y");
    }


    // loads defaults from properties file
    private Properties loadDefaultsFromPropertiesFile(String propertiesFilePath, String defaultsPrefix) {
        Properties defaults = new Properties();
        this.defaultsPrefix = defaultsPrefix;

        URI uri = null;
        URL url = null;
        InputStream in = null;

        // all this extra ceremony is in order to be able to override the properties file with an external one somewhere on the file system
        try {
            // try loading the path of the configFile assuming it is local to the package
            url = OptionsParser.class.getClassLoader().getResource(propertiesFilePath);
            // if this is local approach successful, cast to URI,
            if (url != null) {
                uri = url.toURI();
            }
            // if the local approach is unsuccessful, load assuming an absolute path
            else uri = new File(propertiesFilePath).toURI();

            // switch back to URL version of it in order to open an input stream.
            url = uri.toURL();

            // open the input stream
            if (url != null) {
                in = url.openStream();
            }

            if (in == null) {
                throw new IOException("Failed to read default options from the path provided " + propertiesFilePath);
            } else defaults.load(in);

        } catch (URISyntaxException e) {
            e.printStackTrace();  //todo:
        } catch (MalformedURLException e) {
            e.printStackTrace();  //todo:
        } catch (IOException e) {
            e.printStackTrace();  //todo:
        }

        getLog().info("Loaded properties file from '" + uri + "'.");

        return defaults;
    }


    // stores commandline args in a map where the key is the letter to which the argument is bound and the value is the argument string
    private static HashMap<String, String> loadCommandLineArgs(String[] args) {

        HashMap<String, String> argsMap = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-") && args[i].length() == 2) {
                String argShortName = args[i].replaceAll("-", "");
                if (args.length > i) {
                    String argVal = args[i + 1];
                    if (argVal.startsWith("-")) argsMap.put(argShortName, "");
                    else argsMap.put(argShortName, argVal);
                }
            }
        }

        return argsMap;
    }

    // cleans up the input in order to properly fetch default values.
    private String getProperty(String optLongName) {

        String propertyName = "";
        if (defaultsPrefix == null || defaultsPrefix.equals("")) propertyName = optLongName;
        else propertyName = (defaultsPrefix + "." + optLongName).replace("..", ".");

        String defaultStringVal = defaults.getProperty(propertyName);

        return defaultStringVal;
    }

    private String parseCommandLineArgs(String optShortName) {
        if (provisionalCommandLineArgsMap.containsKey(optShortName))
            return provisionalCommandLineArgsMap.get(optShortName);
        return null;
    }


    public static Logger getLog() {
        return log;
    }

    /**
     * should be called after all options have been added
     *
     * @param noArgsMessage   message to print in case no commandline arguments are provided
     * @param commandLineArgs string array of arguments provided by the user.
     */
    public void finalise(String noArgsMessage, String[] commandLineArgs) {
        HelpFormatter help = new HelpFormatter();

        try {

            CommandLine commandlineParser = parser.parse(options, commandLineArgs, true);


            // check for mode help option
            if (commandlineParser.hasOption("") || commandlineParser.hasOption("h")) {
                // print out mode help
                help.printHelp(defaultsPrefix, options, true);
                statusCode += 1;
            } else {

                if (commandlineParser.getOptions().length == 0) {
                    System.out.println(noArgsMessage);
                    help.printHelp(defaultsPrefix, options, true);
                }

                System.out.println(defaultsPrefix + " will be run with the following supplied options...");

                // print the supplied options
                for (Option opt : commandlineParser.getOptions()) {
                    System.out.println("\t" + opt.getLongOpt() +
                            (opt.hasArg() ? ": " + opt.getValue() : "") +
                            " (" + opt.getArgName() + ")");
                }

                // check the values previously assigned and make sure that they are not somehow different
                for (EnhancedOption enhancedOption : enhancedOptions.values()) {
                    // what base path to use for magetab files.
                    String optionPrefix = enhancedOption.optShortName;
                    if (commandlineParser.hasOption(optionPrefix)) {
                        enhancedOption.finalisedStringValue = commandlineParser.getOptionValue(optionPrefix);
                    } else {
                        enhancedOption.finalisedStringValue = defaults.getProperty(defaultsPrefix + "." + enhancedOption.optLongName);
                        getLog().info("Using default property for '" + enhancedOption.optLongName + "': " + enhancedOption.preliminaryStringValue);
                    }

                    if ((enhancedOption.preliminaryStringValue != null || enhancedOption.finalisedStringValue != null) && !enhancedOption.preliminaryStringValue.equals(enhancedOption.finalisedStringValue)) {
                        String errormessage = "Preliminary and finalised string values for option " + enhancedOption.optLongName + " do not match.";
                        errormessage += "Preliminary value: '" + enhancedOption.preliminaryStringValue + "' finalised value: '" + enhancedOption.finalisedStringValue;
                        throw new ParseException(errormessage);
                    }
                }

            }
        } catch (ParseException e) {
            System.out.println("Failed to read supplied arguments (" + e.getMessage() + ")");
            help.printHelp(defaultsPrefix, options, false);
            statusCode += 4;
        }

    }


    /**
     * User: jmcmurry
     * Date: 13/02/2014
     * Inner class to wrap the Apache Option because you can't fetch an option except by short ID and setting the values is a bit weird and deprecated.
     */
    public class EnhancedOption {

        protected final String optLongName;
        protected final String optShortName;
        protected String preliminaryStringValue;
        protected String finalisedStringValue;
        protected final boolean optionIsRequired;
        protected final Option option;

        /**
         * Creates an option and returns a provisional boolean value
         * @param optLongName name of the option
         * @param optIsRequiredInCommandline requires that a value be specified in the commandline when run
         * @param optionIsRequired requires that a value be specified, whether via commandline or properties file
         * @param optShortName single letter handle used when specifying args from commandline eg. -a accession
         * @param optHelpMsg message to print to the user as part of help documentation and logging
         * @return provisional boolean value based on whether the string argument starts with t or y (case insensitive).
         * Finalise the OptionsParser object in order to safely proceed using this value.
         */
        public EnhancedOption(String optLongName, String optType, boolean optIsRequiredInCommandline, boolean optionIsRequired, String optShortName, String optHelpMsg, String preliminaryStringValue) {

            if (usedPrefixes.contains(optShortName))
                throw new IllegalArgumentException("Prefix " + optShortName + " has already been assigned.");

            this.optLongName = optLongName;
            this.optShortName = optShortName;
            this.preliminaryStringValue = preliminaryStringValue;
            this.optionIsRequired = optionIsRequired;

            Option option = new Option(
                    optShortName,
                    optLongName,
                    true,
                    optHelpMsg);
            option.setArgName(optType);
            option.setRequired(optIsRequiredInCommandline);
            options.addOption(option);

            this.option = option;
            enhancedOptions.put(optLongName, this);
            usedPrefixes.add(optShortName);
        }
    }


    public int getStatusCode() {
        return statusCode;
    }


}
