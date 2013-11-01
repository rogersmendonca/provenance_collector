Provenance Collector Agent 
===========================
Description: Pentaho Data Integration (a.k.a. Kettle) Job Entry that collects retrospective and prospective provenance data during a Kettle Job execution.

Author: Rogers Reiche de Mendonça (rogers.rj@gmail.com)

Installation:
* Temporary provenance tables
	- Create a database in MySQL
	- Execute the script ./mysql/SQL/PROVENANCE__COLLECTOR_DDL_CREATE.sql

* Kettle Job Entry
	- Update the pdi.home property in ./kettle_plugin/pom.xml to the Kettle home directory
	- Run "mvn install" in ./kettle_plugin
