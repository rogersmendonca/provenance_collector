SELECT
  t2.text
, t4.id_repository
, t4.id_workflow
, t4.name
FROM retrosp_workflow t1, prosp_note t2, prosp_workflow_note t3, prosp_workflow t4
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 71
AND t4.id_repository = t1.id_prosp_repository
AND t4.id_workflow = t1.id_prosp_workflow 
AND t3.id_repository = t4.id_repository
AND t3.id_workflow = t4.id_workflow
AND t2.id_note = t3.id_note