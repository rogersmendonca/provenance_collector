-- --------------------------------------------------------
--
-- CREATE TABLE prosp_repository
--

CREATE TABLE IF NOT EXISTS prosp_repository (
  id_repository int(11) NOT NULL COMMENT 'Identificador do repositório.',
  name varchar(255) NOT NULL COMMENT 'Nome do repositório.',
  location varchar(255) NOT NULL COMMENT 'Localização do repositório.',
  PRIMARY KEY (id_repository)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Repositório (proveniência prospectiva).';

-- --------------------------------------------------------
--
-- CREATE TABLE user
--

CREATE TABLE IF NOT EXISTS user (
  id_repository int(11) NOT NULL COMMENT 'Identificador do repositório.',
  id_user int(11) NOT NULL COMMENT 'Identificador do usuário.',
  login varchar(255) NOT NULL COMMENT 'Login do usuário.',
  name varchar(255) NOT NULL COMMENT 'Nome do usuário.',
  description text default NULL COMMENT 'Descrição do usuário.',
  PRIMARY KEY (id_repository, id_user),
  KEY fk_repository (id_repository),
  CONSTRAINT cstr_user_fk_repository FOREIGN KEY (id_repository) REFERENCES prosp_repository (id_repository)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Usuário.';

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_workflow
--

CREATE TABLE IF NOT EXISTS prosp_workflow (
  id_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow está armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow.',  
  object_id varchar(255) NOT NULL COMMENT 'Identificador da composição do workflow na ferramenta de ETL.',
  name varchar(255) NOT NULL COMMENT 'Nome do workflow.',
  description text default NULL COMMENT 'Descrição do workflow.',
  id_created_user int(11) NOT NULL COMMENT 'Identificador do usuário que criou o workflow (prosp.).',
  created_date timestamp NOT NULL COMMENT 'Data e hora de criação da composição do workflow.',
  id_modified_user int(11) NOT NULL COMMENT 'Identificador do usuário que fez a última modificação na composição do workflow.',
  modified_date timestamp NOT NULL COMMENT 'Data e hora da última modificação na composição do workflow.',
  version_nr int(11) NOT NULL COMMENT 'Número da versão da composição do workflow.',
  id_root_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow raiz está armazenada.',
  id_root int(11) NOT NULL COMMENT 'Identificador da composição do workflow raiz.',
  id_parent_repository int(11) NULL COMMENT 'Identificador do repositório onde a composição do workflow pai está armazenada.',
  id_parent int(11) NULL COMMENT 'Identificador da composição do workflow pai.',
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Workflow (proveniência prospectiva).'; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_note
--

CREATE TABLE IF NOT EXISTS prosp_note (
  id_note int(11) NOT NULL COMMENT 'Identificador da anotação.',
  text text NOT NULL COMMENT 'Texto da anotação.',  
  PRIMARY KEY (id_note)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Anotação de documentação (proveniência prospectiva).'; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_workflow_note
--

CREATE TABLE IF NOT EXISTS prosp_workflow_note (
  id_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow está armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow.',  
  id_note int(11) NOT NULL COMMENT 'Identificador da anotação.',
  PRIMARY KEY (id_repository, id_workflow, id_note),
  KEY fk_workflow (id_repository, id_workflow),  
  KEY fk_note (id_note),
  CONSTRAINT cstr_prosp_workflow_note_fk_workflow FOREIGN KEY (id_repository, id_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_prosp_workflow_note_fk_note FOREIGN KEY (id_note) REFERENCES prosp_note (id_note)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Relação entre workflow e anotação (proveniência prospectiva).'; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step
--

CREATE TABLE IF NOT EXISTS prosp_step_type (
  id_step_type int(11) NOT NULL COMMENT 'Identificador do tipo de passo.',
  name varchar(255) NOT NULL COMMENT 'Nome do tipo de passo.',
  PRIMARY KEY (id_step_type)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tipo de passo (proveniência prospectiva).'; 

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step
--

CREATE TABLE IF NOT EXISTS prosp_step (
  id_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow está armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow.',  
  id_step int(11) NOT NULL COMMENT 'Identificador da composição do passo.',
  id_step_type int(11) NOT NULL COMMENT 'Identificador do tipo do passo.',  
  name varchar(255) NOT NULL COMMENT 'Nome do passo.',
  description text default NULL COMMENT 'Descrição do passo.',
  copy_nr int(11) NOT NULL COMMENT 'Número da cópia do passo.',
  PRIMARY KEY (id_repository, id_workflow, id_step),
  KEY fk_workflow (id_repository, id_workflow),
  KEY fk_step_type (id_step_type),
  KEY fk_step_type2 (id_repository, id_workflow, id_step, id_step_type),
  CONSTRAINT cstr_prosp_step_fk_workflow FOREIGN KEY (id_repository, id_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_prosp_step_fk_step_type FOREIGN KEY (id_step_type) REFERENCES prosp_step_type (id_step_type)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Passo do workflow (proveniência prospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_step_parameter
--

CREATE TABLE IF NOT EXISTS prosp_step_parameter (
  id_step_type int(11) NOT NULL COMMENT 'Identificador do tipo do passo.',  
  id_step_param int(11) NOT NULL COMMENT 'Identificador do parâmetro.',      
  param_name varchar(255) NOT NULL COMMENT 'Nome do parâmetro.',  
  PRIMARY KEY (id_step_type, id_step_param),
  KEY fk_step_type (id_step_type),  
  CONSTRAINT cstr_prosp_step_param_fk_step_type FOREIGN KEY (id_step_type) REFERENCES prosp_step_type (id_step_type)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Parâmetro do passo (proveniência prospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop
--

CREATE TABLE IF NOT EXISTS prosp_hop (  
  id_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow está armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow.',  
  id_step_from int(11) NOT NULL COMMENT 'Identificador da composição do passo de origem da ligação.',
  id_step_to int(11) NOT NULL COMMENT 'Identificador da composição do passo de destino da ligação.',
  enabled char(1) NOT NULL default 'N' COMMENT 'Indica se a ligação entre os 2 passos está habilitada (Y) ou não (N).',  
  unconditional char(1) NOT NULL default 'N' COMMENT 'Indica se a utilização da ligação não estará condicionada ao resultado do passo de origem (Y) ou se estará condicionada (N).',
  evaluation char(1) NOT NULL default 'N' COMMENT 'Se a utilização da ligação estiver condicionada ao resultado do passo de origem, indica se a ligação será utilizada se o passo de origem for concluído com sucesso (Y) ou com erros (N).',  
  PRIMARY KEY (id_repository, id_workflow, id_step_from, id_step_to),  
  KEY fk_from (id_repository, id_workflow, id_step_from),
  KEY fk_to (id_repository, id_workflow, id_step_to),
  CONSTRAINT cstr_prosp_hop_fk_from FOREIGN KEY (id_repository, id_workflow, id_step_from) REFERENCES prosp_step (id_repository, id_workflow, id_step),
  CONSTRAINT cstr_prosp_hop_fk_to FOREIGN KEY (id_repository, id_workflow, id_step_to) REFERENCES prosp_step (id_repository, id_workflow, id_step)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ligação entre 2 passos (proveniência prospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE prosp_hop_field
--

CREATE TABLE IF NOT EXISTS prosp_hop_field (
  id_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow está armazenada.',
  id_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow.',  
  id_step_from int(11) NOT NULL COMMENT 'Identificador da composição do passo de origem da ligação.',
  id_step_to int(11) NOT NULL COMMENT 'Identificador da composição do passo de destino da ligação.',
  id_field int(11) NOT NULL COMMENT 'Identificador do campo de dados.',  
  field_name varchar(255) NOT NULL COMMENT 'Nome do campo de dados.',
  PRIMARY KEY (id_repository, id_workflow, id_step_from, id_step_to, id_field), 
  KEY fk_hop (id_repository, id_workflow, id_step_from, id_step_to),
  CONSTRAINT cstr_prosp_hop_field_fk_hop FOREIGN KEY (id_repository, id_workflow, id_step_from, id_step_to) REFERENCES prosp_hop (id_repository, id_workflow, id_step_from, id_step_to)  
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Campo de dados (proveniência prospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_workflow
--

CREATE TABLE IF NOT EXISTS retrosp_workflow (
  id_prosp_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow está armazenada.',
  id_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow.',  
  id_workflow int(11) NOT NULL COMMENT 'Identificador da execução do workflow.',  
  start_date timestamp NULL default NULL COMMENT 'Data e hora do início da execução do workflow.',
  finish_date timestamp NULL default NULL COMMENT  'Data e hora do término da execução do workflow.',
  success char(1) NOT NULL default 'N' COMMENT 'Indica se o workflow foi executado com sucesso (Y) ou não (N).',
  id_user int(11) NOT NULL COMMENT 'Identificador do usuário que executou o workflow.',  
  id_root_prosp_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow raiz está armazenada.',
  id_root_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow raiz.',
  id_root int(11) NOT NULL COMMENT 'Identificador da execução do workflow raiz.',
  id_parent_prosp_repository int(11) NULL COMMENT 'Identificador do repositório onde a composição do workflow pai está armazenada.',
  id_parent_prosp_workflow int(11) NULL COMMENT 'Identificador da composição do workflow pai.',  
  id_parent int(11) NULL COMMENT 'Identificador da execução do workflow pai.',    
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow),  
  KEY fk_prosp_workflow (id_prosp_repository, id_prosp_workflow),
  KEY fk_user (id_prosp_repository, id_user),
  KEY fk_retrosp_root (id_root_prosp_repository, id_root_prosp_workflow, id_root),
  KEY fk_retrosp_parent (id_parent_prosp_repository, id_parent_prosp_workflow, id_parent),
  CONSTRAINT cstr_retrosp_workflow_fk_prosp_workflow FOREIGN KEY (id_prosp_repository, id_prosp_workflow) REFERENCES prosp_workflow (id_repository, id_workflow),
  CONSTRAINT cstr_retrosp_workflow_fk_user FOREIGN KEY (id_prosp_repository, id_user) REFERENCES user (id_repository, id_user),
  CONSTRAINT cstr_retrosp_workflow_fk_retrosp_root FOREIGN KEY (id_root_prosp_repository, id_root_prosp_workflow, id_root) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  CONSTRAINT cstr_retrosp_workflow_fk_retrosp_parent FOREIGN KEY (id_parent_prosp_repository, id_parent_prosp_workflow, id_parent) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Workflow (proveniência retrospectiva).';

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step
--

CREATE TABLE IF NOT EXISTS retrosp_step (
  id_prosp_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow está armazenada.',
  id_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow.',  
  id_workflow int(11) NOT NULL COMMENT 'Identificador da execução do workflow.',  
  id_prosp_step int(11) NOT NULL COMMENT 'Identificador da composição do passo.',  
  seq int(11) NOT NULL COMMENT 'Sequencial de execução do passo no workflow.',  
  start_date timestamp NULL default NULL COMMENT 'Data e hora do início da execução do passo.',
  finish_date timestamp NULL default NULL COMMENT  'Data e hora do término da execução do passo.',
  success char(1) NOT NULL default 'N' COMMENT 'Indica se o passo foi executado com sucesso (Y) ou não (N).',
  id_user int(11) NOT NULL COMMENT 'Identificador do usuário que executou o passo.',     
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),  
  KEY fk_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  KEY fk_prosp_step (id_prosp_repository, id_prosp_workflow, id_prosp_step),
  KEY fk_user (id_prosp_repository, id_user),
  CONSTRAINT cstr_retrosp_step_fk_workflow FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow) REFERENCES retrosp_workflow (id_prosp_repository, id_prosp_workflow, id_workflow),
  CONSTRAINT cstr_retrosp_step_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step) REFERENCES prosp_step (id_repository, id_workflow, id_step),
  CONSTRAINT cstr_retrosp_step_fk_user FOREIGN KEY (id_prosp_repository, id_user) REFERENCES user (id_repository, id_user)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Passo do workflow (proveniência retrospectiva).';

-- --------------------------------------------------------
--
-- CREATE TABLE retrosp_step_parameter
--
                           
CREATE TABLE IF NOT EXISTS retrosp_step_parameter (
  id_prosp_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow está armazenada.',
  id_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow.',  
  id_workflow int(11) NOT NULL COMMENT 'Identificador da execução do workflow.',  
  id_prosp_step int(11) NOT NULL COMMENT 'Identificador da composição do passo.',  
  seq int(11) NOT NULL COMMENT 'Sequencial de execução do passo no workflow.',      
  id_prosp_step_type int(11) NOT NULL COMMENT 'Identificador do tipo do passo.',     
  id_prosp_step_param int(11) NOT NULL COMMENT 'Identificador do parâmetro.',  
  param_value text default NULL COMMENT 'Valor do parâmetro.',    
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq, id_prosp_step_type, id_prosp_step_param),
  KEY fk_prosp_step (id_prosp_repository, id_prosp_workflow, id_prosp_step, id_prosp_step_type),
  KEY fk_prosp_step_param (id_prosp_step_type, id_prosp_step_param),
  KEY fk_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),
  CONSTRAINT cstr_retrosp_step_param_fk_prosp_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step, id_prosp_step_type) REFERENCES prosp_step (id_repository, id_workflow, id_step, id_step_type),
  CONSTRAINT cstr_retrosp_step_param_fk_prosp_step_param FOREIGN KEY (id_prosp_step_type, id_prosp_step_param) REFERENCES prosp_step_parameter (id_step_type, id_step_param),
  CONSTRAINT cstr_retrosp_step_param_fk_step FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Parâmetro do passo (proveniência retrospectiva).';

-- --------------------------------------------------------

--
-- CREATE TABLE retrosp_row_field
--

CREATE TABLE IF NOT EXISTS retrosp_row_field (
  id_prosp_repository int(11) NOT NULL COMMENT 'Identificador do repositório onde a composição do workflow está armazenada.',
  id_prosp_workflow int(11) NOT NULL COMMENT 'Identificador da composição do workflow.',  
  id_workflow int(11) NOT NULL COMMENT 'Identificador da execução do workflow.',  
  id_prosp_step_from int(11) NOT NULL COMMENT 'Identificador da composição do passo de origem da ligação.',
  id_prosp_step_to int(11) NOT NULL COMMENT 'Identificador da composição do passo de destino da ligação.',  
  id_prosp_field int(11) NOT NULL COMMENT 'Identificador do campo de dados.',  
  seq_from int(11) NOT NULL COMMENT 'Sequencial de execução do passo de origem no workflow.',
  seq_to int(11) NOT NULL COMMENT 'Sequencial de execução do passo de destino no workflow.',
  row_count int(11) NOT NULL COMMENT 'Número da linha de dados.',
  field_value text default NULL COMMENT 'Valor do campo de dados.',  
  PRIMARY KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field, id_workflow, seq_from, seq_to, row_count),
  KEY fk_prosp_hop_field (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field),
  KEY fk_step_from (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_from, seq_from),
  KEY fk_step_to (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_to, seq_to),
  CONSTRAINT cstr_retrosp_row_field_fk_prosp_hop_field FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_prosp_step_from, id_prosp_step_to, id_prosp_field) REFERENCES prosp_hop_field (id_repository, id_workflow, id_step_from, id_step_to, id_field),
  CONSTRAINT cstr_retrosp_row_field_fk_step_from FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_from, seq_from) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq),
  CONSTRAINT cstr_retrosp_row_field_fk_step_to FOREIGN KEY (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step_to, seq_to) REFERENCES retrosp_step (id_prosp_repository, id_prosp_workflow, id_workflow, id_prosp_step, seq)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Campo de uma linha de dados (proveniência retrospectiva).';

-- --------------------------------------------------------