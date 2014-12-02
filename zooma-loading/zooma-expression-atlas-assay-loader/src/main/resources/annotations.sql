select STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, null as SEMANTIC_TAG from (
 select x.STUDY, x.BIOENTITY, x.PROPERTY_TYPE, x.PROPERTY_VALUE from (
  select distinct s.ACC as STUDY, m.NAME as BIOENTITY, p.NAME as PROPERTY_TYPE, pv.NAME as PROPERTY_VALUE
  from STUDY s
  inner join NODE n on s.ID = n.STUDY_ID
  inner join NODEFACTORVALUEMAP nfv on n.ID = nfv.NODE_ID
  inner join MATERIAL m on n.MATERIAL_ID = m.ID
  inner join PROPERTY_VALUE pv on nfv.ID = pv.NODEFACTORVALUEMAP_ID
  inner join PROPERTY p on pv.PROPERTY_ID = p.ID
  where pv.OBJ_TYPE = 'FV'
  and pv.NAME is not null) x, (
  select STUDY, BIOENTITY, PROPERTY_TYPE, count(PROPERTY_TYPE) as FREQ from (
   select distinct s.ACC as STUDY, m.NAME as BIOENTITY, p.NAME as PROPERTY_TYPE, pv.NAME as PROPERTY_VALUE
   from STUDY s
   inner join NODE n on s.ID = n.STUDY_ID
   inner join NODEFACTORVALUEMAP nfv on n.ID = nfv.NODE_ID
   inner join MATERIAL m on n.MATERIAL_ID = m.ID
   inner join PROPERTY_VALUE pv on nfv.ID = pv.NODEFACTORVALUEMAP_ID
   inner join PROPERTY p on pv.PROPERTY_ID = p.ID
   where pv.OBJ_TYPE = 'FV'
   and pv.NAME is not null)
  group by STUDY, BIOENTITY, PROPERTY_TYPE) y
 where x.STUDY = y.STUDY
 and x.BIOENTITY = y.BIOENTITY
 and x.PROPERTY_TYPE = y.PROPERTY_TYPE
 and y.FREQ < 2)