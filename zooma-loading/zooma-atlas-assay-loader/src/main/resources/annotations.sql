select distinct e.ACCESSION as STUDY, a.ACCESSION as BIOENTITY, p.NAME as PROPERTY_TYPE, pv.NAME as PROPERTY_VALUE, ot.ACCESSION as SEMANTIC_TAG
from A2_EXPERIMENT e
inner join A2_ASSAY a on e.EXPERIMENTID = a.EXPERIMENTID
inner join A2_ASSAYPV apv on a.ASSAYID = apv.ASSAYID
inner join A2_PROPERTYVALUE pv on apv.PROPERTYVALUEID = pv.PROPERTYVALUEID
inner join A2_PROPERTY p on pv.PROPERTYID = p.PROPERTYID
left join A2_ASSAYPVONTOLOGY apvo on apv.ASSAYPVID = apvo.ASSAYPVID
inner join A2_ONTOLOGYTERM ot on apvo.ONTOLOGYTERMID = ot.ONTOLOGYTERMID