select distinct STUDY, BIOENTITY, BIOENTITY_ID, BIOENTITY_URI, BIOENTITY_TYPE_NAME, PROPERTY_TYPE, PROPERTY_VALUE, PROPERTY_ID, SEMANTIC_TAG, ANNOTATION_DATE
from (
 select a.pubmed_id as STUDY, g.symbol as BIOENTITY, CONCAT('gene', g.gene_id) as BIOENTITY_ID, null as BIOENTITY_URI, 'gene' as BIOENTITY_TYPE_NAME, 'phenotype' as PROPERTY_TYPE, og.group_name as PROPERTY_VALUE, CONCAT('OMIA', og.omia_id) as PROPERTY_ID, CONCAT('http://omia.angis.org.au/OMIA', p.omia_id, '/', p.gb_species_id) as SEMANTIC_TAG, p.date_modified as ANNOTATION_DATE
 from Phene p
 left join OMIA_Group og on p.omia_id = og.omia_id
 left join Article_Phene ap on p.phene_id = ap.phene_id
 left join Articles a on ap.article_id = a.article_id
 left join Phene_Gene pg on p.phene_id = pg.phene_id
 left join Genes_gb g on pg.gene_id = g.gene_id
 where g.gene_id is not null
 union
 select a.pubmed_id as STUDY, g.symbol as BIOENTITY, CONCAT('gene', g.gene_id) as BIOENTITY_ID, null as BIOENTITY_URI,  'gene' as BIOENTITY_TYPE_NAME, 'species' as PROPERTY_TYPE, s.sci_name as PROPERTY_VALUE, null as PROPERTY_ID, CONCAT('http://purl.org/obo/owl/NCBITaxon#NCBITaxon_', s.gb_species_id) as SEMANTIC_TAG, p.date_modified as ANNOTATION_DATE
 from Phene p
 left join Species_gb s on p.gb_species_id = s.gb_species_id
 left join Article_Phene ap on p.phene_id = ap.phene_id
 left join Articles a on ap.article_id = a.article_id
 left join Phene_Gene pg on p.phene_id = pg.phene_id
 left join Genes_gb g on pg.gene_id = g.gene_id
 where g.gene_id is not null
 union
 select a.pubmed_id as STUDY, p.symbol as BIOENTITY, CONCAT('phene', p.phene_id) as BIOENTITY_ID, null as BIOENTITY_URI, 'phenotype' as BIOENTITY_TYPE_NAME, 'phenotype' as PROPERTY_TYPE, og.group_name as PROPERTY_VALUE, CONCAT('OMIA', og.omia_id) as PROPERTY_ID, CONCAT('http://omia.angis.org.au/OMIA', p.omia_id, '/', p.gb_species_id) as SEMANTIC_TAG, p.date_modified as ANNOTATION_DATE
 from Phene p
 left join OMIA_Group og on p.omia_id = og.omia_id
 left join Article_Phene ap on p.phene_id = ap.phene_id
 left join Articles a on ap.article_id = a.article_id
 left join Phene_Gene pg on p.phene_id = pg.phene_id
 left join Genes_gb g on pg.gene_id = g.gene_id
 where g.gene_id is null
 union
 select a.pubmed_id as STUDY, p.symbol as BIOENTITY, CONCAT('phene', p.phene_id) as BIOENTITY_ID, null as BIOENTITY_URI, 'phenotype' as BIOENTITY_TYPE_NAME, 'species' as PROPERTY_TYPE, s.sci_name as PROPERTY_VALUE, null as PROPERTY_ID, CONCAT('http://purl.org/obo/owl/NCBITaxon#NCBITaxon_', s.gb_species_id) as SEMANTIC_TAG, p.date_modified as ANNOTATION_DATE
 from Phene p
 left join Species_gb s on p.gb_species_id = s.gb_species_id
 left join Article_Phene ap on p.phene_id = ap.phene_id
 left join Articles a on ap.article_id = a.article_id
 left join Phene_Gene pg on p.phene_id = pg.phene_id
 left join Genes_gb g on pg.gene_id = g.gene_id
 where g.gene_id is null
 ) as annotations