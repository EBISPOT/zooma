
export interface SearchProperty {
}

export interface SearchParams {
    properties: { 
        propertyValue: string
        propertyType: string
    }[]
    requiredSources:string[]
    preferredSources:string[]
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

    if(params.requiredSources.length > 0) {
        filter += 'required:[' + params.requiredSources.join(',') + ']'
    }

    if(params.preferredSources.length > 0) {
        filter += 'preferred:[' + params.preferredSources.join(',') + ']'
    }

    if(params.ontologySources.length > 0) {
        filter += 'ontologies:[' + params.ontologySources.join(',') + ']'
    }

    let submitRes = await fetch('/api/map?' + filter, {
        method: 'POST',
        body: JSON.stringify(params.properties),
        credentials: 'include',

        headers: {
            'content-type': 'application/json'
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

    let finalRes = await fetch('/api/map-results', {
        method: 'GET'
    })

    let body = await finalRes.json()

    return body.data.map(row => {

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
    })
}

async function status():Promise<string> {

    let res = await fetch('/api/status', {
        method: 'GET'
    })

    return await res.text()
}


async function delay(ms:number) {
    return new Promise((res, rej) => {
        setTimeout(res, ms)
    })
}
