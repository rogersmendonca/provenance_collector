-- phpMyAdmin SQL Dump
-- version 2.11.4
-- http://www.phpmyadmin.net
--
-- Servidor: localhost
-- Tempo de Geração: Fev 11, 2013 as 06:16 PM
-- Versão do Servidor: 5.0.51
-- Versão do PHP: 5.2.5

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Banco de Dados: `pdi_exercicio_log3`
--

-- --------------------------------------------------------

--
-- Estrutura da tabela `prosp_job`
--

CREATE TABLE IF NOT EXISTS `prosp_job` (
  `id` int(11) NOT NULL auto_increment,
  `repo_id` varchar(100) NOT NULL,
  `repo_loc` varchar(100) NOT NULL,
  `object_id` varchar(100) NOT NULL,
  `modified_date` datetime NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(100) default NULL,
  `create_user` varchar(100) default NULL,
  `create_date` datetime NOT NULL,
  `modified_user` varchar(100) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=23 ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `prosp_jobentry`
--

CREATE TABLE IF NOT EXISTS `prosp_jobentry` (
  `id` int(11) NOT NULL,
  `id_prosp_job` int(11) NOT NULL,
  `type` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(100) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `prosp_jobentry_attr`
--

CREATE TABLE IF NOT EXISTS `prosp_jobentry_attr` (
  `id_prosp_je` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `value` varchar(100) default NULL,
  `type` varchar(100) default NULL,
  `ind` int(11) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `prosp_jobentry_copy`
--

CREATE TABLE IF NOT EXISTS `prosp_jobentry_copy` (
  `id` int(11) NOT NULL,
  `id_prosp_je` int(11) NOT NULL,
  `nr` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `prosp_job_hop`
--

CREATE TABLE IF NOT EXISTS `prosp_job_hop` (
  `id_prosp_job` int(11) NOT NULL,
  `from_prosp_jec_id` int(11) NOT NULL,
  `to_prosp_jec_id` int(11) NOT NULL,
  `enabled` char(1) NOT NULL default 'N',
  `evaluation` char(1) NOT NULL default 'N',
  `unconditional` char(1) NOT NULL default 'N'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `prosp_job_note`
--

CREATE TABLE IF NOT EXISTS `prosp_job_note` (
  `id_prosp_job` int(11) NOT NULL,
  `id_prosp_note` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `prosp_note`
--

CREATE TABLE IF NOT EXISTS `prosp_note` (
  `id` int(11) NOT NULL,
  `text` text NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `retrosp_job`
--

CREATE TABLE IF NOT EXISTS `retrosp_job` (
  `id_job` int(11) NOT NULL,
  `id_prosp_job` int(11) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `finish_date` timestamp NULL default NULL,
  `user` varchar(100) NOT NULL,
  `success` char(1) NOT NULL default 'N'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `retrosp_jobentry`
--

CREATE TABLE IF NOT EXISTS `retrosp_jobentry` (
  `id_job` int(11) NOT NULL,
  `id_prosp_jec` int(11) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `finish_date` timestamp NULL default NULL,
  `seq` int(11) NOT NULL default '0',
  `success` char(1) NOT NULL default 'N'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `retrosp_step`
--

CREATE TABLE IF NOT EXISTS `retrosp_step` (
  `trans_id` int(5) NOT NULL,
  `trans_name` varchar(500) NOT NULL,
  `step_copy` int(5) NOT NULL,
  `step_name` varchar(500) NOT NULL,
  `changed_date` datetime default NULL,
  `java_class` varchar(500) NOT NULL,
  `cluster_schema` varchar(500) default NULL,
  `copies` int(11) default NULL,
  `description` varchar(500) default NULL,
  `holder_type` varchar(500) default NULL,
  `object_id` varchar(500) default NULL,
  `type_id` varchar(500) NOT NULL,
  `xml` text,
  `date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `event` char(1) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `retrosp_step_row`
--

CREATE TABLE IF NOT EXISTS `retrosp_step_row` (
  `trans_id` varchar(500) NOT NULL,
  `trans_name` varchar(500) NOT NULL,
  `step_copy` int(5) NOT NULL,
  `step_name` varchar(500) NOT NULL,
  `row_count` int(5) NOT NULL,
  `field_name` varchar(500) NOT NULL,
  `field_value` varchar(500) default NULL,
  `origin` varchar(500) default NULL,
  `date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `event` char(1) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `retrosp_trans`
--

CREATE TABLE IF NOT EXISTS `retrosp_trans` (
  `id_batch` int(11) NOT NULL,
  `dep_date` datetime default NULL,
  `end_date` datetime default NULL,
  `ended` int(1) default NULL,
  `errors` int(11) default NULL,
  `file_name` varchar(500) default NULL,
  `log_date` datetime default NULL,
  `mapping_step` varchar(500) default NULL,
  `name` varchar(500) default NULL,
  `object_id` varchar(500) default NULL,
  `object_name` varchar(500) default NULL,
  `object_type` varchar(500) default NULL,
  `parent_job_id` int(11) default NULL,
  `parent_trans_id` int(11) default NULL,
  `rep_date` datetime default NULL,
  `repository` varchar(500) default NULL,
  `rep_directory` varchar(500) default NULL,
  `result_rows` int(11) default NULL,
  `row_count` int(11) default NULL,
  `start_date` datetime default NULL,
  `status` varchar(500) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `retrosp_trans_field`
--

CREATE TABLE IF NOT EXISTS `retrosp_trans_field` (
  `id_batch` int(11) NOT NULL,
  `trans_name` varchar(500) default NULL,
  `field_name` varchar(500) default NULL,
  `field_value` varchar(500) default NULL,
  `step_origin` varchar(500) default NULL,
  `step_dest` varchar(500) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
