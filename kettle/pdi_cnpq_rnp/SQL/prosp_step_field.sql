SELECT t1.id_repository, t1.id_workflow, t1.id_step_from, t1.id_step_to, t1.id_field, t1.field_name, t2.name AS w_name, t4.name AS from_name, t5.name AS to_name
FROM prosp_hop_field t1, prosp_workflow t2, retrosp_workflow t3, prosp_step t4, prosp_step t5
WHERE t1.id_repository = t2.id_repository
AND t1.id_workflow = t2.id_workflow
AND t1.id_repository = t4.id_repository
AND t1.id_workflow = t4.id_workflow
AND t1.id_step_from = t4.id_step
AND t1.id_repository = t5.id_repository
AND t1.id_workflow = t5.id_workflow
AND t1.id_step_to = t5.id_step
AND t2.id_root_repository = t3.id_prosp_repository
AND t2.id_root = t3.id_prosp_workflow
AND t3.id_prosp_repository =1
AND t3.id_workflow =307