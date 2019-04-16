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
# from urllib2 import *  # Might need if a requests post doesn't work ...


def main():

    #  'neo4j' and 'zooma-solr' apply only inside the docker network, but are
    #  portable across hosts, and the preferred option
    
    stackhost = {'neo': 'neo4j',
                 'solr': 'solr',
                 'zooma_solr': 'zooma-solr'}
    
    driver = GraphDatabase.driver("bolt://%s:7687" % stackhost['neo'])
    
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

    print('Running Cypher query ...')
    results = session.run (cypher_query)

    
    annotations = {}

    print('Populating annotations object ...')
    for result in results:
        # print(result)
        bioentity = result[0]
        propertyType = result[1]
        propertyValue = result[2]
        semanticTag = result[3]
    
        if bioentity not in annotations:
            annotations[bioentity] = {'properties' : [], 'tags' : []}
    
        property = {'propertyType' : propertyType, 'propertyValue': propertyValue}
        annotations[bioentity]['properties'].append(property)
    
        if semanticTag:
            tag = {'propertyType': propertyType, 'propertyValue': propertyValue, 'semanticTag': semanticTag}
            annotations[bioentity]['tags'].append(tag)


    transactions = []

    print('Populating transactions array ...')
    for key, value in annotations.items():
    
        for tag in value['tags']:
            entry = []
            for property in annotations[key]['properties']:
                entry.append(property['propertyType'] + "|" + property['propertyValue'])
    
            entry.append("TAG|"+tag['propertyType']+"|"+tag['propertyValue']+"|"+tag['semanticTag'])
    
            # print (entry)
            transactions.append(entry)
    
    
    # transactions = [
    #     ('OriginTissue : Blood', 'TumorType : Primary', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = OriginTissue : Blood : UBERON_0000178'),
    #     ('OriginTissue : Blood', 'TumorType : Primary', 'SampleDiagnosis : acute myeloid leukemia','TAG = SampleDiagnosis : acute myeloid leukemia : NCIT_C3171'),
    #     ('OriginTissue : Blood', 'TumorType : Primary', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = TumorType : Primary : UBERON_0002371'),
    #     ('OriginTissue : Blood', 'TumorType : Metastatic', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = OriginTissue : Blood : UBERON_0000178'),
    #     ('OriginTissue : Blood', 'TumorType : Metastatic', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = SampleDiagnosis : Metastatic acute myeloid leukemia : EFO_00002'),
    #     ('OriginTissue : Blood', 'TumorType : Metastatic', 'SampleDiagnosis : acute myeloid leukemia', 'TAG = TumorType : Primary : ONTO_XXXX')
    # ]

    print("Building rules...")
    itemsets, rules = apriori(transactions, min_support=0, min_confidence=0)
    
    #  Print out every rule with two items on the left hand side and one item on
    #  the right hand side, sorted by lift
    rules_rhs = filter(lambda rule:  len(rule.rhs) == 1, rules)

    #  Assign root of core URLs to correct node (it's a format string because we
    #  go on to use 2 distinct ports)
    recommender_core = 'http://%%s:%%s/%%s/recommendations' % ()

    #  Empty the existing core
    emptier_url = '%s/update?stream.body=<delete><query>*.*</query></delete>&commit=true' % (recommender_core % (stackhost['solr'], '8983', 'solr'))
    # print('emptier_url set to: %s' % emptier_url)
    print('Emptying pre-existing rules from recommender core ...')
    requests.get(emptier_url)

    documents = []
    
    doc_cnt = 0
    print('Writing new rules to recommender core ...')
    for rule in sorted(rules_rhs, key=lambda rule: rule.lift):
        if "TAG" in rule.rhs[0]:
    
            doc = {
                'propertiesType': [],
                'propertiesValue' : [],
                'propertiesTypeTag' : "",
                'propertiesValueTag' : "",
                'tag' : "",
                'conf' : rule.confidence,
                'support' : rule.support,
                "lift" : rule.lift,
                "conviction" : rule.conviction
            }
            for lr in rule.lhs:
                type, value = lr.split("|")

                doc['propertiesType'].append(type)
                doc['propertiesValue'].append(value)

            # print(rule)  # Prints the rule and its confidence, support, lift, ...
    
            ignore, stype, svalue, stag = rule.rhs[0].split("|")
            doc['propertiesTypeTag'] = stype
            doc['propertiesValueTag'] = svalue
            doc['tag'] = stag
            documents.append(doc)

            # print(json.dumps(doc))
            requests.post(recommender_core % (stackhost['zooma_solr'], '8080', ''), json.dumps(doc))

    rule_file = 'rules.json'
    print('Saving new rules to %s ...' % rule_file)
    with open(rule_file, 'w') as outfile:
        json.dump(documents, outfile, indent=4, )

    print('Completed.')
    return "all fine"


if __name__ == "__main__":
    main()
