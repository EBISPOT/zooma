package uk.ac.ebi.fgpt.zooma.datasource;

import java.util.Collection;

/**
 * Simple bean that contains an OMIM ID, a preferred title, and collection of alternative titles.
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimEntry {
    private final String omimID;
    private final String preferredTitle;
    private final Collection<String> alternativeTitles;

    public OmimEntry(String omimID, String preferredTitle, Collection<String> alternativeTitles) {
        this.omimID = omimID;
        this.preferredTitle = preferredTitle;
        this.alternativeTitles = alternativeTitles;
    }

    public String getOmimID() {
        return omimID;
    }

    public String getPreferredTitle() {
        return preferredTitle;
    }

    public Collection<String> getAlternativeTitles() {
        return alternativeTitles;
    }

    @Override public String toString() {
        return "OmimEntry{" +
                "omimID='" + omimID + '\'' +
                ", preferredTitle='" + preferredTitle + '\'' +
                ", alternativeTitles=" + alternativeTitles +
                '}';
    }
}
