package uk.ac.ebi.fgpt.zooma.datasource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 04/07/14
 */
public class UniprotHumanDiseaseVocabReaderTester {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://www.uniprot.org/docs/humdisease.txt");
            UniprotHumanDiseaseVocabReader reader = new UniprotHumanDiseaseVocabReader(url.openStream());
            List<UniprotHumanDiseaseEntry> entries = new ArrayList<>();
            UniprotHumanDiseaseEntry entry;
            System.out.print("Reading from " + url + ".");
            while ((entry = reader.readEntry()) != null) {
                entries.add(entry);
                System.out.print(".");
            }
            System.out.println("done!");

            System.out.println("Successfully read " + entries.size() + " entries from " + url);
            for (UniprotHumanDiseaseEntry nextEntry : entries) {
                System.out.println(nextEntry);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
