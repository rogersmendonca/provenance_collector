SELECT
  t2.id_repository
, t2.id_workflow
, t2.id_step
, t2.type
, t2.name
, t2.description
, t2.copy_nr
, t3.name as w_name
FROM retrosp_workflow t1, prosp_step t2, prosp_workflow t3
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 71
AND t3.id_repository = t1.id_prosp_repository
AND t3.id_workflow = t1.id_prosp_workflow 
AND t2.id_repository = t3.id_repository
AND t2.id_workflow = t3.id_workflow