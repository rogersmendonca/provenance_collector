--
-- Banco de Dados: pdi_provenance
--

DROP DATABASE IF EXISTS pdi_provenance;
CREATE DATABASE pdi_provenance;
GRANT ALL PRIVILEGES ON pdi_provenance.* TO 'kettle'@'%' IDENTIFIED BY 'kettle';
GRANT ALL PRIVILEGES ON pdi_provenance.* TO 'kettle'@'localhost' IDENTIFIED BY 'kettle';

USE pdi_provenance;

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";