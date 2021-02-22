import { EBIMasthead, EBIScripts, EBIFooter } from "../components/EBI";
import Head from "next/head";
import ZoomaNav from "../components/ZoomaNav";
import { Fragment, Component, ChangeEvent } from "react";
import { Row, Column, Callout } from 'react-foundation'
import ResultsTable from "../components/ResultsTable";
import * as ZoomaApi from '../api/ZoomaApi'
import { getDatasources, ZoomaDatasources } from "../api/ZoomaDatasources";
import { runInThisContext } from "vm";
import DatasourcesModal from "../components/DatasourcesModal";
import { ZoomaDatasourceConfig } from "../api/ZoomaDatasourceConfig";

interface Props {
}

interface State {
  datasources:ZoomaDatasources|undefined
  datasourceConfig:ZoomaDatasourceConfig|undefined
  showDatasourceModal:boolean
  query:string
  searching: boolean,
  progress:number
  results:ZoomaApi.SearchResult[]
}

export default class Home extends Component<Props, State> {

    constructor(props) {
      super(props)

      this.state = {
        datasources: undefined,
        datasourceConfig: undefined,
        showDatasourceModal: false,
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
        unrankedDatasources: datasources.datasourceNames,
        rankedDatasources: [],

        doNotSearchOntologies: false,
        ontologySources: []
      }

      this.setState(prevState => ({ ...prevState, datasources, datasourceConfig }))

    }

    render() {

        return (
          <Fragment>
            <EBIMasthead />
            <ZoomaNav />
            <main>
              {
                this.state.showDatasourceModal &&
                <DatasourcesModal datasources={this.state.datasources} datasourceConfig={this.state.datasourceConfig} onConfigChanged={this.onDatasourceConfigChanged} />
              }
              <Row>
                <Column small={12} medium={6} orderOnSmall={1}>
                  <Row>
                    <Column small={6} medium={6}>
                        <Row>
                        What's this?
                        </Row>
                    </Column>
                    <Column small={6} medium={6}>
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
                  <Row className="align-right">
                      <button className="button" onClick={this.onClickDatasources} style={{marginRight: 'auto'}}>
                        <span className="icon icon-functional" data-icon='s'></span>
                        &nbsp; Datasources
                        </button>
                      <button className="button" onClick={this.onClickAnnotate}>Annotate</button>
                      &nbsp;
                      <button className="button secondary"
                          disabled={this.state.results.length === 0}
                          onClick={this.onClickClear}>Clear</button>
                  </Row>
                </Column>
                <Column small={12} medium={6} orderOnSmall={2}>
                  <Blurb />
                </Column>
              </Row>
              <Row>
                <Column small={12} medium={12}>
                  <h3>Results</h3>
                  <p>The table below shows a report describing how ZOOMA annotates text terms supplied above.</p>
                  <ResultsTable results={this.state.results} datasources={this.state.datasources} />
                </Column>
              </Row>
            </main>
            <EBIFooter />
            <EBIScripts />
          </Fragment>
        )
    }

    onEditQuery = (e:ChangeEvent) => {
        let newValue = e.target['value']
        this.setState(prevState => ({ ...prevState, query: newValue }))
    }

    onClickAnnotate = async () => {

      let properties = this.state.query
            .split('\n')
            .map(line => line.split('\t'))
            .map(tokens => ({ propertyValue: tokens[0], propertyType: tokens[1] }))

      let searchParams:ZoomaApi.SearchParams = {
          properties,
          requiredSources: [],
          preferredSources: [],
          ontologySources: []
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
}

function Blurb() {
  return (
      <Callout>
        <p>Zooma is a tool for mapping free text annotations to ontology term based on a curated repository of annotation knowledge.</p>
        <p>Where mappings are not found in the curated respository one or more ontologies can be selected from the Ontology Lookup Service to increase coverage. For example if you want to map GWAS annotations select the GWAS datasource and a common disease ontology such as EFO or DOID to maximise coverage when terms have no curated mappings.</p>
        <p>Use the text box to find possible ontology mappings for free text terms in the ZOOMA repository of curated annotation knowledge. You can add one term (e.g. 'Homo sapiens') per line. If you also have a type for your term (e.g. 'organism'), put this after the term, separated by a tab. 
If you are new to ZOOMA, take a look at our getting started guide.</p>
      </Callout>
  )
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