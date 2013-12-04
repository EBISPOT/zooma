package uk.ac.ebi.fgpt.zooma.datasource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimPhenotypeEntryReaderTester {
    public static void main(String[] args) {
        try {
            File f = new File("/home/tburdett/zooma2/omim/omim.txt");
            OmimPhenotypeEntryReader reader = new OmimPhenotypeEntryReader(new FileInputStream(f));
            List<OmimPhenotypeEntry> entries = new ArrayList<>();
            OmimPhenotypeEntry entry;
            System.out.print("Reading from " + f.getAbsolutePath() + ".");
            while ((entry = reader.readEntry()) != null) {
                entries.add(entry);
                System.out.print(".");
            }
            System.out.println("done!");

            System.out.println("Successfully read " + entries.size() + " entries from " + f.getAbsolutePath());
            for (OmimPhenotypeEntry nextEntry : entries) {
                System.out.println(nextEntry);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
