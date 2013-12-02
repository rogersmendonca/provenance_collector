SELECT
  t2.id_prosp_repository
, t2.id_prosp_workflow
, t2.id_workflow
, t2.id_prosp_step
, t2.seq
, t2.start_date
, t2.finish_date
, t2.success
, t2.id_user
, t3.name
, t4.login
, t5.name as w_name
FROM retrosp_workflow t1, retrosp_step t2, prosp_step t3, user t4, prosp_workflow t5
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 294
AND t2.id_prosp_repository = t1.id_prosp_repository
AND t2.id_prosp_workflow = t1.id_prosp_workflow
AND t2.id_workflow = t1.id_workflow
AND t3.id_repository = t2.id_prosp_repository
AND t3.id_workflow = t2.id_prosp_workflow
AND t3.id_step = t2.id_prosp_step
AND t4.id_repository = t2.id_prosp_repository
AND t4.id_user = t2.id_user
AND t5.id_repository = t3.id_repository
AND t5.id_workflow = t3.id_workflow
ORDER BY t2.id_prosp_repository, t2.id_prosp_workflow, t2.id_workflow, t2.seq