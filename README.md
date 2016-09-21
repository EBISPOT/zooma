[![Build Status](https://travis-ci.org/PRIDE-Utilities/ols-client.svg?branch=master)](https://travis-ci.org/PRIDE-Utilities/ols-client)
ols-client
======================
# OLS Client 
  * [What is OLS Client?](#what-is-ols-client)
  * [Search Options](#search-options)
  * [Using OLS Client](#using-ols-client)
  * [Support](#support)
  * [Maven Dependency](#maven-dependency)
  
**OLS Dialog Publications:**
 * Perez-Riverol Y, Uszkoreit J, Sanchez A, Ternent T, Del Toro N, Hermjakob H, Vizcaíno JA, Wang R. (2015). ms-data-core-api: An open-source, metadata-oriented library for computational proteomics. Bioinformatics. 2015 Apr 24. [PDF File](http://www.ncbi.nlm.nih.gov/pubmed/25910694) [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/25910694)
 * If you use **OLS Client** as part of a paper, please include the references above.

[Go to top of page](#ols-client)

---
## What is OLS Client? 
**OLS Client** is a Java API to the [Ontology Lookup Service](http://www.ebi.ac.uk/ols/) allowing easy access to an extensive list of biomedical ontologies (see [supported ontologies](http://www.ebi.ac.uk/ols/) for a complete list).

**OLS Client** is a part of the PRIDE Utilities including major projects like [ms-data-core-api](https://github.com/PRIDE-Utilities/ms-data-core-api), [pride-mod] (https://github.com/PRIDE-Utilities/pride-mod)
 and [pride-utilities] (https://github.com/PRIDE-Utilities/pride-utilities) Making **OLS Client** a standalone project is done to make it more easily available for other projects.

Five ways of searching the **OLS Client** is supported. See [Search Options](#search-options).

[Go to top of page](#ols-client)

---
Five ways of searching the **OLS Dialog** is supported:
  * [Term Name Search](#term-name-search)
  * [Term ID Search](#term-id-search)
  * [PSI MOD Mass Search](#psi-mod-mass-search)
  * [Browse Ontology](#browse-ontology)
  * [Term Hierarchy Graph](#term-hierarchy-graph)

### Term Name Search 
Term Name Search simply finds all terms having term names that (partially) match the insert search term. Insert the first letters of the name of the term to locate in the search field. Note that the search is in "real time". Meaning that a new search is started (and the result list updated) for every character typed. The number behind the search field is the number of currently matching terms.

Note that in some cases a complete search is not performed until at least four characters have been inserted. If the wanted term is not found, make sure that at least four characters have been inserted.

The search results are listed in the table in the middle, and clicking a term displays additional information about the selected term in the "Term Details" section below.

When the wanted term has been found, select the term in the table and click the "Use Selected Term" button at the bottom of the frame. (Or you can double click on the selected term.)

For an example see the [Screenshots](#screenshots) section.

[Go to top of page](#ols-client)

---
### Term ID Search 
Term ID Search allows you the locate a given term and its details by inserting the term id, e.g., MOD:00425 or GO:0000269.

The results are displayed and selected in the same way as for results from a [Term Name Search](#term-name-search).

For an example see the [Screenshots](#screenshots) section.

[Go to top of page](#ols-client)

---
### PSI MOD Mass Search 
PSI MOD Mass Search allows you to search the PSI-MOD ontology for specific modifications using the mass of the modification. There are four different mass types: DiffAvg and DiffMono corresponding to the average and mono mass of the mass change the modifications results in, and MassAvg and MassMono corresponding to the mass of the modified residue.

Insert the mass, the mass accuracy and the mass type and click on "Search" to perform the search. The results are displayed and selected in the same way as for results from a [Term Name Search](#term-name-search).

[Go to top of page](#ols-client)

---
### Browse Ontology
Browse Ontology makes it possible to find the wanted term by browsing the selected ontology. The ontology is displayed using a tree structure where the relationships between the terms are highlighted. When selecting a term in the tree, details about the selected term is displayed in the "Term Details" section.

The results are selected and used in the same way as for results from a [Term Name Search](#term-name-search).

For an example see the [Screenshots](#screenshots) section.

[Go to top of page](#ols-client)

---
## Using OLS Dialog

### Running the Jar File
Running the jar file (either by double clicking it, or running it from the command line) starts a small example showing how **OLS Dialog** can be used. The code for the example can be found in the SVN archive (in the package named no.uib.olsdialog.example).

### In Other Projects
To use **OLS Dialog** in your project include **OLS Dialog** and the required libraries as dependencies, and make all classes that are going to access the OLS implement the OLSInputable interface (found in the package named no.uib.olsdialog). See the source code for details.

[Go to top of page](#ols-client)

---
## Support 
For questions or additional help, please contact the authors or, if appropriate, e-mail a support request to the PRIDE team at the EBI: `pride-support at ebi.ac.uk` (replace `at` with `@`).

[Go to top of page](#ols-client)

---
## Maven Dependency 
**OLS Dialog** is available for use in Maven projects:
~~~~
    <dependencies>
      <dependency>
        <groupId>uk.ac.ebi.pride.utilities</groupId>
        <artifactId>ols-client</artifactId>
        <version>XXX</version>
      </dependency>
    </dependencies>
~~~~
~~~~
    <repositories>
      <!-- EBI repo -->
      <repository>
        <id>nexus-ebi-release-repo</id>
        <url>http://www.ebi.ac.uk/Tools/maven/repos/content/groups/ebi-repo/</url>
      </repository>
    </repositories>
~~~~
Update the version number (XXX) to latest released version.

[Go to top of page](#ols-client)
