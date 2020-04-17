-- --------------------------------------------------------
--
-- CREATE TABLE prosp_repository
--

CREATE TABLE prosp_repository (
  id_repository INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(255) NOT NULL,
  CONSTRAINT cstr_pr_pk PRIMARY KEY (id_repository)
);

-- --------------------------------------------------------
--
-- CREATE TABLE user
--

CREATE TABLE user (
  id_repository INT NOT NULL,
  id_user  INT NOT NULL,
  login VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4000),
  CONSTRAINT cstr_u_pk PRIMARY KEY (id_repository, id_user),
  CONSTRAINT cstr_u_fk_repository FOREIGN KEY (id_repository) REFERENCES prosp_repository (id_repository)
);

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_workflow
--

CREATE TABLE prosp_workflow (
  id_repository INT NOT NULL,
  id_workflow INT NOT NULL,  
  object_id VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4000),
  id_created_user INT NOT NULL,
  created_date TIMESTAMP NOT NULL,
  id_modified_user INT NOT NULL,
  modified_date TIMESTAMP NOT NULL,
  version_nr INT NOT NULL,
  id_root_repository INT NOT NULL,
  id_root INT NOT NULL,
  id_parent_repository INT NULL,
  id_parent INT NULL,  
  CONSTRAINT cstr_pp_pk PRIMARY KEY (id_repository, id_workflow),  
  CONSTRAINT cstr_pp_fk_repository FOREIGN KEY (id_repository) REFERENCES prosp_repository (id_repository),
  CONSTRAINT cstr_pp_fk_root FOREIGN KEY (id_root_repository, id_root) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_pp_fk_parent FOREIGN KEY (id_parent_repository, id_parent) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_pp_fk_created FOREIGN KEY (id_repository, id_created_user) REFERENCES user (id_repository, id_user),
  CONSTRAINT cstr_pp_fk_modified FOREIGN KEY (id_repository, id_modified_user) REFERENCES user (id_repository, id_user)
); 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_note
--

CREATE TABLE prosp_note (
  id_note INT NOT NULL,
  text VARCHAR(4000) NOT NULL,  
  CONSTRAINT cstr_pn_pk PRIMARY KEY (id_note)
); 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_workflow_note
--

