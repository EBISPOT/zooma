

import React, { Component, Fragment, ChangeEvent } from "react"
import { Row, Column, Tabs, TabItem, TabsContent, TabPanel } from "react-foundation"
import * as ZoomaApi from "../api/ZoomaApi"
import { ZoomaDatasources } from "../api/ZoomaDatasources"
import { sources } from '../data/sources.json'
import Modal from './Modal'
import classNames from 'classnames'
import DragAndDropLists, { List, ListEntry } from "./DragAndDropLists"
import { ZoomaDatasourceConfig } from "../api/ZoomaDatasourceConfig"
import Autocomplete from 'react-autocomplete'

interface Props {
    datasources:ZoomaDatasources
    datasourceConfig:ZoomaDatasourceConfig
    onConfigChanged:(config:ZoomaDatasourceConfig)=>void
}

interface State {
    tab:'curated'|'ontology'
    ontoAutocomplete:string
}

export default class DatasourcesModal extends Component<Props, State> {

    constructor(props) {
        super(props)

        this.state = {
            tab: 'curated',
            ontoAutocomplete: ''
        }
    }

    render() {

        let { tab } = this.state

        let { datasources, datasourceConfig } = this.props

        let lists = [
            { 
                title: 'Unranked',
                entries: datasourceConfig.unrankedDatasources.map(ds => ({ id: ds, content: ds })),
            },
            {
                title: 'Ranked',
                entries: datasourceConfig.rankedDatasources.map(ds => ({ id: ds, content: ds }))
            }
        ]

        return (
            <Modal title="Configure Datasources">
                {/* <div className="tabs-content">
                <ul className="tabs" data-tabs>
                    <li className={classNames('tabs-title', {'is-active': tab === 'curated'} )}><a>Curated Datasources</a></li>
                    <li className={classNames('tabs-title', {'is-active': tab === 'ontology'} )}><a>Ontology Sources</a></li>
                </ul>
                </div>
                <div className="tabs-panel">
                    <p>
                    test
                    </p>
                </div>
                <button className="button">Done</button> */}
                <Tabs>
                    <TabItem isActive={tab === 'curated'} onClick={(e) => { this.setState({ tab: 'curated' }) }}>
                        <a href={'#curated'} aria-selected={tab === 'curated'}>Curated Datasources</a>
                    </TabItem>
                    <TabItem isActive={tab === 'ontology'} onClick={(e) => { this.setState({ tab: 'ontology' }) }}>
                        <a href={'#ontology'} aria-selected={tab === 'ontology'}>Ontology Sources</a>
                    </TabItem>
				</Tabs>
				<TabsContent>
					<TabPanel id={'curated'} isActive={tab === 'curated'}>
                        <label>
                            <input type="checkbox"/>
                            Don't search in any datasources
                        </label>
                        <DragAndDropLists lists={lists} onChange={this.onDatasourceListsChanged}/>
                    </TabPanel>
					<TabPanel id={'ontology'} isActive={tab === 'ontology'}>
                        <label>
                            <input type="checkbox"/>
                            Don't search in any ontologies
                            <br/>
                            <Autocomplete
                                getItemValue={(item) => item}
                                shouldItemRender={(item, value) => item.toLowerCase().indexOf(value.toLowerCase()) > -1}
                                inputProps={{
                                    placeholder: 'Search ontologies by name, e.g. EFO or Experimental Factor Ontology',
                                    style:{ width: '500px', padding: '8px'}
                                }}
                                items={datasources.searchableOntoNames}
                                renderItem={(item, isHighlighted) =>
                                    <div style={{ background: isHighlighted ? 'lightgray' : 'white', padding: '8px' }}>
                                        {item}
                                    </div>
                                }
                                value={this.state.ontoAutocomplete}
                                onChange={(e, val) => this.setState((s) => ({ ...s, ontoAutocomplete: val }))}
                                onSelect={this.onSelectOntology}
                            />
                            {
                                <ul>
                                {datasourceConfig.ontologySources.map(source => {
                                    return <li key={source}>
                                        <input type="checkbox"/>
                                        {source}
                                    </li>
                                })}
                                </ul>
                            }
                        </label>
                    </TabPanel>
				</TabsContent>
                <br/>
                <button className="button">Done</button>
            </Modal>
        )
    }

    onDatasourceListsChanged = (lists:List[]) => {

        let [ unrankedDatasources, rankedDatasources ] = lists

        let newConfig:ZoomaDatasourceConfig = {
            ...this.props.datasourceConfig,
            unrankedDatasources: unrankedDatasources.entries.map(d => d.id),
            rankedDatasources: rankedDatasources.entries.map(d => d.id)
         }

        this.props.onConfigChanged(newConfig)
    }

    onSelectOntology = (val) => {

        let newConfig:ZoomaDatasourceConfig = {
            ...this.props.datasourceConfig,
            // ontologySources: [
         }


        this.setState((s) => ({ ...s, ontoAutocomplete: '' }))
    }


}

