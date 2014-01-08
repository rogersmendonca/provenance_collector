-- --------------------------------------------------------
--
-- CREATE TABLE prosp_repository
--

CREATE TABLE IF NOT EXISTS prosp_repository (
  id_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio.',
  name varchar(255) NOT NULL COMMENT 'Nome do reposit�rio.',
  location varchar(255) NOT NULL COMMENT 'Localiza��o do reposit�rio.',
  PRIMARY KEY (id_repository)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Reposit�rio (proveni�ncia prospectiva).';

-- --------------------------------------------------------
--
-- CREATE TABLE user
--

CREATE TABLE IF NOT EXISTS user (
  id_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio.',
  id_user int(11) NOT NULL COMMENT 'Identificador do usu�rio.',
  login varchar(255) NOT NULL COMMENT 'Login do usu�rio.',
  name varchar(255) NOT NULL COMMENT 'Nome do usu�rio.',
  description text default NULL COMMENT 'Descri��o do usu�rio.',
  PRIMARY KEY (id_repository, id_user),
  KEY fk_repository (id_repository),
  CONSTRAINT cstr_user_fk_repository FOREIGN KEY (id_repository) REFERENCES prosp_repository (id_repository)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Usu�rio.';

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_workflow
--

CREATE TABLE IF NOT EXISTS prosp_workflow (
  id_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow est� armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow.',  
  object_id varchar(255) NOT NULL COMMENT 'Identificador da composi��o do workflow na ferramenta de ETL.',
  name varchar(255) NOT NULL COMMENT 'Nome do workflow.',
  description text default NULL COMMENT 'Descri��o do workflow.',
  id_created_user int(11) NOT NULL COMMENT 'Identificador do usu�rio que criou o workflow (prosp.).',
  created_date timestamp NOT NULL COMMENT 'Data e hora de cria��o da composi��o do workflow.',
  id_modified_user int(11) NOT NULL COMMENT 'Identificador do usu�rio que fez a �ltima modifica��o na composi��o do workflow.',
  modified_date timestamp NOT NULL COMMENT 'Data e hora da �ltima modifica��o na composi��o do workflow.',
  version_nr int(11) NOT NULL COMMENT 'N�mero da vers�o da composi��o do workflow.',
  id_root_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow raiz est� armazenada.',
  id_root int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow raiz.',
  id_parent_repository int(11) NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow pai est� armazenada.',
  id_parent int(11) NULL COMMENT 'Identificador da composi��o do workflow pai.',
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Workflow (proveni�ncia prospectiva).'; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_note
--

CREATE TABLE IF NOT EXISTS prosp_note (
  id_note int(11) NOT NULL COMMENT 'Identificador da anota��o.',
  text text NOT NULL COMMENT 'Texto da anota��o.',  
  PRIMARY KEY (id_note)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Anota��o de documenta��o (proveni�ncia prospectiva).'; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_workflow_note
--

CREATE TABLE IF NOT EXISTS prosp_workflow_note (
  id_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow est� armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow.',  
  id_note int(11) NOT NULL COMMENT 'Identificador da anota��o.',
  PRIMARY KEY (id_repository, id_workflow, id_note),
  KEY fk_workflow (id_repository, id_workflow),  
  KEY fk_note (id_note),
  CONSTRAINT cstr_prosp_workflow_note_fk_workflow FOREIGN KEY (id_repository, id_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_prosp_workflow_note_fk_note FOREIGN KEY (id_note) REFERENCES prosp_note (id_note)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Rela��o entre workflow e anota��o (proveni�ncia prospectiva).'; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step
--

CREATE TABLE IF NOT EXISTS prosp_step_type (
  id_step_type int(11) NOT NULL COMMENT 'Identificador do tipo de passo.',
  name varchar(255) NOT NULL COMMENT 'Nome do tipo de passo.',
  PRIMARY KEY (id_step_type)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tipo de passo (proveni�ncia prospectiva).'; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step
--

CREATE TABLE IF NOT EXISTS prosp_step (
  id_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow est� armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow.',  
  id_step int(11) NOT NULL COMMENT 'Identificador da composi��o do passo.',
  id_step_type int(11) NOT NULL COMMENT 'Identificador do tipo do passo.',  
  name varchar(255) NOT NULL COMMENT 'Nome do passo.',
  description text default NULL COMMENT 'Descri��o do passo.',
  copy_nr int(11) NOT NULL COMMENT 'N�mero da c�pia do passo.',
  PRIMARY KEY (id_repository, id_workflow, id_step),
  KEY fk_workflow (id_repository, id_workflow),
  KEY fk_step_type (id_step_type),
  KEY fk_step_type2 (id_repository, id_workflow, id_step, id_step_type),
  CONSTRAINT cstr_prosp_step_fk_workflow FOREIGN KEY (id_repository, id_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_prosp_step_fk_step_type FOREIGN KEY (id_step_type) REFERENCES prosp_step_type (id_step_type)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Passo do workflow (proveni�ncia prospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step_parameter
--

CREATE TABLE IF NOT EXISTS prosp_step_parameter (
  id_step_type int(11) NOT NULL COMMENT 'Identificador do tipo do passo.',  
  id_step_param int(11) NOT NULL COMMENT 'Identificador do par�metro.',      
  param_name varchar(255) NOT NULL COMMENT 'Nome do par�metro.',  
  PRIMARY KEY (id_step_type, id_step_param),
  KEY fk_step_type (id_step_type),  
  CONSTRAINT cstr_prosp_step_param_fk_step_type FOREIGN KEY (id_step_type) REFERENCES prosp_step_type (id_step_type)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Par�metro do passo (proveni�ncia prospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop
--

CREATE TABLE IF NOT EXISTS prosp_hop (  
  id_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow est� armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow.',  
  id_step_from int(11) NOT NULL COMMENT 'Identificador da composi��o do passo de origem da liga��o.',
  id_step_to int(11) NOT NULL COMMENT 'Identificador da composi��o do passo de destino da liga��o.',
  enabled char(1) NOT NULL default 'N' COMMENT 'Indica se a liga��o entre os 2 passos est� habilitada (Y) ou n�o (N).',  
  unconditional char(1) NOT NULL default 'N' COMMENT 'Indica se a utiliza��o da liga��o n�o estar� condicionada ao resultado do passo de origem (Y) ou se estar� condicionada (N).',
  evaluation char(1) NOT NULL default 'N' COMMENT 'Se a utiliza��o da liga��o estiver condicionada ao resultado do passo de origem, indica se a liga��o ser� utilizada se o passo de origem for conclu�do com sucesso (Y) ou com erros (N).',  
  PRIMARY KEY (id_repository, id_workflow, id_step_from, id_step_to),  
  KEY fk_from (id_repository, id_workflow, id_step_from),
  KEY fk_to (id_repository, id_workflow, id_step_to),
  CONSTRAINT cstr_prosp_hop_fk_from FOREIGN KEY (id_repository, id_workflow, id_step_from) REFERENCES prosp_step (id_repository, id_workflow, id_step),
  CONSTRAINT cstr_prosp_hop_fk_to FOREIGN KEY (id_repository, id_workflow, id_step_to) REFERENCES prosp_step (id_repository, id_workflow, id_step)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Liga��o entre 2 passos (proveni�ncia prospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop_field
--

CREATE TABLE IF NOT EXISTS prosp_hop_field (
  id_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow est� armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow.',  
  id_step_from int(11) NOT NULL COMMENT 'Identificador da composi��o do passo de origem da liga��o.',
  id_step_to int(11) NOT NULL COMMENT 'Identificador da composi��o do passo de destino da liga��o.',
  id_field int(11) NOT NULL COMMENT 'Identificador do campo de dados.',  
  field_name varchar(255) NOT NULL COMMENT 'Nome do campo de dados.',
  PRIMARY KEY (id_repository, id_workflow, id_step_from, id_step_to, id_field), 
  KEY fk_hop (id_repository, id_workflow, id_step_from, id_step_to),
  CONSTRAINT cstr_prosp_hop_field_fk_hop FOREIGN KEY (id_repository, id_workflow, id_step_from, id_step_to) REFERENCES prosp_hop (id_repository, id_workflow, id_step_from, id_step_to)  
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Campo de dados (proveni�ncia prospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_workflow
--

CREATE TABLE IF NOT EXISTS retrosp_workflow (
  id_prosp_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow est� armazenada.',
  id_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow.',  
  id_workflow int(11) NOT NULL COMMENT 'Identificador da execu��o do workflow.',  
  start_date timestamp NULL default NULL COMMENT 'Data e hora do in�cio da execu��o do workflow.',
  finish_date timestamp NULL default NULL COMMENT  'Data e hora do t�rmino da execu��o do workflow.',
  success char(1) NOT NULL default 'N' COMMENT 'Indica se o workflow foi executado com sucesso (Y) ou n�o (N).',
  id_user int(11) NOT NULL COMMENT 'Identificador do usu�rio que executou o workflow.',  
  id_root_prosp_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow raiz est� armazenada.',
  id_root_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow raiz.',
  id_root int(11) NOT NULL COMMENT 'Identificador da execu��o do workflow raiz.',
  id_parent_prosp_repository int(11) NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow pai est� armazenada.',
  id_parent_prosp_workflow int(11) NULL COMMENT 'Identificador da composi��o do workflow pai.',  
  id_parent int(11) NULL COMMENT 'Identificador da execu��o do workflow pai.',    
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow),  
  KEY fk_prosp_workflow (id_prosp_repository, id_prosp_workflow),
  KEY fk_user (id_prosp_repository, id_user),
  KEY fk_retrosp_root (id_root_prosp_repository, id_root_prosp_workflow, id_root),
  KEY fk_retrosp_parent (id_parent_prosp_repository, id_parent_prosp_workflow, id_parent),
  CONSTRAINT cstr_retrosp_workflow_fk_prosp_workflow FOREIGN KEY (id_prosp_repository, id_prosp_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_retrosp_workflow_fk_user FOREIGN KEY (id_prosp_repository, id_user) REFERENCES user (id_repository, id_user),
  CONSTRAINT cstr_retrosp_workflow_fk_retrosp_root FOREIGN KEY (id_root_prosp_repository, id_root_prosp_workflow, id_root) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  CONSTRAINT cstr_retrosp_workflow_fk_retrosp_parent FOREIGN KEY (id_parent_prosp_repository, id_parent_prosp_workflow, id_parent) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Workflow (proveni�ncia retrospectiva).';

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step
--

CREATE TABLE IF NOT EXISTS retrosp_step (
  id_prosp_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow est� armazenada.',
  id_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow.',  
  id_workflow int(11) NOT NULL COMMENT 'Identificador da execu��o do workflow.',  
  id_prosp_step int(11) NOT NULL COMMENT 'Identificador da composi��o do passo.',  
  seq int(11) NOT NULL COMMENT 'Sequencial de execu��o do passo no workflow.',  
  start_date timestamp NULL default NULL COMMENT 'Data e hora do in�cio da execu��o do passo.',
  finish_date timestamp NULL default NULL COMMENT  'Data e hora do t�rmino da execu��o do passo.',
  success char(1) NOT NULL default 'N' COMMENT 'Indica se o passo foi executado com sucesso (Y) ou n�o (N).',
  id_user int(11) NOT NULL COMMENT 'Identificador do usu�rio que executou o passo.',     
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),  
  KEY fk_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  KEY fk_prosp_step (id_prosp_repository, id_prosp_workflow, id_prosp_step),
  KEY fk_user (id_prosp_repository, id_user),
  CONSTRAINT cstr_retrosp_step_fk_workflow FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  CONSTRAINT cstr_retrosp_step_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step) REFERENCES prosp_step (id_repository, id_workflow, id_step),
  CONSTRAINT cstr_retrosp_step_fk_user FOREIGN KEY (id_prosp_repository, id_user) REFERENCES user (id_repository, id_user)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Passo do workflow (proveni�ncia retrospectiva).';

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step_parameter
--
                           
CREATE TABLE IF NOT EXISTS retrosp_step_parameter (
  id_prosp_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow est� armazenada.',
  id_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow.',  
  id_workflow int(11) NOT NULL COMMENT 'Identificador da execu��o do workflow.',  
  id_prosp_step int(11) NOT NULL COMMENT 'Identificador da composi��o do passo.',  
  seq int(11) NOT NULL COMMENT 'Sequencial de execu��o do passo no workflow.',      
  id_prosp_step_type int(11) NOT NULL COMMENT 'Identificador do tipo do passo.',     
  id_prosp_step_param int(11) NOT NULL COMMENT 'Identificador do par�metro.',  
  param_value text default NULL COMMENT 'Valor do par�metro.',    
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq, id_prosp_step_type, id_prosp_step_param),
  KEY fk_prosp_step (id_prosp_repository, id_prosp_workflow, id_prosp_step, id_prosp_step_type),
  KEY fk_prosp_step_param (id_prosp_step_type, id_prosp_step_param),
  KEY fk_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),
  CONSTRAINT cstr_retrosp_step_param_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step, id_prosp_step_type) REFERENCES prosp_step (id_repository, id_workflow, id_step, id_step_type),
  CONSTRAINT cstr_retrosp_step_param_fk_prosp_step_param FOREIGN KEY (id_prosp_step_type, id_prosp_step_param) REFERENCES prosp_step_parameter (id_step_type, id_step_param),
  CONSTRAINT cstr_retrosp_step_param_fk_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Par�metro do passo (proveni�ncia retrospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_row_field
--

CREATE TABLE IF NOT EXISTS retrosp_row_field (
  id_prosp_repository int(11) NOT NULL COMMENT 'Identificador do reposit�rio onde a composi��o do workflow est� armazenada.',
  id_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composi��o do workflow.',  
  id_workflow int(11) NOT NULL COMMENT 'Identificador da execu��o do workflow.',  
  id_prosp_step_from int(11) NOT NULL COMMENT 'Identificador da composi��o do passo de origem da liga��o.',
  id_prosp_step_to int(11) NOT NULL COMMENT 'Identificador da composi��o do passo de destino da liga��o.',  
  id_prosp_field int(11) NOT NULL COMMENT 'Identificador do campo de dados.',  
  seq_from int(11) NOT NULL COMMENT 'Sequencial de execu��o do passo de origem no workflow.',
  seq_to int(11) NOT NULL COMMENT 'Sequencial de execu��o do passo de destino no workflow.',
  row_count int(11) NOT NULL COMMENT 'N�mero da linha de dados.',
  field_value text default NULL COMMENT 'Valor do campo de dados.',  
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field, id_workflow, seq_from, seq_to, row_count),
  KEY fk_prosp_hop_field (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field),
  KEY fk_step_from (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_from, seq_from),
  KEY fk_step_to (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_to, seq_to),
  CONSTRAINT cstr_retrosp_row_field_fk_prosp_hop_field FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field) REFERENCES prosp_hop_field (id_repository, id_workflow, id_step_from, id_step_to, id_field),
  CONSTRAINT cstr_retrosp_row_field_fk_step_from FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_from, seq_from) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),
  CONSTRAINT cstr_retrosp_row_field_fk_step_to FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_to, seq_to) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Campo de uma linha de dados (proveni�ncia retrospectiva).';

-- --------------------------------------------------------