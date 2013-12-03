package uk.ac.ebi.fgpt.zooma.datasource;

import java.util.Collection;
import java.util.Date;

/**
 * Simple bean that contains an OMIM ID, a preferred title, and collection of alternative titles for a phenotype in
 * OMIM.
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimPhenotypeEntry {
    private final String omimID;
    private final String preferredTitle;
    private final Collection<String> alternativeTitles;
    private final String lastAnnotator;
    private final Date lastAnnotationDate;

    public OmimPhenotypeEntry(String omimID, String preferredTitle, Collection<String> alternativeTitles) {
        this(omimID, preferredTitle, alternativeTitles, null, null);
    }

    public OmimPhenotypeEntry(String omimID,
                              String preferredTitle,
                              Collection<String> alternativeTitles,
                              String lastAnnotator, Date lastAnnotationDate) {
        this.omimID = omimID;
        this.preferredTitle = preferredTitle;
        this.alternativeTitles = alternativeTitles;
        this.lastAnnotator = lastAnnotator;
        this.lastAnnotationDate = lastAnnotationDate;
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

    public String getLastAnnotator() {
        return lastAnnotator;
    }

    public Date getLastAnnotationDate() {
        return lastAnnotationDate;
    }

    @Override public String toString() {
        return "OmimPhenotypeEntry{" +
                "omimID='" + omimID + '\'' +
                ", preferredTitle='" + preferredTitle + '\'' +
                ", alternativeTitles=" + alternativeTitles +
                ", lastAnnotator='" + lastAnnotator + '\'' +
                ", lastAnnotationDate=" + lastAnnotationDate +
                '}';
    }
}
