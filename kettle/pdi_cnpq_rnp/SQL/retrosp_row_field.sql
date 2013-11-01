SELECT
  t2.id_prosp_repository
, t2.id_prosp_workflow
, t2.id_prosp_step_from
, t2.id_prosp_step_to
, t2.id_prosp_field
, t2.id_workflow
, t2.seq_from
, t2.seq_to
, t2.row_count
, t2.field_value
, t3.name as name_from
, t4.name as name_to
, t5.field_name
, t6.name as w_name
FROM retrosp_workflow t1, retrosp_row_field t2, prosp_step t3, prosp_step t4, prosp_hop_field t5, prosp_workflow t6
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 7
AND t2.id_prosp_repository = t1.id_prosp_repository
AND t2.id_prosp_workflow = t1.id_prosp_workflow
AND t2.id_workflow = t1.id_workflow
AND t3.id_repository = t2.id_prosp_repository
AND t3.id_workflow = t2.id_prosp_workflow
AND t3.id_step = t2.id_prosp_step_from
AND t4.id_repository = t2.id_prosp_repository
AND t4.id_workflow = t2.id_prosp_workflow
AND t4.id_step = t2.id_prosp_step_to
AND t5.id_repository = t2.id_prosp_repository
AND t5.id_workflow = t2.id_prosp_workflow
AND t5.id_step_from = t2.id_prosp_step_from
AND t5.id_step_to = t2.id_prosp_step_to
AND t5.id_field = t2.id_prosp_field
AND t6.id_repository = t2.id_prosp_repository
AND t6.id_workflow = t2.id_prosp_workflow

