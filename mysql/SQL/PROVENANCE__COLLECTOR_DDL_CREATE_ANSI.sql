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
-- CREATE TABLE prosp_process
--

CREATE TABLE prosp_process (
  id_repository INT NOT NULL,
  id_process INT NOT NULL,  
  object_id VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4000),
  created_user VARCHAR(255),
  created_date TIMESTAMP NOT NULL,
  modified_user VARCHAR(255),
  modified_date TIMESTAMP NOT NULL,
  id_root_repository INT NOT NULL,
  id_root INT NOT NULL,
  id_parent_repository INT NULL,
  id_parent INT NULL,  
  CONSTRAINT cstr_pp_pk PRIMARY KEY (id_repository, id_process),  
  CONSTRAINT cstr_pp_fk_repository FOREIGN KEY (id_repository) REFERENCES prosp_repository (id_repository),
  CONSTRAINT cstr_pp_fk_root FOREIGN KEY (id_root_repository, id_root) REFERENCES prosp_process (id_repository, id_process),
  CONSTRAINT cstr_pp_fk_parent FOREIGN KEY (id_parent_repository, id_parent) REFERENCES prosp_process (id_repository, id_process)
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
-- CREATE TABLE prosp_process_note
--

CREATE TABLE prosp_process_note (
  id_repository INT NOT NULL,
  id_process INT NOT NULL,
  id_note INT NOT NULL,
  CONSTRAINT cstr_ppn_pk PRIMARY KEY (id_repository, id_process, id_note),
  CONSTRAINT cstr_ppn_fk_process FOREIGN KEY (id_repository, id_process) REFERENCES prosp_process (id_repository, id_process),
  CONSTRAINT cstr_ppn_fk_note FOREIGN KEY (id_note) REFERENCES prosp_note (id_note)
); 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step
--

CREATE TABLE prosp_step (
  id_repository INT NOT NULL,
  id_process INT NOT NULL,
  id_step INT NOT NULL,
  type VARCHAR(255) NOT NULL,  
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4000),
  nr INT NOT NULL,
  CONSTRAINT cstr_ps_pk PRIMARY KEY (id_repository, id_process, id_step),
  CONSTRAINT cstr_ps_fk_process FOREIGN KEY (id_repository, id_process) REFERENCES prosp_process (id_repository, id_process)
);

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step_parameter
--

CREATE TABLE prosp_step_parameter (
  id_repository INT NOT NULL,
  id_process INT NOT NULL,
  id_step INT NOT NULL,  
  id_step_param INT NOT NULL,      
  name VARCHAR(255) NOT NULL, 
  value VARCHAR(4000),  
  CONSTRAINT cstr_psp_pk PRIMARY KEY (id_repository, id_process, id_step, id_step_param),
  CONSTRAINT cstr_psp_fk_step FOREIGN KEY (id_repository, id_process, id_step) REFERENCES prosp_step (id_repository, id_process, id_step)
);

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop
--

CREATE TABLE prosp_hop (  
  id_repository INT NOT NULL,
  id_process INT NOT NULL,
  id_step_from INT NOT NULL,
  id_step_to INT NOT NULL,
  enabled CHAR(1) NOT NULL DEFAULT 'N',
  evaluation CHAR(1) NOT NULL DEFAULT 'N',
  unconditional CHAR(1) NOT NULL DEFAULT 'N',
  CONSTRAINT cstr_ph_pk PRIMARY KEY (id_repository, id_process, id_step_from, id_step_to),  
  CONSTRAINT cstr_ph_fk_from FOREIGN KEY (id_repository, id_process, id_step_from) REFERENCES prosp_step (id_repository, id_process, id_step),
  CONSTRAINT cstr_ph_fk_to FOREIGN KEY (id_repository, id_process, id_step_to) REFERENCES prosp_step (id_repository, id_process, id_step)
);

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop_field
--

CREATE TABLE prosp_hop_field (
  id_repository INT NOT NULL,
  id_process INT NOT NULL,
  id_step_from INT NOT NULL,
  id_step_to INT NOT NULL,
  id_field INT NOT NULL,  
  field_name VARCHAR(255) NOT NULL,
  CONSTRAINT cstr_phf_pk PRIMARY KEY (id_repository, id_process, id_step_from, id_step_to, id_field), 
  CONSTRAINT cstr_phf_fk_hop FOREIGN KEY (id_repository, id_process, id_step_from, id_step_to) REFERENCES prosp_hop (id_repository, id_process, id_step_from, id_step_to)  
);

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_process
--

CREATE TABLE retrosp_process (
  id_prosp_repository INT NOT NULL,
  id_prosp_process INT NOT NULL,  
  id_process INT NOT NULL,  
  start_date TIMESTAMP NULL,
  finish_date TIMESTAMP NULL,
  user VARCHAR(255),
  success CHAR(1) NOT NULL DEFAULT 'N',
  CONSTRAINT cstr_rp_pk PRIMARY KEY (id_prosp_repository, id_prosp_process, id_process),  
  CONSTRAINT cstr_rp_fk_prosp_process FOREIGN KEY (id_prosp_repository, id_prosp_process) REFERENCES prosp_process (id_repository, id_process)
);

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step
--

CREATE TABLE retrosp_step (
  id_prosp_repository INT NOT NULL,
  id_prosp_process INT NOT NULL,  
  id_process INT NOT NULL,   
  id_prosp_step INT NOT NULL,  
  seq INT NOT NULL,  
  start_date TIMESTAMP NULL,
  finish_date TIMESTAMP NULL,
  success CHAR(1) NOT NULL DEFAULT 'N',
  CONSTRAINT cstr_rs_pk PRIMARY KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq),  
  CONSTRAINT cstr_rs_fk_process FOREIGN KEY (id_prosp_repository, id_prosp_process, id_process) REFERENCES retrosp_process (id_prosp_repository, id_prosp_process, id_process),
  CONSTRAINT cstr_rs_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_process, id_prosp_step) REFERENCES prosp_step (id_repository, id_process, id_step)
);

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_row_field
--

CREATE TABLE retrosp_row_field (
  id_prosp_repository INT NOT NULL,
  id_prosp_process INT NOT NULL,
  id_prosp_step_from INT NOT NULL,
  id_prosp_step_to INT NOT NULL,
  id_prosp_field INT NOT NULL,  
  id_process INT NOT NULL,   
  seq_from INT NOT NULL,  
  seq_to INT NOT NULL,   
  row_count INT NOT NULL,
  field_value VARCHAR(4000),  
  CONSTRAINT cstr_rrf_pk PRIMARY KEY (id_prosp_repository, id_prosp_process, id_prosp_step_from, id_prosp_step_to, id_prosp_field, id_process, seq_from, seq_to, row_count),
  CONSTRAINT cstr_rrf_fk_prosp_hop_field FOREIGN KEY (id_prosp_repository, id_prosp_process, id_prosp_step_from, id_prosp_step_to, id_prosp_field) REFERENCES prosp_hop_field (id_repository, id_process, id_step_from, id_step_to, id_field),
  CONSTRAINT cstr_rrf_fk_step_from FOREIGN KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step_from, seq_from) REFERENCES retrosp_step (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq),
  CONSTRAINT cstr_rrf_fk_step_to FOREIGN KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step_to, seq_to) REFERENCES retrosp_step (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq)
);

-- --------------------------------------------------------