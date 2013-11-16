SELECT
  t2.id_repository
, t2.id_workflow
, t2.id_step_from
, t2.id_step_to
, t2.id_field
, t2.field_name
, t3.name as w_name
, t4.name as from_name
, t5.name as to_name
FROM retrosp_workflow t1, prosp_hop_field t2, prosp_workflow t3, prosp_step t4, prosp_step t5
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 133
AND t2.id_repository = t1.id_prosp_repository
AND t2.id_workflow = t1.id_prosp_workflow
AND t3.id_repository = t2.id_repository
AND t3.id_workflow = t2.id_workflow
AND t4.id_repository = t2.id_repository
AND t4.id_workflow = t2.id_workflow
AND t4.id_step = t2.id_step_from
AND t5.id_repository = t2.id_repository
AND t5.id_workflow = t2.id_workflow
AND t5.id_step = t2.id_step_to