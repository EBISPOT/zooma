import { ZoomaDatasources } from "./ZoomaDatasources"

export interface ZoomaDatasourceConfig {

    doNotSearchDatasources:boolean

    excludedDatasources:string[]
    unrankedDatasources:string[]
    rankedDatasources:string[]

    doNotSearchOntologies:boolean
    ontologySources:string[]

}
