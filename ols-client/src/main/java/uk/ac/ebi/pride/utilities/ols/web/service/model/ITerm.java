package uk.ac.ebi.pride.utilities.ols.web.service.model;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This interface allow to represent actual terms from the ontology and summary terms from the search results.
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 10/07/2017.
 */
public interface ITerm {

    String getName();
    Identifier getIri();
    Identifier getShortName();
    Identifier getOboId();
    String[] getDescription();
    String getOntologyName();
    String getOntologyIri();
    Annotation getAnnotation();
    Identifier getGlobalId();

}
