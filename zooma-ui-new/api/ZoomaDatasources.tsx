

import * as ZoomaApi from './ZoomaApi'

import { databases } from '../data/databases.json'

export async function getDatasources():Promise<ZoomaDatasources> {

    let res = await fetch('/api/sources', {
        method: 'GET'
    })

    let sources = (await res.json()) as ZoomaApi.Datasource[]

    return new ZoomaDatasources(sources)
}

export class ZoomaDatasources {

    datasourceNames:string[] = []
    searchableOntoNames:string[] = []
    ontologyPrefixes:string[] = []
    loadedOntologyURIs:string[] = []
    nameDescriptionMap:Map<string,string> = new Map()
    nameTitleMap:Map<string,string> = new Map()
    uriNameMap:Map<string,string> = new Map()

    constructor(sources:ZoomaApi.Datasource[]) {

        for(let source of sources) {

            if(source.type === 'DATABASE') {

                let db = databases[source.name]

                if(db !== undefined) {
                    this.datasourceNames.push(db.name)
                    this.nameDescriptionMap[source.name] = db.desc
                } else {
                    this.datasourceNames.push(source.name)
                    this.nameDescriptionMap[source.name] = 'No description.'
                }

                this.uriNameMap[source.uri] = source.name

            } else if(source.type === 'ONTOLOGY') {
                this.searchableOntoNames.push(source.title + ' (' + source.name + ')')
                this.ontologyPrefixes.push(source.name)
                this.loadedOntologyURIs.push(source.uri)
                this.nameDescriptionMap.set(source.name, source.description)
                this.uriNameMap.set(source.uri, source.name)
            }
        }

    }
}

