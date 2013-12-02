SELECT
  t2.id_repository
, t2.id_workflow
, t2.id_step
, t3.name as type
, t2.name
, t2.description
, t2.copy_nr
, t4.name as w_name
FROM retrosp_workflow t1, prosp_step t2, prosp_step_type t3, prosp_workflow t4
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 133
AND t4.id_repository = t1.id_prosp_repository
AND t4.id_workflow = t1.id_prosp_workflow 
AND t2.id_repository = t4.id_repository
AND t2.id_workflow = t4.id_workflow
AND t3.id_step_type = t2.id_step_type
AND NOT EXISTS (
	SELECT t5.*
	FROM prosp_hop t5
	WHERE t5.id_repository = t2.id_repository
	AND t5.id_workflow = t2.id_workflow
	AND t5.id_step_from = t2.id_step)
