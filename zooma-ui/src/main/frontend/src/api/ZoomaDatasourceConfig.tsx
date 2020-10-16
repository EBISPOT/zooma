import { ZoomaDatasources } from "./ZoomaDatasources"

export interface ZoomaDatasourceConfig {

    doNotSearchDatasources:boolean
    unrankedDatasources:string[]
    rankedDatasources:string[]

    doNotSearchOntologies:boolean
    ontologySources:string[]

}
