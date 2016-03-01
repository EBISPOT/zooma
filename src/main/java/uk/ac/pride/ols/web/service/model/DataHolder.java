package uk.ac.pride.ols.web.service.model;

import java.io.Serializable;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
public class  DataHolder implements Serializable {
        private Double annotationNumberValue;
        private String annotationStringValue;
        private String annotationType;
        private String termId;
        private String termName;
        private Object __equalsCalc = null;
        private boolean __hashCodeCalc = false;

        public DataHolder() {
        }

        public DataHolder(Double annotationNumberValue, String annotationStringValue, String annotationType, String termId, String termName) {
            this.annotationNumberValue = annotationNumberValue;
            this.annotationStringValue = annotationStringValue;
            this.annotationType = annotationType;
            this.termId = termId;
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

        public String getTermId() {
            return this.termId;
        }

        public void setTermId(String termId) {
            this.termId = termId;
        }

        public String getTermName() {
            return this.termName;
        }

        public void setTermName(String termName) {
            this.termName = termName;
        }

        public synchronized boolean equals(Object obj) {
            if(!(obj instanceof DataHolder)) {
                return false;
            } else {
                DataHolder other = (DataHolder)obj;
                if(obj == null) {
                    return false;
                } else if(this == obj) {
                    return true;
                } else if(this.__equalsCalc != null) {
                    return this.__equalsCalc == obj;
                } else {
                    this.__equalsCalc = obj;
                    boolean _equals = (this.annotationNumberValue == null && other.getAnnotationNumberValue() == null || this.annotationNumberValue != null && this.annotationNumberValue.equals(other.getAnnotationNumberValue())) && (this.annotationStringValue == null && other.getAnnotationStringValue() == null || this.annotationStringValue != null && this.annotationStringValue.equals(other.getAnnotationStringValue())) && (this.annotationType == null && other.getAnnotationType() == null || this.annotationType != null && this.annotationType.equals(other.getAnnotationType())) && (this.termId == null && other.getTermId() == null || this.termId != null && this.termId.equals(other.getTermId())) && (this.termName == null && other.getTermName() == null || this.termName != null && this.termName.equals(other.getTermName()));
                    this.__equalsCalc = null;
                    return _equals;
                }
            }
        }

        public synchronized int hashCode() {
            if(this.__hashCodeCalc) {
                return 0;
            } else {
                this.__hashCodeCalc = true;
                int _hashCode = 1;
                if(this.getAnnotationNumberValue() != null) {
                    _hashCode += this.getAnnotationNumberValue().hashCode();
                }

                if(this.getAnnotationStringValue() != null) {
                    _hashCode += this.getAnnotationStringValue().hashCode();
                }

                if(this.getAnnotationType() != null) {
                    _hashCode += this.getAnnotationType().hashCode();
                }

                if(this.getTermId() != null) {
                    _hashCode += this.getTermId().hashCode();
                }

                if(this.getTermName() != null) {
                    _hashCode += this.getTermName().hashCode();
                }

                this.__hashCodeCalc = false;
                return _hashCode;
            }
        }

}
