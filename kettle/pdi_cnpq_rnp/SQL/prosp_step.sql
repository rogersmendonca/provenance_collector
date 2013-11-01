SELECT
  t1.id_repository
, t1.id_workflow
, t1.id_step
, t1.type
, t1.name
, t1.description
, t1.copy_nr
, t2.name
FROM prosp_step t1, prosp_workflow_t2, retrosp_workflow t3
WHERE t1.id_repository = t2.id_repository
AND t1.id_workflow = t2.id_workflow
AND t2.id_root_repository = t3.id_prosp_repository
AND t2.id_root = t3.id_prosp_workflow
AND t3.id_prosp_repository = 1
AND t3.id_workflow = 198