package uk.ac.pride.ols.web.service.model;

import java.io.Serializable;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
public class AnnotationHolder implements Serializable {


    private Double annotationNumberValue;

    private String annotationStringValue;

    private String annotationType;

    private String oboId;

    private String termName;

    public AnnotationHolder() {
    }

    public AnnotationHolder(Double annotationNumberValue, String annotationStringValue, String annotationType, String oboId, String termName) {
        this.annotationNumberValue = annotationNumberValue;
        this.annotationStringValue = annotationStringValue;
        this.annotationType = annotationType;
        this.oboId = oboId;
        this.termName = termName;
    }

    public Double getAnnotationNumberValue() {
        return this.annotationNumberValue;
    }

    public void setAnnotationNumberValue(Double annotationNumberValue) {
        this.annotationNumberValue = annotationNumberValue;
    }

    public String getAnnotationStringValue() {
        return this.annotationStringValue;
    }

    public void setAnnotationStringValue(String annotationStringValue) {
        this.annotationStringValue = annotationStringValue;
    }

    public String getAnnotationType() {
        return this.annotationType;
    }

    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public String getOboId() {
        return this.oboId;
    }

    public void setOboId(String oboId) {
        this.oboId = oboId;
    }

    public String getTermName() {
        return this.termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }



}
