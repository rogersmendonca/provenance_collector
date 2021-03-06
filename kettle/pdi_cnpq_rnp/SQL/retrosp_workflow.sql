SELECT
  t1.id_prosp_repository
, t1.id_prosp_workflow
, t1.id_workflow
, t1.start_date
, t1.finish_date
, t1.id_user
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
, t5.login
FROM retrosp_workflow t1, prosp_workflow t2, user t5
WHERE t1.id_root_prosp_repository = ?
AND t1.id_root_prosp_workflow = ?
AND t1.id_root = ?
AND t2.id_repository = t1.id_prosp_repository
AND t2.id_workflow = t1.id_prosp_workflow
AND t5.id_repository = t1.id_prosp_repository
AND t5.id_user = t1.id_user