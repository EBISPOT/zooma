

import React, { Fragment, Component } from "react"
import {UnControlled as CodeMirror} from 'react-codemirror2'

import sparql from './sparql'

enum Renderings {
    HTML = 'HTML',
    XML = 'XML',
    JSON = 'JSON',
    CSV = 'CSV',
    TSV = 'TSV',
    RDFXML = 'RDF/XML',
    N3 = 'RDF/N3'
}

interface Props {
    

    namespacePrefixes:Map<string,string>

    exampleQueries:{
        shortname:string,
        description:string,
        query:string,
    }[]

    defaultQuery: string

    servletBase: string


}

interface State {

    query:string
    rendering:string

}

export default class Lode extends Component<Props, State> {


    codemirror:any

    constructor(props) {
        super(props)

        this.state = {
            query: this.getPrefixes() + this.props.defaultQuery,
            rendering: 'HTML'
        }

        this.codemirror = null

    }

    render() {

        return (
            <div>
                <form id='lodestar-sparql-form' className='ui-widget ui-corner-all' name='lode-star-sparql form' action='#lodestart-sparql-results' method='GET'>
                    <fieldset>
                        <legend>Enter SPARQL Query</legend>
                        <section className='lodestar-grid12 grid_12 alpha'>
                            <p>
                                <CodeMirror
                                    value={this.state.query}
                                    defineMode={sparql}
                                    options={{
                                        mode: 'sparql',
                                        lineNumbers: false
                                    }}
                                    onChange={(editor, data, value) => {
                                        this.setState(prevState => ({ ...prevState, query: value }))
                                    }}
                                    editorDidMount={editor => this.codemirror = editor }
                                />
                            </p>
                            <p style={{float: 'right'}}>
                                <label>Output:
                                    <select onChange={this.onSelectRendering}>
                                        {
                                            Object.keys(Renderings).map(key => (
                                                <option
                                                    value={key}
                                                    selected={this.state.rendering === Renderings[key]}
                                                >{Renderings[key]}</option>
                                            ))
                                        }
                                    </select>
                                </label>
                            </p>
                            <p>
                                <label>Results per page:
                                    <select>
                                        <option value='25'>25</option>
                                        <option value='50'>50</option>
                                        <option value='105'>100</option>
                                    </select>
                                </label>
                                <input id='offset' name='offset' type='hidden' value='0' />
                                <p>
                                    <input type='button' className='button primary' style={{display: 'inline'}}  onClick={this.submitQuery} value='Submit Query' />&nbsp;
                                    <input type='button' className='button secondary' style={{display: 'inline'}} onClick={this.reloadPage} value='Reset' />

                                </p>
                            </p>
                        </section>
                        <section id='example_queries' className='lodestar-grid12 grid_12 alpha'>
                            <p>
                                <h3>Example Queries</h3>
                            </p>
                        </section>
                        <section id='loadstar-results-section'>
                            <div id='pagination' className='pagination-banner'></div>
                            <div style={{padding: '5px', width:'99%', overflow: 'scroll'}}>
                                <table id='loadstar-results-table'></table>
                            </div>
                        </section>

                    </fieldset>
                </form>

            </div>

        )
    }

    componentDidMount() {

    }

    componentWillUnmount() {
    }

    submitQuery = () => {

        let { query } = this.state

        var exp = /^\s*(?:PREFIX\s+\w*:\s?<[^>]*>\s*)*(\w+)\s*.*/i
        var match = exp.exec(query)

        if(match) {
            if (match[1].toUpperCase() == 'CONSTRUCT' || match[1].toUpperCase() == 'DESCRIBE') {

            }
        }
    }

    reloadPage = () => {
    }

    getPrefixes() {

        let prefixes:string[] = []
        
        for(let ns of Array.from(this.props.namespacePrefixes.keys())) {
            prefixes.push('PREFIX ' + ns + ': <' + this.props.namespacePrefixes.get(ns) + '>')
        }

        console.dir(prefixes)

        return prefixes.join('\n') + '\n\n'
    }

    onSelectRendering(event) {

        let rendering = event.target.value as string

        this.setState(prevState => ({ ...prevState, rendering }))
    }

}




