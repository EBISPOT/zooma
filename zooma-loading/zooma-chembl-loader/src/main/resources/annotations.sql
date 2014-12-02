select * from (
 select distinct d.PUBMED_ID as STUDY,  a.CHEMBL_ID as BIOENTITY, 'CELL_LINE' as PROPERTY_TYPE, c.CELL_NAME as PROPERTY_VALUE, concat('http://www.ebi.ac.uk/efo/',c.EFO_ID) as SEMANTIC_TAG
 from CHEMBL_18.DOCS d
 join CHEMBL_18.ASSAYS a on d.DOC_ID=a.DOC_ID
 join CHEMBL_18.CELL_DICTIONARY c on a.CELL_ID=c.CELL_ID
 where c.EFO_ID is not null
 and a.CHEMBL_ID is not null
 and c.CELL_NAME is not null
 union
  select distinct d.PUBMED_ID as STUDY,  a.CHEMBL_ID as BIOENTITY, 'CELL_LINE' as PROPERTY_TYPE, c.CELL_NAME as PROPERTY_VALUE, concat('http://purl.obolibrary.org/obo/',c.CLO_ID) as SEMANTIC_TAG
  from CHEMBL_18.DOCS d
  join CHEMBL_18.ASSAYS a on d.DOC_ID=a.DOC_ID
  join CHEMBL_18.CELL_DICTIONARY c on a.CELL_ID=c.CELL_ID
  where c.CLO_ID is not null
  and a.CHEMBL_ID is not null
  and c.CELL_NAME is not null)