import { EBIMasthead } from "../components/EBI";
import ZoomaNav from "../Navbar";
import React, { Fragment } from "react";
import { Row, Column } from 'react-foundation'

export default function About() {

  return(
      <main>
        <div className="row">
          <div className="columns medium-12 padding-top-large">
            <h3>About ZOOMA</h3>
            <p>
              ZOOMA is an application for discovering optimal ontology mappings, developed by the <a
                href="//www.ebi.ac.uk/about/spot-team">Samples, Phenotypes and Ontologies Team</a> at EBI.
                It can be used to automatically annotate &quot;properties&quot; (plain text, descriptive values about
                biological entities) with &quot;semantic tags&quot; (ontology classes).
            </p>

            <p>
              If you are new to ZOOMA, take a look at our <a href="./docs/getting-started.html">getting started
                                                                                                guide</a>.
            </p>
          </div>
        </div>

        <div className="row">
          <div className="columns medium-12">
            <h4>Background</h4>

            <p>
              ZOOMA was developed by the Samples, Phenotypes and Ontologies Team at the EBI. We help a number of
              groups and databases annotate their data to ontologies, including
                <a href="//www.ebi.ac.uk/gxa"
                target="_blank"><span className="external">Expression Atlas</span></a>,
                <a href="//www.targetvalidation.org"
                target="_blank"><span className="external">Open Targets</span></a>, the
                <a href="//www.ebi.ac.uk/gwas" target="_blank"><span className="external">GWAS Catalog</span></a>
                and
                many others. This means we handle a wide variety of diverse datasets that contain metadata
                descriptions
                about species, anatomical components, cell types, drugs treatments and compounds, diseases and
                phenotypes and many others.
            </p>

            <p>
              To support richer querying, we assist with the annotation of these metadata descriptions to a series
              of
              ontologies, allowing us to harmonise data across experiments to drive advanced searches and improve
              data
              visualisation. Generating such annotations is a manually intensive operation that requires
              considerable
              curation. And, as ontologies evolve and new technologies for performing functional genomics
              experiments
              emerge, annotations need reviewing and improving. We call the difference between the data we have
              submitted, and the data that is accurately mapped to ontology terms, an <b>&quot;annotation gap&quot;</b>.
            </p>

            <p>
              ZOOMA exists to reduce this annotation gap through automatic annotation. ZOOMA contains a linked
              data
              repository of annotation knowledge and highly annotated data that has been seeded with manually
              curated
              annotation derived from publicly available databases, such as the Expression Atlas, as well as other
              sources.
              By creating this repository of annotations, tracking their provenance and scoring their quality, we
              have
              been able to build a &quot;smart&quot; annotation search service. Because the source data has been curated by
              hand, it is a rich source of knowledge that is optimised for this task of semantic mark-up of
              keywords,
              in contrast with text-mining approaches. It is also capable of capturing more obscure types of
              annotations such as those involving compound properties.
            </p>

            <p>
              The Samples, Phenotypes and Ontologies Team is also responsible for the development and maintainence
              of
                the Experimental Factor Ontology (<a href="//www.ebi.ac.uk/efo"
                target="_blank"><span className="external">EFO</span></a>) and the
                Ontology Lookup Service (<a href="//www.ebi.ac.uk/ols" target="_blank">OLS</a>). If
                you'd like to know more about ontologies, need new know where to find or request terms to describe
                your
                data, or would just like some information, check out the EFO or OLS websites, or have a look at our
                <a
                href="//www.ebi.ac.uk/about/spot-team"
                target="_blank">SPOT homepage</a>. Alternatively, you can get in touch using the contact
                details
                below.
            </p>
          </div>
        </div>

        <div className="row">
          <div className="columns medium-12">
            <h3>Why use ZOOMA?</h3>

            <p>
              You should use ZOOMA if you have data that has been described with any series of keywords. ZOOMA
              provides a mechanism to search for these keywords, using typing or context information or
              combinations
              of co-occuring keywords to improve accuracy, and automatically annotate your data to ontology terms.
            </p>

            <p>
              Using ZOOMA, it is trivially easy to automatically annotate any data that has been richly described
              previously. This improves the interoperability of your data, but also frees up curators to work on
              more
              difficult and interesting tasks, rather than spending time making the same old fixes and corrections
              to
              align data with the current state of the art in ontologies.
            </p>
          </div>
        </div>

        <div className="row">
          <div className="columns medium-12">
            <h3>Contact</h3>

            <p>For questions and to provide feedback, please contact
                <a href="mailto:ontology-tools-support@ebi.ac.uk">ontology-tools-support@ebi.ac.uk</a>.
            </p>

            <p>For bug reports and feature requests, please use our
                <a href="//www.ebi.ac.uk/panda/jira/browse/FGPTO/component/10874"
                target="_blank"><span className="external">JIRA bug tracker</span></a>.
            </p>
          </div>
        </div>

        <div className="row">
          <div className="columns medium-12">
            <h3>Privacy Policy</h3>

            <p>The General Data Protection Regulation (GDPR) will apply in the UK from 25 May 2018. It will replace the 1998 Data Protection Act and introduce new rules on privacy notices, as well as processing and safeguarding personal data.</p>

            <p>
              This website requires cookies, and the limited processing of your personal data in order to function. By using the site you are agreeing to this as outlined in our <a href="//www.ebi.ac.uk/data-protection/privacy-notice/embl-ebi-public-website">Privacy Notice</a> and <a href="//www.ebi.ac.uk/about/terms-of-use">Terms of Use</a>.
            </p>

            <p>
              <a href="//www.ebi.ac.uk/data-protection/privacy-notice/zooma">Zooma Submission Service</a> applies to the data submitted to Zooma (eg. data to ontology annotations) via the zooma-submission@ebi.ac.uk e-mail address.
            </p>

            <p>
              <a href="//www.ebi.ac.uk/data-protection/privacy-notice/zooma-mailing-list">Zooma Mail Service</a> applies to our public e-mail list ontology-tools-support [at] ebi.ac.uk.
            </p>
          </div>
        </div>


        <div className="row">
          <div className="columns medium-12">
            <h3>Thanks</h3>

            <p>
              <a href="//atlassian.com/software/bamboo/overview">Bamboo</a>: Continuous integration,
                                                                                    continuous
                                                                                    deployment and release management.
            </p>

            <p>
              <a href="//atlassian.com/software/fisheye/overview">Fisheye</a>: Browse, search and track your
                                                                                    source code repositories.
            </p>

            <p>
              <a href="//www.jetbrains.com/idea/index.html">IntelliJ IDEA</a> is the full-featured commercial
                                                                                        IDE
                                                                                        — with a complete set of tools
                                                                                        and
                                                                                        integrations with the most
                                                                                        important modern technologies
                                                                                        and
                                                                                        frameworks, such as Spring and
                                                                                        Hibernate — a must-have for
                                                                                        effective Web and Java EE
                                                                                        development.
            </p>

            <p>
              YourKit is kindly supporting open source projects with its full-featured Java Profiler. YourKit, LLC
              is
              the creator of innovative and intelligent tools for profiling Java and .NET applications. Take a
              look at
                YourKit's leading software products: <a href="//www.yourkit.com/java/profiler/index.jsp">YourKit
              Java
                                                                                                                Profiler</a>
                and <a href="//www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.
            </p>

          </div>
        </div>

      </main>

  )

}
