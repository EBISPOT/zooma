package uk.ac.ebi.fgpt.zooma.datasource;

import java.util.Collection;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 04/07/14
 */
public class UniprotHumanDiseaseEntry {
    private final String accession;
    private final String name;
    private final String omimID;
    private final Collection<String> synonyms;

    public UniprotHumanDiseaseEntry(String accession,
                                    String name,
                                    String omimID,
                                    Collection<String> synonyms) {
        this.accession = accession;
        this.name = name;
        this.omimID = omimID;
        this.synonyms = synonyms;
    }

    public String getAccession() {
        return accession;
    }

    public String getName() {
        return name;
    }

    public String getOmimID() {
        return omimID;
    }

    public Collection<String> getSynonyms() {
        return synonyms;
    }

    @Override public String toString() {
        return "UniprotHumanDiseaseEntry{" +
                "accession='" + accession + '\'' +
                ", name='" + name + '\'' +
                ", omimID='" + omimID + '\'' +
                ", synonyms=" + synonyms +
                '}';
    }
}
