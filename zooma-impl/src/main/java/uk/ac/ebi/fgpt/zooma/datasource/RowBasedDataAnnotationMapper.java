package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.net.URI;
import java.util.Date;

/**
 * An abstract implementation of a data mapper for row based data.  This defines one method for creating annotations and
 * employs a simple caching strategy that retains the previous row in order to link multiple rows together in the case
 * where all fields, except for the semantic tag, are identical.
 * <p/>
 * As this mapper only caches the last row, a prerequisite of any query code that wants to use this mapping strategy is
 * that rows are ordered by the fields that will be compared (i.e. study, bioentity, property type and value) as if rows
 * that are identical by all fields except semantic tags are separated by other rows, this comparison strategy will
 * fail.
 *
 * @author Tony Burdett
 * @date 17/12/13
 */
public abstract class RowBasedDataAnnotationMapper extends Initializable {
    private final AnnotationFactory annotationFactory;

    private URI lastAnnotationURI;
    private Object[] lastRow;

    protected RowBasedDataAnnotationMapper(AnnotationFactory annotationFactory) {
        this.annotationFactory = annotationFactory;
    }

    protected AnnotationFactory getAnnotationFactory() {
        return annotationFactory;
    }

    protected Annotation createAnnotation(URI annotationURI,
                                          String annotationID,
                                          String studyAccession,
                                          URI studyURI,
                                          String studyID,
                                          URI studyType,
                                          String bioentityName,
                                          URI bioentityURI,
                                          String bioentityID,
                                          String bioentityTypeName,
                                          URI bioentityTypeURI,
                                          String propertyType,
                                          String propertyValue,
                                          URI propertyURI,
                                          String propertyID,
                                          URI semanticTag,
                                          String annotator,
                                          Date annotationDate) {
        Object[] row = new Object[]{annotationURI,
                annotationID,
                studyAccession,
                studyURI,
                studyID,
                studyType,
                bioentityName,
                bioentityURI,
                bioentityID,
                bioentityTypeName,
                bioentityTypeURI,
                propertyType,
                propertyValue,
                propertyURI,
                propertyID};

        // have we cached a prior result?
        if (lastRow != null && lastAnnotationURI != null) {
            // if so, compare new data to the last data
            if (compareRows(row, lastRow)) {
                // data apart from semantic tags are equal, so set annotation URI equal to last annotation -
                // this will cause a factory to update the old annotation
                annotationURI = lastAnnotationURI;
            }
        }

        // otherwise create and return the new annotation
        Annotation a =  getAnnotationFactory().createAnnotation(annotationURI,
                                                                annotationID,
                                                                studyAccession,
                                                                studyURI,
                                                                studyID,
                                                                studyType,
                                                                bioentityName,
                                                                bioentityURI,
                                                                bioentityID,
                                                                bioentityTypeName,
                                                                bioentityTypeURI,
                                                                propertyType,
                                                                propertyValue,
                                                                propertyURI,
                                                                propertyID,
                                                                semanticTag,
                                                                annotator,
                                                                annotationDate);
        lastAnnotationURI = a.getURI();
        lastRow = row;
        return a;
    }

    private boolean compareRows(Object[] row, Object[] lastRow) {
        if (row.length == lastRow.length) {
            for (int i = 0; i < row.length; i++) {
                // if either element is not null
                if (row[i] != null || lastRow[i] != null) {
                    // check for unmatchedNulls first, so we can do object comparison second
                    boolean unmatchedNulls = (row[i] == null && lastRow[i] != null) ||
                            (row[i] != null && lastRow[i] == null);
                    if (unmatchedNulls || !row[i].equals(lastRow[i])) {
                        // at least one element unequal, so we can break
                        return false;
                    }
                }
            }
            // all elements were equal, return true
            return true;
        }
        else {
            return false;
        }
    }

    @Override protected void doInitialization() throws Exception {
        // do nothing
    }

    @Override protected void doTermination() throws Exception {
        // do nothing
    }
}
