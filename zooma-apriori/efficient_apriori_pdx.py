#!/usr/bin/env python3
"""
Description goes here
"""
__author__ = "jupp"
__license__ = "Apache 2.0"
__date__ = "03/10/2018"

from efficient_apriori import apriori
from neo4j import GraphDatabase, basic_auth
import json
import requests


def main():
    
    driver = GraphDatabase.driver("bolt://scrappy.ebi.ac.uk:7687")
    # driver = GraphDatabase.driver("bolt://neo4j:7687")
    
    cypher_query = """
    MATCH (a)-[:HAS_PROVENANCE]->(s:Source)
    WHERE s.name = 'pdx-finder'
    WITH a
    MATCH (be:BiologicalEntity)<-[:HAS_BIO_ENTITY]-(a:Annotation)-[:HAS_PROPERTY]->(p:Property)
    OPTIONAL MATCH (a)-[:HAS_SEMANTIC_TAG]->(st:SemanticTag)
    RETURN distinct be.bioEntity, p.propertyType, p.propertyValue, st.semanticTag
    ORDER BY be.bioEntity
    ;"""
    
    session = driver.session()
    
    results = session.run (cypher_query)
    
    annotations = {}
    
    for result in results:
        # print(result)
        bioentity = result[0]
        propertyType = result[1]
        propertyValue = result[2]
        semanticTag = result[3]
    
        if bioentity not in annotations:
            annotations[bioentity] = {'properties' : [], 'tags' : []}
    
        property = { 'propertyType' : propertyType, 'propertyValue' : propertyValue}
        annotations[bioentity]['properties'].append(property)
    
        if semanticTag:
            tag = { 'propertyType' : propertyType, 'propertyValue' : propertyValue, 'semanticTag' : semanticTag}
            annotations[bioentity]['tags'].append(tag)
    
    transactions = []
    for key, value in annotations.items():
    
        for tag in value['tags']:
            entry = []
            for property in annotations[key]['properties']:
                entry.append(property['propertyType'] + "|" + property['propertyValue'])
    
            entry.append("TAG|"+tag['propertyType']+"|"+tag['propertyValue']+"|"+tag['semanticTag'])
    
            # print (entry)
            transactions.append(entry)
    
    
    # transactions = [('OriginTissue : Blood', 'TumorType : Primary', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = OriginTissue : Blood : UBERON_0000178'),
    #                ('OriginTissue : Blood', 'TumorType : Primary', 'SampleDiagnosis : acute myeloid leukemia','TAG = SampleDiagnosis : acute myeloid leukemia : NCIT_C3171'),
    #                ('OriginTissue : Blood', 'TumorType : Primary', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = TumorType : Primary : UBERON_0002371')
    #                 ,
    #                ('OriginTissue : Blood', 'TumorType : Metastatic', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = OriginTissue : Blood : UBERON_0000178'),
    #                ('OriginTissue : Blood', 'TumorType : Metastatic', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = SampleDiagnosis : Metastatic acute myeloid leukemia : EFO_00002'),
    #                ('OriginTissue : Blood', 'TumorType : Metastatic', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = TumorType : Primary : ONTO_XXXX')
    #
    #                ]
    itemsets, rules = apriori(transactions, min_support=0, min_confidence=0)
    
    
    # Print out every rule with 2 items on the left hand side,
    # 1 item on the right hand side, sorted by lift
    rules_rhs = filter(lambda rule:  len(rule.rhs) == 1, rules)
    
    documents = []
    
    doc_cnt = 0
    for rule in sorted(rules_rhs, key=lambda rule: rule.lift):
        if "TAG" in rule.rhs[0]:
    
            doc = {'properties' : [], 'conf' : rule.confidence, 'support' : rule.support, "lift" : rule.lift, "conviction" : rule.conviction}
            for lr in rule.lhs:
                type, value = lr.split("|")
                property = {'propertyType' : type, 'propertyValue' : value}
                doc['properties'].append(property)
    
            # print(rule) # Prints the rule and its confidence, support, lift, ...
    
            ignore, stype, svalue, stag = rule.rhs[0].split("|")
            doc['tag'] = {'propertyType' : stype, 'propertyValue' : svalue, 'tag' : stag}
            documents.append(doc)
            if doc_cnt < 4:
                print(json.dumps(doc))
                # requests.post('http://scrappy:8081/recommendations', json.dumps(doc))
                # requests.post('http://localhost:8081/recommendations', data=doc)
                
            doc_cnt += 1
    
    with open('rules.json', 'w') as outfile:
        json.dump(documents, outfile, indent=4, )

    return "all fine"


if __name__ == "__main__":
    main()
