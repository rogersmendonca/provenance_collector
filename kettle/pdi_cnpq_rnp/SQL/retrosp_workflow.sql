SELECT
  t1.id_prosp_repository
, t1.id_prosp_workflow
, t1.id_workflow
, t1.start_date
, t1.finish_date
, t1.success
, t1.id_root_prosp_repository
, t1.id_root_prosp_workflow
, t1.id_root
, (SELECT t3.name FROM prosp_workflow t3 WHERE t3.id_repository = t1.id_root_prosp_repository AND t3.id_workflow = t1.id_root_prosp_workflow) as root_name
, t1.id_parent_prosp_repository
, t1.id_parent_prosp_workflow
, t1.id_parent
, (SELECT t4.name FROM prosp_workflow t4 WHERE t4.id_repository = t1.id_parent_prosp_repository AND t4.id_workflow = t1.id_parent_prosp_workflow) as parent_name
, t2.name
FROM retrosp_workflow t1, prosp_workflow t2
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 7
AND t2.id_repository = t1.id_prosp_repository
AND t2.id_workflow = t1.id_prosp_workflow