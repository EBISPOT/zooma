import Link from "next/link";
import React, { Fragment } from "react";
import { Row } from "react-foundation";
import { EBIMasthead, EBIFooter, EBIScripts } from "../../components/EBI";
import ZoomaNav from "../../components/ZoomaNav";


export default function docs() {
    return (
        <Fragment>
            <EBIMasthead />
            <ZoomaNav />
            <main>
                <h2>Getting Started</h2>
                <h3>Basics</h3>
                <p className="grid_16 alpha">
                    The ZOOMA <a href="../index.html">homepage</a> contains a text box that allows you to discover
                    possible ontology mappings for your terms. Try clicking the "Show me some examples..." link
                    above
                    this box - it should populate the box with example terms. Hit 'Annotate'. A progress bar
                    appears, showing you when ZOOMA has mapped your terms.
                </p>
                <img src="/images/show-me-examples.png" className="grid_6 omega" alt="Show me some examples..."/>
                <p>
                    Once complete, you should see a table of results. Green shows that ZOOMA could map this term
                    "automatically", i.e. with a HIGH degree of confidence. Yellow shows that ZOOMA was less sure with a GOOD, MEDIUM or LOW degree of confidence, and
                    results might need further curator intervention to determine if ZOOMA is correct.<br/>
                    We can help Zooma return a mapping with higher confidence if we supply additional context for a term by adding a type to
                    our search. If you look at the text box, you'll see when you clicked "Show me some examples..." it
                    populated a type after some values, with a tab between them, e.g.: <i>cooked broccoli	compound</i>.
                </p>
                <img style={{textAlign: 'center'}} src="/images/full-example.png" alt="A ZOOMA table of results"/>
                <p>
                    Let's look at some of the examples in a little more detail, and try to make it harder for Zooma to find a result.
                    First let us limit it's sources by annotating the terms to the Expression Atlas datasource.<br/> Zooma gives you the option
                    to limit the sources that it will use to find annotation predictions. These sources are either curated datasources,
                    or ontologies from the <a href="//www.ebi.ac.uk/ols" target="_blank">Ontology Lookup Service</a>. <br/>
                    From the 'Configure Datasources' gear, select 'Configure Curated Datasources' and select only Expression Atlas as the source to be searched in.
                </p>
                <img style={{textAlign: 'center'}} 
                     src="/images/configure-datasources.png"
                     alt="Configure Zooma Datasources"/>
                <p>
                    Next limit the terms to the four ones shown below, and remove the type 'phenotype' from spastic paraplegia 10. <br/>
                    Now hit 'Annotate'. <br/>
                    Again we can see that some terms where mapped with high confidence, and others with good. If we look at the terms that weren't mapped,
                    in the case of spastic paraplegia 10, we can see that Zooma will not find a mapping in the Expression Atlas datasource.
                </p>
                <img style={{textAlign: 'center'}} src="/images/results.png" alt="A ZOOMA table of results"/>
                <p>
                    Lets try to help Zooma out again, by adding some context.
                    If we add a type to spastic paraplegia 10, we can improve our confidence. Modify the input box -
                    after the term Spastic paraplegia 10, put a tab and add 'phenotype'. Now hit 'Annotate' again. You should see
                    that the third result in our table has gone yellow with a good confidence.
                </p>
                <img style={{textAlign: 'center'}} 
                     src="/images/results-extra-type.png"
                     alt="More results, with additional typing"/>
                <p>
                    If you open the 'Configure Datasources' gear, and de-select the Expression Atlas datasource, Zooma will look into all the available curated datasources.
                    If you Annotate the terms once again, you will see that Zooma has found a mapping with high confidence in the EVA ClinVar database for spastic paraplegia.
                </p>
                <img style={{textAlign: 'center'}} 
                     src="/images/results-all-datasources.png"
                     alt="More results, with additional datasources"/>
                <h3>Worked Examples</h3>
                <p>
                    Below are some example inputs you can run against ZOOMA. These datasets are all taken from
                    ArrayExpress. You can open any of these files and copy and paste everything into the ZOOMA search
                    box to see what coverage you get.
                </p>
                <ul>
                    <li><a target="_blank" href="./examples/example-1.txt">ZOOMA example input 1</a></li>
                    <li><a target="_blank" href="./examples/example-2.txt">ZOOMA example input 2</a></li>
                    <li><a target="_blank" href="./examples/example-3.txt">ZOOMA example input 3</a></li>
                    <li><a target="_blank" href="./examples/example-4.txt">ZOOMA example input 4</a></li>
                    <li><a target="_blank" href="./examples/example-5.txt">ZOOMA example input 5</a></li>
                    <li><a target="_blank" href="./examples/example-6.txt">ZOOMA example input 6</a></li>
                    <li><a target="_blank" href="./examples/example-7.txt">ZOOMA example input 7</a></li>
                    <li><a target="_blank" href="./examples/example-8.txt">ZOOMA example input 8</a></li>
                    <li><a target="_blank" href="./examples/example-9.txt">ZOOMA example input 9</a></li>
                    <li><a target="_blank" href="./examples/example-10.txt">ZOOMA example input 10</a></li>
                </ul>

                <p>
                    Copy and paste the data from one of the example input files, above, into the ZOOMA search box. Hit
                    annotate and check you have some sensible looking results.
                </p>

                <h4>Zooma Results</h4>

                <p>
                    We're going to export and inspect ZOOMA's output to see how well it did. Above your table of
                    results,
                    you should see an option to "Download my results". If you click this, you should be able to open a
                    complete report from ZOOMA in a tab delimited text format in your browser.
                </p>

                <p>
                    Either right-click this link, then "Save as..." or else open the link and copy and paste the data.
                    Open
                    it in your spreadsheet editor of choice (Excel or Google Drive should work fine).
                </p>

                <p>
                    For example, you might get ZOOMA output like this:
                </p>
                <table>
                    <thead>
                    <tr>
                        <th>PROPERTY TYPE</th>
                        <th>PROPERTY VALUE</th>
                        <th>ONTOLOGY TERM LABEL(S)</th>
                        <th>MAPPING TYPE</th>
                        <th>ONTOLOGY TERM(S)</th>
                        <th>SOURCE(S)</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>Organism</td>
                        <td>Arabidopsis thaliana</td>
                        <td>Arabidopsis thaliana</td>
                        <td>High</td>
                        <td><a href="http://purl.obolibrary.org/obo/NCBITaxon_3702" target="_blank">NCBITaxon_3702</a>
                        </td>
                        <td>http://www.ebi.ac.uk/gxa</td>
                    </tr>
                    </tbody>
                </table>
                <p>
                    Does this look like a reasonable result?
                </p>
                <h4>Configuring Datasources</h4>
                <p>
                    Zooma gives you the option to limit the sources that it will use to find annotation predictions. These sources are either curated datasources, i.e.
                    manually curated annotations derived from public databases,
                    or annotations found from ontologies in the <a href="//www.ebi.ac.uk/ols" target="_blank">Ontology Lookup Service</a>.
                </p>
                <p>
                    By selecting the 'Configure Datasources' gear under the Zooma text box, you can either
                    select from the available curated datasources, or look for an ontology in the <a href="//www.ebi.ac.uk/ols" target="_blank">Ontology Lookup Service</a>.
                </p>
                <img style={{textAlign: 'center'}}
                     src="/images/configure-datasources-complete.png"
                     alt="Configure Datasources"/>

                <img style={{textAlign: 'center'}}
                     src="/images/ontology-sources.png"
                     alt="Configure Ontology Sources"/>

                <h3>Adding New Mappings to Zooma</h3>
                
                <p>
                    From here on, we're going to review how to feed back data to Zooma to help improve mappings in
                    future. For example, if you found results in the worked example above that looked wrong, or if Zooma
                    gave multiple hits, or if there was something that did not map that you think should map, you can
                    add new mappings to Zooma to help improve it's output. Below, we're going to see how to do this.
                    <br/><br/>
                    Let say that you've copied/pasted example 9 in zooma and clicked on the 'annotate' button.<br/>
                    Looking at the term 'ripening' you can see that it is mapped to semantic tag PO_0007010 ("whole plant fruit ripening stage")
                    which is the ripening stage of the whole plant starting when the first fruit starts ripening and ending
                    when the last fruit finishes ripening.
                    <br/>Maybe you think that this association is wrong and you'd rather see 'ripening' being associated
                    with semantic tag PO_0025502 ("fruit ripening stage") which is the ripening of one fruit.<br/>
                    Then you can submit a tab file to our <a href="//www.ebi.ac.uk/panda/jira/browse/FGPTO/component/10874"
                    target="_blank"><span className="external">JIRA bug tracker</span></a> which would be as follow :<br/>
                    <table>
                    <thead>
                    <tr>
                        <th>STUDY</th>
                        <th>BIOENTITY</th>
                        <th>PROPERTY_TYPE</th>
                        <th>PROPERTY_VALUE</th>
                        <th>SEMANTIC_TAG</th>
                        <th>ANNOTATOR</th>
                        <th>ANNOTATION_DATE</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td></td>
                        <td></td>
                        <td>plant structure development stage</td>
                        <td>ripening</td>
                        <td>http://purl.obolibrary.org/obo/PO_0025502</td>
                        <td>your_name</td>
                        <td>the_date</td>
                    </tr>
                    </tbody>
                    </table>
                    </p>
                <p>
                    The study, bioentity and type are not mandatory but help enriching the annotation for searches with
                    more context.<br/>
                    The date should be in the following format : 2015-08-21 12:00:00<br/>
                    We will then load that file to Zooma in one of our weekly data releases. Once that done, when
                    searching for 'ripening', Zooma would also suggest the PO_0025502 term you added.
                    <br/>
                    <br/>
                    Below is an example of an annotation submited by the <a href="//www.ebi.ac.uk/gxa/home">Expression
                    Atlas</a> group at the EBI :
                </p>
                <table>
                    <thead>
                    <tr>
                        <th>STUDY</th>
                        <th>BIOENTITY</th>
                        <th>PROPERTY_TYPE</th>
                        <th>PROPERTY_VALUE</th>
                        <th>SEMANTIC_TAG</th>
                        <th>ANNOTATOR</th>
                        <th>ANNOTATION_DATE</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>E-GEOD-10927</td>
                        <td>GSE10927GSM277147</td>
                        <td>biopsy site</td>
                        <td>left</td>
                        <td>http://www.ebi.ac.uk/efo/EFO_0001658</td>
                        <td>Eleanor Williams</td>
                        <td>01/10/2014 18:32</td>
                    </tr>
                    </tbody>
                </table>
                <br/>
                <br/>
                <p>
                Now imagine that, to save curator time and energy, you've got this pipeline which uses the zooma rest api to
                automatically annotate your data. Because you want to be on the safe side, the pipeline only accept annotations
                which come back with a 'HIGH' mapping confidence.<br/>
                Looking back at the 'example 9', you can see that the term 'GM08928' is mapped to semantic tag 'CLO_0011237'.
                You know that this is true but in Zooma, because not many sources say so, it comes back with a 'GOOD' score
                rather then a 'HIGH' score. So even though your automatic pipeline runs every week, you find yourself
                having to annotate the same term over an over again.<br/>
                To avoid that, you can also submit a spreadsheet, as described above, associating term 'GM08928' to semantic
                tag 'CLO_0011237' and submit it to us. Because now zooma has the same association from 2 different datasources
                then it is likely to come back next time with a 'HIGH' score and your annotation will be done automatically.
                </p>
            </main>
            <h3>Documentation</h3>
            <ul id="secondary-nav">
                <li className="first"><Link href="/docs">Home</Link></li>
                <li className="active"><Link href="/docs/search">Getting Started</Link></li>
                <li><Link href="/docs/api">REST API Documentation</Link></li>
                <li className="last"><Link href="/docs/developers">Developer Documentation</Link></li>
            </ul>
            <EBIFooter />
            <EBIScripts />
        </Fragment>
    )
}