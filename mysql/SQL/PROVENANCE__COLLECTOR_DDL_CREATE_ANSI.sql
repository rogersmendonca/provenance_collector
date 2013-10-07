-- --------------------------------------------------------
--
-- CREATE TABLE prosp_repository
--

CREATE TABLE prosp_repository (
  id_repository INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(255) NOT NULL,
  CONSTRAINT cstr_prosp_repository_pk PRIMARY KEY (id_repository)
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
  description text,
  created_user VARCHAR(255),
  created_TIMESTAMP TIMESTAMP NOT NULL,
  modified_user VARCHAR(255),
  modified_TIMESTAMP TIMESTAMP NOT NULL,
  id_root_repository INT NOT NULL,
  id_root INT NOT NULL,
  id_parent_repository INT NULL,
  id_parent INT NULL,  
  CONSTRAINT cstr_prosp_process_pk PRIMARY KEY (id_repository, id_process),  
  CONSTRAINT cstr_prosp_process_fk_repository FOREIGN KEY (id_repository) REFERENCES prosp_repository (id_repository),
  CONSTRAINT cstr_prosp_process_fk_root FOREIGN KEY (id_root_repository, id_root) REFERENCES prosp_process (id_repository, id_process),
  CONSTRAINT cstr_prosp_process_fk_parent FOREIGN KEY (id_parent_repository, id_parent) REFERENCES prosp_process (id_repository, id_process)
); 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_note
--

CREATE TABLE prosp_note (
  id_note INT NOT NULL,
  text text NOT NULL,  
  CONSTRAINT cstr_prosp_note_pk PRIMARY KEY (id_note)
); 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_process_note
--

CREATE TABLE prosp_process_note (
  id_repository INT NOT NULL,
  id_process INT NOT NULL,
  id_note INT NOT NULL,
  CONSTRAINT cstr_prosp_process_note_pk PRIMARY KEY (id_repository, id_process, id_note),
  CONSTRAINT cstr_prosp_process_note_fk_process FOREIGN KEY (id_repository, id_process) REFERENCES prosp_process (id_repository, id_process),
  CONSTRAINT cstr_prosp_process_note_fk_note FOREIGN KEY (id_note) REFERENCES prosp_note (id_note)
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
  description text,
  nr INT NOT NULL,
  CONSTRAINT cstr_prosp_step_pk PRIMARY KEY (id_repository, id_process, id_step),
  CONSTRAINT cstr_prosp_step_fk_process FOREIGN KEY (id_repository, id_process) REFERENCES prosp_process (id_repository, id_process)
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
  value text,  
  CONSTRAINT cstr_prosp_step_parameter_pk PRIMARY KEY (id_repository, id_process, id_step, id_step_param),
  CONSTRAINT cstr_prosp_step_field_fk_step FOREIGN KEY (id_repository, id_process, id_step) REFERENCES prosp_step (id_repository, id_process, id_step)
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
  enabled char(1) NOT NULL default 'N',
  evaluation char(1) NOT NULL default 'N',
  unconditional char(1) NOT NULL default 'N',
  CONSTRAINT cstr_prosp_hop_pk PRIMARY KEY (id_repository, id_process, id_step_from, id_step_to),  
  CONSTRAINT cstr_prosp_hop_fk_from FOREIGN KEY (id_repository, id_process, id_step_from) REFERENCES prosp_step (id_repository, id_process, id_step),
  CONSTRAINT cstr_prosp_hop_fk_to FOREIGN KEY (id_repository, id_process, id_step_to) REFERENCES prosp_step (id_repository, id_process, id_step)
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
  CONSTRAINT cstr_prosp_hop_field_pk PRIMARY KEY (id_repository, id_process, id_step_from, id_step_to, id_field), 
  CONSTRAINT cstr_prosp_hop_field_fk_hop FOREIGN KEY (id_repository, id_process, id_step_from, id_step_to) REFERENCES prosp_hop (id_repository, id_process, id_step_from, id_step_to)  
);

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_process
--

CREATE TABLE retrosp_process (
  id_prosp_repository INT NOT NULL,
  id_prosp_process INT NOT NULL,  
  id_process INT NOT NULL,  
  start_TIMESTAMP TIMESTAMP NULL,
  finish_TIMESTAMP TIMESTAMP NULL,
  user VARCHAR(255),
  success char(1) NOT NULL default 'N',
  CONSTRAINT cstr_retrosp_process_pk PRIMARY KEY (id_prosp_repository, id_prosp_process, id_process),  
  CONSTRAINT cstr_retrosp_process_fk_prosp_process FOREIGN KEY (id_prosp_repository, id_prosp_process) REFERENCES prosp_process (id_repository, id_process)
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
  start_TIMESTAMP TIMESTAMP NULL,
  finish_TIMESTAMP TIMESTAMP NULL,
  success char(1) NOT NULL default 'N',
  CONSTRAINT cstr_retrosp_step_pk PRIMARY KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq),  
  CONSTRAINT cstr_retrosp_step_fk_process FOREIGN KEY (id_prosp_repository, id_prosp_process, id_process) REFERENCES retrosp_process (id_prosp_repository, id_prosp_process, id_process),
  CONSTRAINT cstr_retrosp_step_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_process, id_prosp_step) REFERENCES prosp_step (id_repository, id_process, id_step)
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
  field_value text,  
  CONSTRAINT cstr_retrosp_row_field_pk PRIMARY KEY (id_prosp_repository, id_prosp_process, id_prosp_step_from, id_prosp_step_to, id_prosp_field, id_process, seq_from, seq_to, row_count),
  CONSTRAINT cstr_retrosp_row_field_fk_prosp_hop_field FOREIGN KEY (id_prosp_repository, id_prosp_process, id_prosp_step_from, id_prosp_step_to, id_prosp_field) REFERENCES prosp_hop_field (id_repository, id_process, id_step_from, id_step_to, id_field),
  CONSTRAINT cstr_retrosp_row_field_fk_step_from FOREIGN KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step_from, seq_from) REFERENCES retrosp_step (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq),
  CONSTRAINT cstr_retrosp_row_field_fk_step_to FOREIGN KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step_to, seq_to) REFERENCES retrosp_step (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq)
);

-- --------------------------------------------------------