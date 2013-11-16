SELECT 
  t2.id_step_type
, t2.id_step_param
, t2.param_name
, t4.id_repository
, t4.id_workflow
, t4.name as w_name
, t3.id_step
, t3.name as s_name
FROM retrosp_workflow t1, prosp_step_parameter t2, prosp_step t3, prosp_workflow t4
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 133
AND t4.id_repository = t1.id_prosp_repository
AND t4.id_workflow = t1.id_prosp_workflow
AND t3.id_repository = t4.id_repository
AND t3.id_workflow = t4.id_workflow
AND t2.id_step_type = t3.id_step_type