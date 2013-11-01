SELECT
  t2.id_prosp_repository
, t2.id_prosp_workflow
, t2.id_workflow
, t2.id_prosp_step
, t2.seq
, t2.id_prosp_step_param
, t2.param_value
, t3.param_name
FROM retrosp_workflow t1, retrosp_step_parameter t2, prosp_step_parameter t3
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 7
AND t2.id_prosp_repository = t1.id_prosp_repository
AND t2.id_prosp_workflow = t1.id_prosp_workflow
AND t2.id_workflow = t1.id_workflow
AND t3.id_repository = t2.id_prosp_repository
AND t3.id_workflow = t2.id_prosp_workflow
AND t3.id_step = t2.id_prosp_step
AND t3.id_step_param = t2.id_prosp_step_param