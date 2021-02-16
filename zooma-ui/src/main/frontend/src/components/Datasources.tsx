


import * as React from 'react'
import { Row, Column, Tabs, TabItem, TabsContent, TabPanel, Button } from "react-foundation"
import * as ZoomaApi from "../api/ZoomaApi"
import { ZoomaDatasources } from "../api/ZoomaDatasources"
import { sources } from '../data/sources.json'
import Modal from './Modal'
import classNames from 'classnames'
import DragAndDropLists, { List, ListEntry } from "./DragAndDropLists"
import { ZoomaDatasourceConfig } from "../api/ZoomaDatasourceConfig"
import Autocomplete from 'react-autocomplete'
import { Fragment } from 'react'

interface Props {
    datasources:ZoomaDatasources
    datasourceConfig:ZoomaDatasourceConfig
    onConfigChanged:(config:ZoomaDatasourceConfig)=>void
}

interface State {
    tab:'curated'|'ontology'
    ontoAutocomplete:string
}

export default class Datasources extends React.Component<Props, State> {

    constructor(props) {
        super(props)

        this.state = {
            tab: 'curated',
            ontoAutocomplete: ''
        }

        console.log('datasources are')
        console.dir(props.datasources)
    }

    render() {

        let { tab } = this.state

        let { datasources, datasourceConfig } = this.props

        let lists = [
            { 
                title: 'Excluded',
                entries: datasourceConfig.excludedDatasources.map(ds => ({
                    id: ds,
                    content: <Fragment>{datasources.nameTitleMap.get(ds) || ds}</Fragment>
                })),
            },
            { 
                title: 'Unranked',
                entries: datasourceConfig.unrankedDatasources.map(ds => ({
                    id: ds,
                    content: <Fragment>{datasources.nameTitleMap.get(ds) || ds}</Fragment>
                })),
            },
            {
                title: 'Ranked',
                entries: datasourceConfig.rankedDatasources.map(ds => ({
                    id: ds,
                    content: <Fragment>{datasources.nameTitleMap.get(ds) || ds}</Fragment>
                }))
            }
        ]

        console.log('lists')
        console.dir(lists)

        return (
            <Fragment>
				<Row style={{border: '1px solid #777', padding: '12px'}}>
                    <Column small={6}>
                        <h4>1. Curated Datasources</h4>
                        <label>
                            <input type="checkbox" checked={datasourceConfig.doNotSearchDatasources} onChange={this.onChangeDoNotSearchDatasources} />
                            Don't search in any datasources
                        </label>
                        <DragAndDropLists lists={lists} onChange={this.onDatasourceListsChanged}/>
                        <a onClick={this.excludeAll}>Exclude all</a>
                    </Column>
					<Column small={6}>
                        <h4>2. Ontology Sources</h4>
                        <label>
                            <input type="checkbox" checked={datasourceConfig.doNotSearchOntologies} onChange={this.onChangeDoNotSearchOntologies} />
                            Don't search in any ontologies
                        </label>
                        <Autocomplete
                            getItemValue={(item) => item.name}
                            shouldItemRender={(item, value) => item.displayName.toLowerCase().indexOf(value.toLowerCase()) > -1}
                            inputProps={{
                                placeholder: 'Search ontologies by name, e.g. EFO or Experimental Factor Ontology',
                                style:{ width: '500px', padding: '8px'}
                            }}
                            items={datasources.searchableOntoNames}
                            renderItem={(item, isHighlighted) =>
                                <div style={{ background: isHighlighted ? 'lightgray' : 'white', padding: '8px' }}>
                                    {item.displayName}
                                </div>
                            }
                            value={this.state.ontoAutocomplete}
                            onChange={(e, val) => this.setState((s) => ({ ...s, ontoAutocomplete: val }))}
                            onSelect={this.onSelectOntology}
                        />
                        <br/>
                        <br/>
                        <div>
                            {datasourceConfig.ontologySources.map(source => {
                                return <div key={source}>
                                    <a onClick={() => this.removeOntologySource(source)}>x</a>
                                    &nbsp;
                                    {source}
                                </div>
                            })}
                        </div>
                    </Column>
				</Row>
            </Fragment>
        )
    }

    onDatasourceListsChanged = (lists:List[]) => {

        let [ excludedDatasources, unrankedDatasources, rankedDatasources ] = lists

        let newConfig:ZoomaDatasourceConfig = {
            ...this.props.datasourceConfig,
            excludedDatasources: excludedDatasources.entries.map(d => d.id),
            unrankedDatasources: unrankedDatasources.entries.map(d => d.id),
            rankedDatasources: rankedDatasources.entries.map(d => d.id)
         }

        this.props.onConfigChanged(newConfig)
    }

    onSelectOntology = (val) => {

        let newSources = [...this.props.datasourceConfig.ontologySources]

        if(newSources.indexOf(val) === -1) {
            newSources.push(val)
        }

        let newConfig:ZoomaDatasourceConfig = {
            ...this.props.datasourceConfig,
            ontologySources: newSources
         }

        this.setState((s) => ({ ...s, ontoAutocomplete: '' }))

        this.props.onConfigChanged(newConfig)
    }

    onChangeDoNotSearchDatasources = (e) => {

        let newConfig:ZoomaDatasourceConfig = {
            ...this.props.datasourceConfig,
            doNotSearchDatasources: e.target.checked
         }

        this.props.onConfigChanged(newConfig)
    }

    onChangeDoNotSearchOntologies = (e) => {

        let newConfig:ZoomaDatasourceConfig = {
            ...this.props.datasourceConfig,
            doNotSearchOntologies: e.target.checked
         }

        this.props.onConfigChanged(newConfig)
    }

    removeOntologySource = (s) => {

        let newSources = [...this.props.datasourceConfig.ontologySources]

        let i = newSources.indexOf(s)

        if(i !== -1) {
            newSources.splice(i, 1)
        }

        let newConfig:ZoomaDatasourceConfig = {
            ...this.props.datasourceConfig,
            ontologySources: newSources
         }

        this.props.onConfigChanged(newConfig)
    }


    excludeAll = () => {
        
        let config = this.props.datasourceConfig

        let excluded = [
            ...config.excludedDatasources,
            ...config.unrankedDatasources,
            ...config.rankedDatasources
        ]

        let newConfig:ZoomaDatasourceConfig = {
            ...this.props.datasourceConfig,
            excludedDatasources: excluded,
            unrankedDatasources: [],
            rankedDatasources: []
         }

        this.props.onConfigChanged(newConfig)
    }

}

