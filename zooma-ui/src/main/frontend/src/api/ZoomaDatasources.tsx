

import * as ZoomaApi from './ZoomaApi'

import { databases } from '../data/databases.json'

let apiUrl = '/spot/zooma/v2/api'

export async function getDatasources():Promise<ZoomaDatasources> {

    let res = await fetch(apiUrl + '/sources', {
        method: 'GET',
        headers: {
            'accept': 'application/json'
        }
    })

    let sources = (await res.json()) as ZoomaApi.Datasource[]

    return new ZoomaDatasources(sources)
}

export class ZoomaDatasources {

    datasourceNames:string[] = []
    searchableOntoNames:{name:string, displayName:string}[] = []
    ontologyPrefixes:string[] = []
    loadedOntologyURIs:string[] = []
    nameDescriptionMap:Map<string,string> = new Map()
    nameTitleMap:Map<string,string> = new Map()
    uriNameMap:Map<string,string> = new Map()

    constructor(sources:ZoomaApi.Datasource[]) {

        for(let source of sources) {

            if(source.type === 'DATABASE') {

                let db = databases[source.name]

                this.datasourceNames.push(source.name)

                if(db !== undefined) {
                    //this.datasourceNames.push(db.name)
                    this.uriNameMap.set(source.uri, source.name)
                    this.nameTitleMap.set(source.name, db.name)
                    this.nameDescriptionMap.set(source.name, db.desc)
                } else {
                    //this.datasourceNames.push(source.name)
                    this.uriNameMap.set(source.uri, source.name)
                    this.nameTitleMap.set(source.name, source.name)
                    this.nameDescriptionMap.set(source.name, 'No description.')
                }

            } else if(source.type === 'ONTOLOGY') {
                this.searchableOntoNames.push({ name: source.name, displayName: source.title + ' (' + source.name + ')' })
                this.ontologyPrefixes.push(source.name)
                this.loadedOntologyURIs.push(source.uri)
                this.nameDescriptionMap.set(source.name, source.description)
                this.uriNameMap.set(source.uri, source.name)
            }
        }

    }
}

