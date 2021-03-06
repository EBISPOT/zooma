
let apiUrl = '/spot/zooma/v2/api'

export interface SearchProperty {
}

export interface SearchParams {
    properties: { 
        propertyValue: string
        propertyType: string
    }[]

    doNotSearchDatasources:boolean
    requiredSources:string[]
    preferredSources:string[]

    doNotSearchOntologies:boolean
    ontologySources:string[]
}

    /*
     [0] - property type
     [1] - property value
     [2] - matched ontology term label
     [3] - matched ontology term synonyms
     [4] - mapping confidence
     [5] - matched ontology term "ID" (i.e. fragment)
     [6] - matched ontology URI
     [7] - datasource
     */

export interface SearchResult {
    propertyType:string
    propertyValue:string
    ontologyTermLabel:string
    ontologyTermSynonyms:string
    mappingConfidence:string
    ontologyTermID:string
    ontologyURI:string
    datasource:string
}

export interface Datasource {
    type:string
    name:string
    longName:string|undefined
    description:string
    uri:string
    title:string
}

export async function search(params:SearchParams, onProgress:(pc:number)=>void) {

    let filter = ''

    if(params.doNotSearchDatasources === true) {
        filter += 'required:[Select%20None]'
    } else {
        if(params.requiredSources.length > 0) {
            filter += 'required:[' + params.requiredSources.join(',') + ']'
        }
        if (params.preferredSources.length > 0) {
            filter += 'preferred:[' + params.preferredSources.join(',') + ']'
        }
    }

    if(params.doNotSearchOntologies === true) {
        filter += 'ontologies:[Select%20None]'
    } else {
        if(params.ontologySources.length > 0) {
            filter += 'ontologies:[' + params.ontologySources.join(',') + ']'
        }
    }

    if(filter !== '') {
        filter = 'filter=' + filter
    }

    let submitRes = await fetch(apiUrl + '/services/map?' + filter, {
        method: 'POST',
        body: JSON.stringify(params.properties),
        credentials: 'include',

        headers: {
            'content-type': 'application/json',
            'accept': 'text/plain'
        }
    })

    for(;;) {

        let progress = await status()

        onProgress(parseFloat(progress))

        if(progress === '1.0') {
            break
        }

        await delay(1000)
    }

    let [ jsonRes, tsvRes ] = await Promise.all([

        fetch(apiUrl + '/services/map?json', {
            method: 'GET',
            headers: {
                'accept': 'application/json'
            }
        }),

        fetch(apiUrl + '/services/map', {
            method: 'GET',
            headers: {
                'accept': 'text/plain'
            }
        })
    ])


    let body = await jsonRes.json()
    let tsv = await tsvRes.text()

    return {
        results: body.data.map((row:any) => {

            return {
                propertyType: row[0],
                propertyValue: row[1],
                ontologyTermLabel: row[2],
                ontologyTermSynonyms: row[3],
                mappingConfidence: row[4],
                ontologyTermID: row[5],
                ontologyURI: row[6],
                datasource: row[7]
            } as SearchResult
        }),
        tsv
    }
}

async function status():Promise<string> {

    let res = await fetch(apiUrl + '/services/map/status', {
        method: 'GET',
        headers: {
            'accept': 'application/json'
        }
    })

    return await res.text()
}


async function delay(ms:number) {
    return new Promise((res, rej) => {
        setTimeout(res, ms)
    })
}
