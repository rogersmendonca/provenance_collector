-- --------------------------------------------------------
--
-- CREATE TABLE prosp_repository
--

CREATE TABLE IF NOT EXISTS prosp_repository (
  id_repository int(11) NOT NULL,
  name varchar(255) NOT NULL,
  location varchar(255) NOT NULL,
  PRIMARY KEY (id_repository)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------
--
-- CREATE TABLE user
--

CREATE TABLE IF NOT EXISTS user (
  id_repository int(11) NOT NULL,
  id_user int(11) NOT NULL,
  login varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  description text default NULL,
  PRIMARY KEY (id_repository, id_user),
  KEY fk_repository (id_repository),
  CONSTRAINT cstr_user_fk_repository FOREIGN KEY (id_repository) REFERENCES prosp_repository (id_repository)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_workflow
--

CREATE TABLE IF NOT EXISTS prosp_workflow (
  id_repository int(11) NOT NULL,
  id_workflow int(11) NOT NULL,  
  object_id varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  description text default NULL,
  id_created_user int(11) NOT NULL,
  created_date timestamp NOT NULL,
  id_modified_user int(11) NOT NULL,
  modified_date timestamp NOT NULL,
  version_nr int(11) NOT NULL,
  id_root_repository int(11) NOT NULL,
  id_root int(11) NOT NULL,
  id_parent_repository int(11) NULL,
  id_parent int(11) NULL,  
  PRIMARY KEY (id_repository, id_workflow),  
  KEY fk_repository (id_repository),
  KEY fk_created_user (id_repository, id_created_user),
  KEY fk_modified_user (id_repository, id_modified_user),
  KEY fk_root (id_root_repository, id_root),
  KEY fk_parent (id_parent_repository, id_parent),  
  CONSTRAINT cstr_prosp_workflow_fk_repository FOREIGN KEY (id_repository) REFERENCES prosp_repository (id_repository),
  CONSTRAINT cstr_prosp_workflow_fk_created FOREIGN KEY (id_repository, id_created_user) REFERENCES user (id_repository, id_user),
  CONSTRAINT cstr_prosp_workflow_fk_modified FOREIGN KEY (id_repository, id_modified_user) REFERENCES user (id_repository, id_user),
  CONSTRAINT cstr_prosp_workflow_fk_root FOREIGN KEY (id_root_repository, id_root) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_prosp_workflow_fk_parent FOREIGN KEY (id_parent_repository, id_parent) REFERENCES prosp_workflow (id_repository, id_workflow)
) ENGINE=InnoDB DEFAULT CHARSET=latin1; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_note
--

CREATE TABLE IF NOT EXISTS prosp_note (
  id_note int(11) NOT NULL,
  text text NOT NULL,  
  PRIMARY KEY (id_note)
) ENGINE=InnoDB DEFAULT CHARSET=latin1; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_workflow_note
--

CREATE TABLE IF NOT EXISTS prosp_workflow_note (
  id_repository int(11) NOT NULL,
  id_workflow int(11) NOT NULL,
  id_note int(11) NOT NULL,
  PRIMARY KEY (id_repository, id_workflow, id_note),
  KEY fk_workflow (id_repository, id_workflow),  
  KEY fk_note (id_note),
  CONSTRAINT cstr_prosp_workflow_note_fk_workflow FOREIGN KEY (id_repository, id_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_prosp_workflow_note_fk_note FOREIGN KEY (id_note) REFERENCES prosp_note (id_note)
) ENGINE=InnoDB DEFAULT CHARSET=latin1; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step
--

CREATE TABLE IF NOT EXISTS prosp_step (
  id_repository int(11) NOT NULL,
  id_workflow int(11) NOT NULL,
  id_step int(11) NOT NULL,
  type varchar(255) NOT NULL,  
  name varchar(255) NOT NULL,
  description text default NULL,
  copy_nr int(11) NOT NULL,
  PRIMARY KEY (id_repository, id_workflow, id_step),
  KEY fk_workflow (id_repository, id_workflow),  
  CONSTRAINT cstr_prosp_step_fk_workflow FOREIGN KEY (id_repository, id_workflow) REFERENCES prosp_workflow (id_repository, id_workflow)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step_parameter
--

CREATE TABLE IF NOT EXISTS prosp_step_parameter (
  id_repository int(11) NOT NULL,
  id_workflow int(11) NOT NULL,
  id_step int(11) NOT NULL,  
  id_step_param int(11) NOT NULL,      
  param_name varchar(255) NOT NULL,  
  PRIMARY KEY (id_repository, id_workflow, id_step, id_step_param),
  KEY fk_step (id_repository, id_workflow, id_step),  
  CONSTRAINT cstr_prosp_step_field_fk_step FOREIGN KEY (id_repository, id_workflow, id_step) REFERENCES prosp_step (id_repository, id_workflow, id_step)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop
--

CREATE TABLE IF NOT EXISTS prosp_hop (  
  id_repository int(11) NOT NULL,
  id_workflow int(11) NOT NULL,
  id_step_from int(11) NOT NULL,
  id_step_to int(11) NOT NULL,
  enabled char(1) NOT NULL default 'N',
  evaluation char(1) NOT NULL default 'N',
  unconditional char(1) NOT NULL default 'N',
  PRIMARY KEY (id_repository, id_workflow, id_step_from, id_step_to),  
  KEY fk_from (id_repository, id_workflow, id_step_from),
  KEY fk_to (id_repository, id_workflow, id_step_to),
  CONSTRAINT cstr_prosp_hop_fk_from FOREIGN KEY (id_repository, id_workflow, id_step_from) REFERENCES prosp_step (id_repository, id_workflow, id_step),
  CONSTRAINT cstr_prosp_hop_fk_to FOREIGN KEY (id_repository, id_workflow, id_step_to) REFERENCES prosp_step (id_repository, id_workflow, id_step)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop_field
--

CREATE TABLE IF NOT EXISTS prosp_hop_field (
  id_repository int(11) NOT NULL,
  id_workflow int(11) NOT NULL,
  id_step_from int(11) NOT NULL,
  id_step_to int(11) NOT NULL,
  id_field int(11) NOT NULL,  
  field_name varchar(255) NOT NULL,
  PRIMARY KEY (id_repository, id_workflow, id_step_from, id_step_to, id_field), 
  KEY fk_hop (id_repository, id_workflow, id_step_from, id_step_to),
  CONSTRAINT cstr_prosp_hop_field_fk_hop FOREIGN KEY (id_repository, id_workflow, id_step_from, id_step_to) REFERENCES prosp_hop (id_repository, id_workflow, id_step_from, id_step_to)  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_workflow
--

CREATE TABLE IF NOT EXISTS retrosp_workflow (
  id_prosp_repository int(11) NOT NULL,
  id_prosp_workflow int(11) NOT NULL,  
  id_workflow int(11) NOT NULL,  
  start_date timestamp NULL default NULL,
  finish_date timestamp NULL default NULL,
  success char(1) NOT NULL default 'N',
  id_root_prosp_repository int(11) NOT NULL,
  id_root_prosp_workflow int(11) NOT NULL,
  id_root int(11) NOT NULL,
  id_parent_prosp_repository int(11) NULL,
  id_parent_prosp_workflow int(11) NULL,
  id_parent int(11) NULL,    
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow),  
  KEY fk_prosp_workflow (id_prosp_repository, id_prosp_workflow),
  KEY fk_retrosp_root (id_root_prosp_repository, id_root_prosp_workflow, id_root),
  KEY fk_retrosp_parent (id_parent_prosp_repository, id_parent_prosp_workflow, id_parent),
  CONSTRAINT cstr_retrosp_workflow_fk_prosp_workflow FOREIGN KEY (id_prosp_repository, id_prosp_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_retrosp_workflow_fk_retrosp_root FOREIGN KEY (id_root_prosp_repository, id_root_prosp_workflow, id_root) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  CONSTRAINT cstr_retrosp_workflow_fk_retrosp_parent FOREIGN KEY (id_parent_prosp_repository, id_parent_prosp_workflow, id_parent) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step
--

CREATE TABLE IF NOT EXISTS retrosp_step (
  id_prosp_repository int(11) NOT NULL,
  id_prosp_workflow int(11) NOT NULL,  
  id_workflow int(11) NOT NULL,   
  id_prosp_step int(11) NOT NULL,  
  seq int(11) NOT NULL,  
  start_date timestamp NULL default NULL,
  finish_date timestamp NULL default NULL,
  success char(1) NOT NULL default 'N',
  id_user int(11) NOT NULL,    
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),  
  KEY fk_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  KEY fk_prosp_step (id_prosp_repository, id_prosp_workflow, id_prosp_step),
  KEY fk_user (id_prosp_repository, id_user),
  CONSTRAINT cstr_retrosp_step_fk_workflow FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  CONSTRAINT cstr_retrosp_step_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step) REFERENCES prosp_step (id_repository, id_workflow, id_step),
  CONSTRAINT cstr_retrosp_step_fk_user FOREIGN KEY (id_prosp_repository, id_user) REFERENCES user (id_repository, id_user)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step_parameter
--
                           
CREATE TABLE IF NOT EXISTS retrosp_step_parameter (
  id_prosp_repository int(11) NOT NULL,
  id_prosp_workflow int(11) NOT NULL,  
  id_workflow int(11) NOT NULL,   
  id_prosp_step int(11) NOT NULL,    
  seq int(11) NOT NULL,
  id_prosp_step_param int(11) NOT NULL,  
  param_value text default NULL,    
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq, id_prosp_step_param),  
  KEY fk_prosp_step_param (id_prosp_repository, id_prosp_workflow, id_prosp_step, id_prosp_step_param),
  KEY fk_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),
  CONSTRAINT cstr_retrosp_step_param_fk_prosp_step_param FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step, id_prosp_step_param) REFERENCES prosp_step_parameter (id_repository, id_workflow, id_step, id_step_param),
  CONSTRAINT cstr_retrosp_step_param_fk_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_row_field
--

CREATE TABLE IF NOT EXISTS retrosp_row_field (
  id_prosp_repository int(11) NOT NULL,
  id_prosp_workflow int(11) NOT NULL,
  id_prosp_step_from int(11) NOT NULL,
  id_prosp_step_to int(11) NOT NULL,
  id_prosp_field int(11) NOT NULL,  
  id_workflow int(11) NOT NULL,   
  seq_from int(11) NOT NULL,  
  seq_to int(11) NOT NULL,   
  row_count int(11) NOT NULL,
  field_value text default NULL,  
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field, id_workflow, seq_from, seq_to, row_count),
  KEY fk_prosp_hop_field (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field),
  KEY fk_step_from (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_from, seq_from),
  KEY fk_step_to (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_to, seq_to),
  CONSTRAINT cstr_retrosp_row_field_fk_prosp_hop_field FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field) REFERENCES prosp_hop_field (id_repository, id_workflow, id_step_from, id_step_to, id_field),
  CONSTRAINT cstr_retrosp_row_field_fk_step_from FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_from, seq_from) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),
  CONSTRAINT cstr_retrosp_row_field_fk_step_to FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_to, seq_to) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------