import { EBIMasthead, EBIFooter } from "../components/EBI";
import ZoomaNav from "../Navbar";
import { Fragment, Component, ChangeEvent } from "react";
import { Row, Column, Callout } from 'react-foundation'
import ResultsTable from "../components/ResultsTable";
import * as ZoomaApi from '../api/ZoomaApi'
import { getDatasources, ZoomaDatasources } from "../api/ZoomaDatasources";
import { runInThisContext } from "vm";
import DatasourcesModal from "../components/Datasources";
import { ZoomaDatasourceConfig } from "../api/ZoomaDatasourceConfig";
import * as React from 'react'
import Datasources from "../components/Datasources"
import ProgressBar from "@ramonak/react-progress-bar";;

interface Props {
}

interface State {
  datasources:ZoomaDatasources|undefined
  datasourceConfig:ZoomaDatasourceConfig|undefined
  query:string
  searching: boolean,
  progress:number
  results:ZoomaApi.SearchResult[]
}

export default class Home extends Component<Props, State> {

    constructor(props:Props) {
      super(props)

      this.state = {
        datasources: undefined,
        datasourceConfig: undefined,
        query: '',
        searching: false,
        progress: 0,
        results: []
      }
    }

    componentDidMount() {

      this.loadDatasources()


    }

    async loadDatasources() {

      let datasources = await getDatasources()

      let datasourceConfig:ZoomaDatasourceConfig = {
        doNotSearchDatasources: false,

        excludedDatasources: [],
        unrankedDatasources: datasources.datasourceNames,
        rankedDatasources: [],

        doNotSearchOntologies: false,
        ontologySources: []
      }

      this.setState(prevState => ({ ...prevState, datasources, datasourceConfig }))

    }

    render() {

        return (
            <main>
              <Row>
                <h3>Query</h3>
                <Column small={12} medium={12}>
                        <p>Use the text box to find possible ontology mappings for free text terms in the ZOOMA repository of curated annotation knowledge. You can add one term (e.g. 'Homo sapiens') per line. If you also have a type for your term (e.g. 'organism'), put this after the term, separated by a tab.
If you are new to ZOOMA, take a look at our getting started guide.</p>
                </Column>
                <Column small={12} medium={12}>
                  <Row>
                    <Column small={12} medium={12}>
                        <Row className="align-right">
                            <a onClick={this.onClickShowExamples}>
                                Show me some examples...
                            </a>
                        </Row>
                    </Column>
                  </Row>
                  <Row>
                          <textarea style={{minHeight: '300px'}} value={this.state.query} onChange={this.onEditQuery}></textarea>
                  </Row>
                </Column>
              </Row>
              <Row>
                <h3>Datasources</h3>
                <Column small={12} medium={12}>
                        <p>ZOOMA maps text to ontology terms based on curated mappings from selected datasources (more preferred), and by searching ontologies directly (less preferred). Here, you can select which curated datasources to use, optionally ranked in order of preference. You can also select which ontologies to search directly.</p>
                </Column>
                <Column small={12} medium={12}>
                    {
                        this.state.datasources &&
                        this.state.datasourceConfig &&
                        <Datasources datasources={this.state.datasources} datasourceConfig={this.state.datasourceConfig} onConfigChanged={this.onDatasourceConfigChanged} />
                    }
                </Column>
              </Row>
                    <br/>
                <Row className="align-center">
                    <button className="button large" onClick={this.onClickAnnotate}>Annotate</button>
                    &nbsp;
                    <button className="button secondary large"
                        disabled={this.state.results.length === 0}
                        onClick={this.onClickClear}>Clear</button>
                </Row>
                {
                    (this.state.progress > 0 && this.state.progress < 0.99) &&
                        <Row className="align-center">
                <Column small={6} medium={6}>
                        <ProgressBar completed={Math.round(this.state.progress * 100)} bgcolor="#8ebe53" labelColor="#000000"  />
                        </Column>
                        </Row>
                }
              <Row>
                <h3>Results</h3>
                <Column small={12} medium={12}>
                  <p>The table below shows a report describing how ZOOMA annotates text terms supplied above.</p>
                  <ResultsTable results={this.state.results} datasources={this.state.datasources} />
                </Column>
              </Row>
            </main>
        )
    }

    onEditQuery = (e:ChangeEvent) => {
        let newValue = (e.target as any).value
        this.setState(prevState => ({ ...prevState, query: newValue }))
    }

    onClickAnnotate = async () => {

      let properties = this.state.query
            .split('\n')
            .map(line => line.split('\t'))
            .map(tokens => ({ propertyValue: tokens[0], propertyType: tokens[1] }))

      let requiredSources = [...this.state.datasourceConfig!.unrankedDatasources, ...this.state.datasourceConfig!.rankedDatasources]
      let preferredSources = this.state.datasourceConfig!.rankedDatasources
      let ontologySources = this.state.datasourceConfig!.ontologySources
      let doNotSearchDatasources = this.state.datasourceConfig!.doNotSearchDatasources
      let doNotSearchOntologies = this.state.datasourceConfig!.doNotSearchOntologies

      let searchParams:ZoomaApi.SearchParams = {
          properties, requiredSources, preferredSources, ontologySources, doNotSearchDatasources, doNotSearchOntologies
      }

      this.setState(prevState => ({ ...prevState, searching: true }))

      let results = await ZoomaApi.search(searchParams, (progress:number) => {
        this.setState(prevState => ({ ...prevState, progress }))
      })

      this.setState(prevState => ({ ...prevState, searching: false, results }))
    }

    onClickClear = () => {
      this.setState(prevState => ({ ...prevState, query: '', results: [] }))
    }

    onClickShowExamples = () => {
      this.setState(prevState => ({ ...prevState, query: examples }))
    }

    onClickDatasources = () => {
      this.setState(prevState => ({ ...prevState, showDatasourceModal: true }))
    }

    onDatasourceConfigChanged = (config:ZoomaDatasourceConfig) => {
      this.setState(prevState => ({ ...prevState, datasourceConfig: config }))
    }

    onDatasourcesModalDone = () => {
      this.setState(prevState => ({ ...prevState, showDatasourceModal: false }))
    }
}

var examples =
`Bright nuclei
Agammaglobulinemia 2\tphenotype
Reduction in IR-induced 53BP1 foci in HeLa\tcell
Impaired cell migration with increased protrusive activity\tphenotype
C57Black/6\tstrain
nuclei stay close together
Retinal cone dystrophy 3B\tdisease
segregation problems/chromatin bridges/lagging chromosomes/multiple DNA masses
Segawa syndrome autosomal recessive\tphenotype
BRCA1\tgene
Deafness, autosomal dominant 17\tphenotype
cooked broccoli\tcompound
Amyloidosis, familial visceral\tphenotype
Spastic paraplegia 10\tphenotype
Epilepsy, progressive myoclonic 1B\tphenotype
Big cells
Cardiomyopathy, dilated, 1S\tphenotype
Long QT syndrome 3/6, digenic\tdisease
Lung adenocarcinoma disease\tstate
doxycycline 130 nanomolar\tcompound
left tibia\torganism\tpart
CD4-positive
cerebellum\torganism\tpart
hematology traits\tgwas\ttrait
nifedipine 0.025 micromolar\tcompound
Microtubule clumps
`