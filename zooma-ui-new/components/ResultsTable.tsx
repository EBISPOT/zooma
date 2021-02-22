
import { Component, Fragment, ChangeEvent } from "react"
import { Row, Column } from "react-foundation"
import * as ZoomaApi from "../api/ZoomaApi"
import { ZoomaDatasources } from "../api/ZoomaDatasources"
import { sources } from '../data/sources.json'

interface Props {
    results:ZoomaApi.SearchResult[]
    datasources:ZoomaDatasources|undefined
}

interface State {
}

export default class ResultsTable extends Component<Props, State> {

    constructor(props) {
        super(props)

        this.state = {
        }
    }

    render() {

        return (
            <table>
                <thead>
                    <tr>
                        <th className="context-help">
                            <span className="context-help-label clickable" data-icon="?">Term Type </span>
                        </th>
                        <th className="context-help">
                            <span className="context-help-label clickable" data-icon="?">Term Value </span>
                        </th>
                        <th className="context-help">
                            <span className="context-help-label clickable" data-icon="?">Ontology Class Label </span>
                        </th>
                        <th className="context-help">
                            <span className="context-help-label clickable" data-icon="?">Mapping Confidence </span>
                        </th>
                        <th className="context-help">
                            <span className="context-help-label clickable" data-icon="?">Ontology Class ID </span>
                        </th>
                        <th className="context-help">
                            <span className="context-help-label clickable" data-icon="?">Source </span>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    {
                    this.props.results.map(result =>
                        <tr className={getResultClass(result)}>
                            <td>{result.propertyType}</td>
                            <td>{result.propertyValue}</td>
                            <td>{result.ontologyTermLabel}</td>
                            {/* <td>{result.ontologyTermSynonyms}</td> */}
                            <td>{result.mappingConfidence}</td>
                            <td>{result.ontologyTermID}</td>
                            <td><Datasource datasources={this.props.datasources} uri={result.datasource}/></td>
                            {/* <td>{result.ontologyIRI}</td> */}
                        </tr>
                    )}
                </tbody>
            </table>
        )
    }

}

function Datasource(props) {

    let { datasources, uri } = props

    if(datasources === undefined) {
        return <span>{uri}</span>
    }

    if(datasources.loadedOntologyURIs.indexOf(uri) !== -1) {

        let name = datasources.uriNameMap.get(uri)

        return (
            <a href={'//www.ebi.ac.uk/ols/ontologies/' + name} target="_blank">
                <img src="images/ols-logo.jpg" style={{height: '20px'}} alt={name} />
                &nbsp;
                { name }
            </a>
        )
    }

    let source = sources.filter(s => s.url === uri)[0]
    
    if(source !== undefined) {

        return (
            <a href={source.linkTo} target="_blank">
                <img src={source.logo} style={{height: '20px'}} alt={source.name} />
                &nbsp;
                { source.name }
            </a>
        )
    }

    return (
        <span>{uri}</span>
        // <a href={source.linkTo}>
        //     <img src={source.logo} />
        //     {source.name}
        // </a>
    )
}

function getResultClass(result) {

    return ({
        'High': 'automatic',
        'Good': 'curation',
        'Medium': 'curation',
        'Low': 'curation'
    })[result.mappingConfidence] || 'unmapped'

}



