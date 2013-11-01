SELECT
  t2.id_repository
, t2.id_workflow
, t2.object_id
, t2.name
, t2.description
, t2.id_created_user
, t3.login as created_login
, t2.created_date
, t2.id_modified_user
, t4.login as modified_login
, t2.modified_date
, t2.version_nr
, t2.id_root_repository
, t2.id_root
, (SELECT t5.name FROM prosp_workflow t5 WHERE t5.id_repository = t2.id_root_repository AND t5.id_workflow = t2.id_root) as root_name
, t2.id_parent_repository
, t2.id_parent
, (SELECT t6.name FROM prosp_workflow t6 WHERE t6.id_repository = t2.id_parent_repository AND t6.id_workflow = t2.id_parent) as parent_name
, t7.location
FROM retrosp_workflow t1, prosp_workflow t2, user t3, user t4, prosp_repository t7
WHERE t1.id_root_prosp_repository = 1
AND t1.id_root_prosp_workflow = 1
AND t1.id_root = 25
AND t2.id_repository = t1.id_prosp_repository
AND t2.id_workflow = t1.id_prosp_workflow
AND t2.id_repository = t3.id_repository
AND t2.id_created_user = t3.id_user
AND t2.id_repository = t4.id_repository
AND t2.id_modified_user = t4.id_user
AND t7.id_repository = t2.id_repository