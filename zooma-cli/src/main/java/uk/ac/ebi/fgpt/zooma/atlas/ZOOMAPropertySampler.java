package uk.ac.ebi.fgpt.zooma.atlas;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.io.ZOOMAInputParser;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility class that can randomly sample from a set of properties to return a collection of the required size
 *
 * @author Tony Burdett
 * @date 28/03/13
 */
public class ZOOMAPropertySampler {
    private static File _outputFile;
    private static int _sampleSize;

    public static void main(String[] args) {
        try {
            int statusCode = parseArguments(args);
            if (statusCode == 0) {
                ZOOMAPropertySampler aps = new ZOOMAPropertySampler();
                aps.takeAtlasPropertySample(_outputFile, _sampleSize);
            }
            else {
                System.exit(statusCode);
            }
        }
        catch (IOException e) {
            System.err.println("Failed to write to output file '" + _outputFile.getAbsolutePath() + "' " +
                                       "(" + e.getMessage() + ")");
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println("ZOOMA did not complete successfully: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int parseArguments(String[] args) throws IOException {
        CommandLineParser parser = new GnuParser();
        HelpFormatter help = new HelpFormatter();
        Options options = bindOptions();

        int parseArgs = 0;
        try {
            CommandLine cl = parser.parse(options, args, true);

            // check for mode help option
            if (cl.hasOption("")) {
                // print out mode help
                help.printHelp("zooma", options, true);
                parseArgs += 1;
            }
            else {
                // check -f required option
                if (cl.hasOption("f")) {
                    // get directory argument
                    File f = new File(cl.getOptionValue("f"));
                    if (!f.exists()) {
                        File p = f.getAbsoluteFile().getParentFile();
                        // create if the directory doesn't exist
                        if (!f.getAbsoluteFile().getParentFile().exists()) {
                            System.out.print("Creating output file directory '" + p.getAbsolutePath() + "'...");
                            if (p.mkdirs()) {
                                System.out.println("ok!");
                            }
                            else {
                                System.out.println("failed.");
                            }
                        }
                    }
                    else {
                        throw new IllegalArgumentException(
                                "Output file '" + f.getAbsolutePath() + "' already exists, " +
                                        "please specify a new file to write output to.");
                    }
                    _outputFile = f;
                }

                // optional arguments, if not supplied use defaults
                if (cl.hasOption("s")) {
                    String sizeOption = cl.getOptionValue("s");
                    _sampleSize = Integer.parseInt(sizeOption);
                }
                else {
                    _sampleSize = 1000;
                    System.out.println("Using default sample size, " + _sampleSize);
                }

            }
        }
        catch (ParseException e) {
            System.err.println("Failed to read supplied arguments (" + e.getMessage() + ")");
            help.printHelp("zooma", options, true);
            parseArgs += 1;
        }
        return parseArgs;
    }

    private static Options bindOptions() {
        Options options = new Options();

        // help
        Option helpOption = new Option("h", "help", false, "Print the help");
        options.addOption(helpOption);

        // add file option
        Option fileOption = new Option(
                "f",
                "outfile",
                true,
                "Output File - the file where you wish to write sampled properties");
        fileOption.setRequired(true);
        options.addOption(fileOption);

        // add file option
        Option sizeOption = new Option(
                "s",
                "size",
                true,
                "Sample size - the number of properties you wish to sample");
        sizeOption.setRequired(false);
        options.addOption(sizeOption);

        return options;
    }

    private final String excludedTypesResource = "zooma-exclusions.properties";
    private final Set<String> excludedTypes;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZOOMAPropertySampler() {
        excludedTypes = new HashSet<>();

        // read sources from file
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(excludedTypesResource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && !line.isEmpty()) {
                    String s = line.toLowerCase();
                    if (s != null) {
                        excludedTypes.add(s);
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            getLog().error("Failed to load properties: could not locate file '" + excludedTypesResource + "'.  " +
                                   "No properties will be excluded");
        }
        catch (IOException e) {
            getLog().error("Failed to load properties: could not read file '" + excludedTypesResource + "'.  " +
                                   "No properties will be excluded");
        }
    }

    public void takeAtlasPropertySample(File outputFile, int sampleSize) throws IOException {
        try {
            // initialize atlas dao
            getLog().info("Initializing GXA database connection");
            System.out.print("Connecting to GXA...");
            ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("zooma-atlas-config.xml");
            ZoomaAtlasDAO atlasDAO = ctx.getBean("atlasDAO", ZoomaAtlasDAO.class);
            System.out.println("done.");

            // get unmapped properties
            getLog().info("Reading unmapped properties from GXA");
            System.out.print("Querying GXA for unmapped properties...");
            List<Property> properties = atlasDAO.getUnmappedProperties();
            System.out.println("done.");

            // now screen
            getLog().info("Sampling properties from the Atlas...");
            System.out.print("Filtering and sorting a sample of " + sampleSize + " properties from the Atlas...");
            ZOOMAPropertySampler sampler = new ZOOMAPropertySampler();
            getLog().info("Excluding ineligible properties");
            sampler.excludeIneligibleProperties(properties);

            // take a sample
            getLog().info("Sampling " + sampleSize + " properties");
            List<Property> sampledProperties = sampler.sampleProperties(properties, sampleSize);

            // sort
            getLog().info("Sorting properties alphabetically");
            Collections.sort(sampledProperties, new Comparator<Property>() {
                @Override public int compare(Property p1, Property p2) {
                    if (p1 instanceof TypedProperty && p2 instanceof TypedProperty) {
                        // compare types first
                        int typeCompare = ((TypedProperty) p1).getPropertyType()
                                .compareTo(((TypedProperty) p2).getPropertyType());
                        if (typeCompare == 0) {
                            // compare values if types are equal
                            return p1.getPropertyValue().compareTo(p2.getPropertyValue());
                        }
                        else {
                            return typeCompare;
                        }
                    }
                    else {
                        // if p1 is typed, return this first
                        if (p1 instanceof TypedProperty) {
                            return -1;
                        }
                        else {
                            //if p2 is typed, return this first
                            if (p2 instanceof TypedProperty) {
                                return 1;
                            }
                            else {
                                // neither property is typed, so sort on value
                                return p1.getPropertyValue().compareTo(p2.getPropertyValue());
                            }
                        }
                    }
                }
            });
            System.out.println("done.");

            // and write out
            getLog().info(
                    "Writing " + sampledProperties.size() + " properties to '" + outputFile.getAbsolutePath() + "'");
            System.out.print("Writing " + sampledProperties.size() + " properties to '" + outputFile.getAbsolutePath() +
                                     "'...");
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            for (Property sampledProperty : sampledProperties) {
                writer.print(sampledProperty.getPropertyValue());
                if (sampledProperty instanceof TypedProperty) {
                    writer.println("\t" + ((TypedProperty) sampledProperty).getPropertyType());
                }
                else {
                    writer.println();
                }
            }
            writer.close();
            System.out.println("done.");
        }
        catch (IOException e) {
            getLog().error("IO Exception caused ZOOMA to fail", e);
        }
        catch (Exception e) {
            getLog().error("An unexpected exception caused ZOOMA to fail", e);
        }
    }

    public void takeInputFilePropertySample(File inputFile, File outputFile) throws IOException {
        getLog().info("Reading properties from input file '" + inputFile.getAbsolutePath() + "'");

        // create input parser and parse supplied properties
        System.out.print("Reading properties from input file '" + inputFile.getAbsolutePath() + "'...");
        ZOOMAInputParser parser = new ZOOMAInputParser(inputFile);
        List<Property> properties = parser.parse();
        parser.close();
        System.out.println("done.");

        // now screen
        ZOOMAPropertySampler sampler = new ZOOMAPropertySampler();
        sampler.excludeIneligibleProperties(properties);
        List<Property> sampledProperties = sampler.sampleProperties(properties, 1000);

        // and write out
        getLog().info("Writing " + sampledProperties.size() + " properties to '" + outputFile.getAbsolutePath() + "'");
        System.out.print("Writing " + sampledProperties.size() + " properties to '" + outputFile.getAbsolutePath() +
                                 "'...");
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
        for (Property sampledProperty : sampledProperties) {
            writer.print(sampledProperty.getPropertyValue());
            if (sampledProperty instanceof TypedProperty) {
                writer.println("\t" + ((TypedProperty) sampledProperty).getPropertyType());
            }
            else {
                writer.println();
            }
        }
        writer.close();
        System.out.println("done.");
    }

    public <T extends Collection<Property>> T sampleProperties(T properties, int sampleSize) {
        try {
            Property[] propertyArray = properties.toArray(new Property[properties.size()]);
            T result = (T) properties.getClass().newInstance();
            for (int i = 0; i < sampleSize; i++) {
                // generate a random number, multiply by collection size and sample
                int index = (int) Math.round(Math.random() * properties.size());
                result.add(propertyArray[index]);
            }
            return result;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to sample properties - " + e.getMessage());
        }
    }

    public void removeExcludedTypes(Collection<Property> properties) {
        Iterator<Property> propertyIterator = properties.iterator();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.next();
            if (property instanceof TypedProperty) {
                String type = ((TypedProperty) property).getPropertyType();
                String normalizedType = ZoomaUtils.normalizePropertyTypeString(type);

                // excluded type?
                for (String excludedType : excludedTypes) {
                    if (normalizedType.equals(ZoomaUtils.normalizePropertyTypeString(excludedType))) {
                        propertyIterator.remove();
                        break;
                    }
                }
            }
        }
    }

    private void excludeIneligibleProperties(List<Property> properties) {
        excludeIneligibleProperties(properties, new HashMap<Property, List<String>>());
    }

    private void excludeIneligibleProperties(List<Property> properties, Map<Property, List<String>> propertyContexts) {
        // firstly, remove any excluded types
        ZOOMAPropertySampler sampler = new ZOOMAPropertySampler();
        sampler.removeExcludedTypes(properties);

        // now, check for strings of a bad length and numeric values
        NumberFormat nf = NumberFormat.getInstance();
        Iterator<Property> propertyIterator = properties.iterator();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.next();
            String propertyValue = property.getPropertyValue();

            // if property value is 3 characters or less, or 500 characters or more in length, exclude
            if (propertyValue.length() <= 3 || propertyValue.length() >= 500) {
                getLog().debug("Property value '" + propertyValue + "' has a length " +
                                       "(" + propertyValue.length() + " characters) " +
                                       "that makes it ineligible for ZOOMA search");
                propertyIterator.remove();
                if (propertyContexts.containsKey(property)) {
                    propertyContexts.remove(property);
                }
            }
            else {
                // if property value is numeric, exclude
                try {
                    nf.parse(property.getPropertyValue());
                    getLog().debug("Property value '" + propertyValue + "' is a numeric value. " +
                                           "This makes it ineligible for ZOOMA search");
                    propertyIterator.remove();
                    if (propertyContexts.containsKey(property)) {
                        propertyContexts.remove(property);
                    }
                }
                catch (java.text.ParseException e) {
                    // if parse exception is thrown, we can continue without removing
                }
            }
        }
    }
}
