-- --------------------------------------------------------
--
-- CREATE TABLE prosp_repository
--

CREATE TABLE IF NOT EXISTS prosp_repository (
  id_repository int(11) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  location varchar(255) NOT NULL,
  PRIMARY KEY (id_repository)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_process
--

CREATE TABLE IF NOT EXISTS prosp_process (
  id_repository int(11) NOT NULL,
  id_process int(11) NOT NULL,  
  object_id varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  description text default NULL,
  created_user varchar(255) default NULL,
  created_date timestamp NOT NULL,
  modified_user varchar(255) default NULL,
  modified_date timestamp NOT NULL,
  id_root_repository int(11) NOT NULL,
  id_root int(11) NOT NULL,
  id_parent int(11) NULL,  
  PRIMARY KEY (id_repository, id_process),  
  KEY fk_repository (id_repository),  
  KEY fk_root (id_root_repository, id_root),
  KEY fk_parent (id_root_repository, id_parent),
  CONSTRAINT cstr_prosp_process_fk_repository FOREIGN KEY (id_repository) REFERENCES prosp_repository (id_repository),
  CONSTRAINT cstr_prosp_process_fk_root FOREIGN KEY (id_root_repository, id_root) REFERENCES prosp_process (id_repository, id_process),
  CONSTRAINT cstr_prosp_process_fk_parent FOREIGN KEY (id_root_repository, id_parent) REFERENCES prosp_process (id_repository, id_process)
) ENGINE=InnoDB DEFAULT CHARSET=latin1; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_note
--

CREATE TABLE IF NOT EXISTS prosp_note (
  id_note int(11) NOT NULL auto_increment,
  text text NOT NULL,  
  PRIMARY KEY (id_note)
) ENGINE=InnoDB DEFAULT CHARSET=latin1; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_process_note
--

CREATE TABLE IF NOT EXISTS prosp_process_note (
  id_repository int(11) NOT NULL,
  id_process int(11) NOT NULL,
  id_note int(11) NOT NULL,
  PRIMARY KEY (id_repository, id_process, id_note),
  KEY fk_process (id_repository, id_process),  
  KEY fk_note (id_note),
  CONSTRAINT cstr_prosp_process_note_fk_process FOREIGN KEY (id_repository, id_process) REFERENCES prosp_process (id_repository, id_process),
  CONSTRAINT cstr_prosp_process_note_fk_note FOREIGN KEY (id_note) REFERENCES prosp_note (id_note)
) ENGINE=InnoDB DEFAULT CHARSET=latin1; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step
--

CREATE TABLE IF NOT EXISTS prosp_step (
  id_repository int(11) NOT NULL,
  id_process int(11) NOT NULL,
  id_step int(11) NOT NULL,
  type varchar(255) NOT NULL,  
  name varchar(255) NOT NULL,
  description text default NULL,
  nr int(11) NOT NULL,
  PRIMARY KEY (id_repository, id_process, id_step),
  KEY fk_process (id_repository, id_process),  
  CONSTRAINT cstr_prosp_step_fk_process FOREIGN KEY (id_repository, id_process) REFERENCES prosp_process (id_repository, id_process)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step_parameter
--

CREATE TABLE IF NOT EXISTS prosp_step_parameter (
  id_repository int(11) NOT NULL,
  id_process int(11) NOT NULL,
  id_step int(11) NOT NULL,  
  id_step_param int(11) NOT NULL,      
  name varchar(255) NOT NULL, 
  value text default NULL,  
  PRIMARY KEY (id_repository, id_process, id_step, id_step_param),
  KEY fk_step (id_repository, id_process, id_step),  
  CONSTRAINT cstr_prosp_step_field_fk_step FOREIGN KEY (id_repository, id_process, id_step) REFERENCES prosp_step (id_repository, id_process, id_step)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop
--

CREATE TABLE IF NOT EXISTS prosp_hop (  
  id_repository int(11) NOT NULL,
  id_process int(11) NOT NULL,
  id_from int(11) NOT NULL,
  id_to int(11) NOT NULL,
  enabled char(1) NOT NULL default 'N',
  evaluation char(1) NOT NULL default 'N',
  unconditional char(1) NOT NULL default 'N',
  PRIMARY KEY (id_repository, id_process, id_from, id_to),  
  KEY fk_from (id_repository, id_process, id_from),
  KEY fk_to (id_repository, id_process, id_to),
  CONSTRAINT cstr_prosp_hop_fk_from FOREIGN KEY (id_repository, id_process, id_from) REFERENCES prosp_step (id_repository, id_process, id_step),
  CONSTRAINT cstr_prosp_hop_fk_to FOREIGN KEY (id_repository, id_process, id_to) REFERENCES prosp_step (id_repository, id_process, id_step)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_process
--

CREATE TABLE IF NOT EXISTS retrosp_process (
  id_prosp_repository int(11) NOT NULL,
  id_prosp_process int(11) NOT NULL,  
  id_process int(11) NOT NULL,  
  start_date timestamp NULL default NULL,
  finish_date timestamp NULL default NULL,
  user varchar(255) default NULL,
  success char(1) NOT NULL default 'N',
  PRIMARY KEY (id_prosp_repository, id_prosp_process, id_process),  
  KEY fk_prosp_process (id_prosp_repository, id_prosp_process),
  CONSTRAINT cstr_retrosp_process_fk_prosp_process FOREIGN KEY (id_prosp_repository, id_prosp_process) REFERENCES prosp_process (id_repository, id_process)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step
--

CREATE TABLE IF NOT EXISTS retrosp_step (
  id_prosp_repository int(11) NOT NULL,
  id_prosp_process int(11) NOT NULL,  
  id_process int(11) NOT NULL,   
  id_prosp_step int(11) NOT NULL,  
  seq int(11) NOT NULL,  
  start_date timestamp NULL default NULL,
  finish_date timestamp NULL default NULL,
  success char(1) NOT NULL default 'N',
  PRIMARY KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq),  
  KEY fk_process (id_prosp_repository, id_prosp_process, id_process),
  KEY fk_prosp_step (id_prosp_repository, id_prosp_process, id_prosp_step),
  CONSTRAINT cstr_retrosp_step_fk_process FOREIGN KEY (id_prosp_repository, id_prosp_process, id_process) REFERENCES retrosp_process (id_prosp_repository, id_prosp_process, id_process),
  CONSTRAINT cstr_retrosp_step_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_process, id_prosp_step) REFERENCES prosp_step (id_repository, id_process, id_step)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_step_row
--

CREATE TABLE IF NOT EXISTS retrosp_step_field (
  id_prosp_repository int(11) NOT NULL,
  id_prosp_process int(11) NOT NULL,  
  id_process int(11) NOT NULL,   
  id_prosp_step int(11) NOT NULL,  
  seq int(11) NOT NULL,
  id_field int(11) NOT NULL,  
  name varchar(255) NOT NULL,
  PRIMARY KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq, id_field), 
  KEY fk_step (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq),
  CONSTRAINT cstr_retrosp_step_field_fk_step FOREIGN KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq) REFERENCES retrosp_step (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_step_row
--

CREATE TABLE IF NOT EXISTS retrosp_step_field_row (
  id_prosp_repository int(11) NOT NULL,
  id_prosp_process int(11) NOT NULL,  
  id_process int(11) NOT NULL,   
  id_prosp_step int(11) NOT NULL,  
  seq int(11) NOT NULL,
  id_field int(11) NOT NULL,   
  row_count int(11) NOT NULL,
  event char(1) NOT NULL,
  field_value text default NULL,  
  PRIMARY KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq, id_field, row_count, event), 
  KEY fk_step_field (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq, id_field),
  CONSTRAINT cstr_retrosp_step_field_row_fk_step_field FOREIGN KEY (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq, id_field) REFERENCES retrosp_step_field (id_prosp_repository, id_prosp_process, id_process, id_prosp_step, seq, id_field)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------