CREATE TABLE prosp_workflow_note (
  id_repository INT NOT NULL,
  id_workflow INT NOT NULL,
  id_note INT NOT NULL,
  CONSTRAINT cstr_ppn_pk PRIMARY KEY (id_repository, id_workflow, id_note),
  CONSTRAINT cstr_ppn_fk_workflow FOREIGN KEY (id_repository, id_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_ppn_fk_note FOREIGN KEY (id_note) REFERENCES prosp_note (id_note)
); 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step_type
--

CREATE TABLE prosp_step_type (
  id_step_type INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  CONSTRAINT cstr_pst_pk PRIMARY KEY (id_step_type)
);


-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step
--

CREATE TABLE prosp_step (
  id_repository INT NOT NULL,
  id_workflow INT NOT NULL,
  id_step INT NOT NULL,
  id_step_type INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4000),
  copy_nr INT NOT NULL,
  CONSTRAINT cstr_ps_pk PRIMARY KEY (id_repository, id_workflow, id_step),
  CONSTRAINT cstr_ps_fk_workflow FOREIGN KEY (id_repository, id_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_ps_fk_step_type FOREIGN KEY (id_step_type) REFERENCES prosp_step_type (id_step_type)
);

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step_parameter
--

CREATE TABLE prosp_step_parameter (
  id_step_type INT NOT NULL,
  id_step_param INT NOT NULL,
  param_name VARCHAR(255) NOT NULL, 
  CONSTRAINT cstr_psp_pk PRIMARY KEY (id_step_type, id_step_param),
  CONSTRAINT cstr_psp_fk_step_type FOREIGN KEY (id_step_type) REFERENCES prosp_step_type (id_step_type)
);

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop
--

CREATE TABLE prosp_hop (  
  id_repository INT NOT NULL,
  id_workflow INT NOT NULL,
  id_step_from INT NOT NULL,
  id_step_to INT NOT NULL,
  enabled CHAR(1) NOT NULL DEFAULT 'N',
  evaluation CHAR(1) NOT NULL DEFAULT 'N',
  unconditional CHAR(1) NOT NULL DEFAULT 'N',
  CONSTRAINT cstr_ph_pk PRIMARY KEY (id_repository, id_workflow, id_step_from, id_step_to),  
  CONSTRAINT cstr_ph_fk_from FOREIGN KEY (id_repository, id_workflow, id_step_from) REFERENCES prosp_step (id_repository, id_workflow, id_step),
  CONSTRAINT cstr_ph_fk_to FOREIGN KEY (id_repository, id_workflow, id_step_to) REFERENCES prosp_step (id_repository, id_workflow, id_step)
);

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop_field
--

CREATE TABLE prosp_hop_field (
  id_repository INT NOT NULL,
  id_workflow INT NOT NULL,
  id_step_from INT NOT NULL,
  id_step_to INT NOT NULL,
  id_field INT NOT NULL,  
  field_name VARCHAR(255) NOT NULL,
  CONSTRAINT cstr_phf_pk PRIMARY KEY (id_repository, id_workflow, id_step_from, id_step_to, id_field), 
  CONSTRAINT cstr_phf_fk_hop FOREIGN KEY (id_repository, id_workflow, id_step_from, id_step_to) REFERENCES prosp_hop (id_repository, id_workflow, id_step_from, id_step_to)  
);

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_workflow
--

CREATE TABLE retrosp_workflow (
  id_prosp_repository INT NOT NULL,
  id_prosp_workflow INT NOT NULL,  
  id_workflow INT NOT NULL,  
  start_date TIMESTAMP NULL,
  finish_date TIMESTAMP NULL,
  success CHAR(1) NOT NULL DEFAULT 'N',
  id_user INT NOT NULL,  
  id_root_prosp_repository  INT NOT NULL,
  id_root_prosp_workflow  INT NOT NULL,
  id_root  INT NOT NULL,
  id_parent_prosp_repository INT NOT NULL,
  id_parent_prosp_workflow  INT NOT NULL,  
  id_parent  INT NULL,    
  CONSTRAINT cstr_rp_pk PRIMARY KEY  (id_prosp_repository, id_prosp_workflow, id_workflow),    
  CONSTRAINT cstr_rp_fk_prosp_workflow FOREIGN KEY (id_prosp_repository, id_prosp_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_rp_fk_user FOREIGN KEY (id_prosp_repository, id_user) REFERENCES user (id_repository, id_user),
  CONSTRAINT cstr_rp_fk_retrosp_root FOREIGN KEY (id_root_prosp_repository, id_root_prosp_workflow, id_root) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  CONSTRAINT cstr_rp_retrosp_parent FOREIGN KEY (id_parent_prosp_repository, id_parent_prosp_workflow, id_parent) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow)
);

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step
--

CREATE TABLE retrosp_step (
  id_prosp_repository INT NOT NULL,
  id_prosp_workflow INT NOT NULL,  
  id_workflow INT NOT NULL,   
  id_prosp_step INT NOT NULL,  
  seq INT NOT NULL,  
  start_date TIMESTAMP NULL,
  finish_date TIMESTAMP NULL,
  success CHAR(1) NOT NULL DEFAULT 'N',
  id_user INT NOT NULL,
  CONSTRAINT cstr_rs_pk PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),  
  CONSTRAINT cstr_rs_fk_workflow FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  CONSTRAINT cstr_rs_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step) REFERENCES prosp_step (id_repository, id_workflow, id_step),
  CONSTRAINT cstr_rs_fk_user FOREIGN KEY (id_prosp_repository, id_user) REFERENCES user (id_repository, id_user)
);

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step_parameter
--
                           
CREATE TABLE retrosp_step_parameter (
  id_prosp_repository INT NOT NULL,
  id_prosp_workflow INT NOT NULL,
  id_workflow INT NOT NULL,
  id_prosp_step INT NOT NULL,
  seq INT NOT NULL,
  id_prosp_step_type INT NOT NULL,
  id_prosp_step_param INT NOT NULL,
  param_value VARCHAR(4000) NULL,
  CONSTRAINT cstr_rsp_pk PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq, id_prosp_step_type, id_prosp_step_param),    
  CONSTRAINT cstr_rsp_fk_prosp_step_param FOREIGN KEY (id_prosp_step_type, id_prosp_step_param) REFERENCES prosp_step_parameter (id_step_type, id_step_param),
  CONSTRAINT cstr_rsp_fk_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),
  CONSTRAINT cstr_rsp_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step) REFERENCES prosp_step (id_repository, id_workflow, id_step)  
);


-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_row_field
--

CREATE TABLE retrosp_row_field (
  id_prosp_repository INT NOT NULL,
  id_prosp_workflow INT NOT NULL,
  id_prosp_step_from INT NOT NULL,
  id_prosp_step_to INT NOT NULL,
  id_prosp_field INT NOT NULL,  
  id_workflow INT NOT NULL,   
  seq_from INT NOT NULL,  
  seq_to INT NOT NULL,   
  row_count INT NOT NULL,
  field_value VARCHAR(4000),  
  CONSTRAINT cstr_rrf_pk PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field, id_workflow, seq_from, seq_to, row_count),
  CONSTRAINT cstr_rrf_fk_prosp_hop_field FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field) REFERENCES prosp_hop_field (id_repository, id_workflow, id_step_from, id_step_to, id_field),
  CONSTRAINT cstr_rrf_fk_step_from FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_from, seq_from) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),
  CONSTRAINT cstr_rrf_fk_step_to FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_to, seq_to) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq)
);

-- --------------------------------------------------------