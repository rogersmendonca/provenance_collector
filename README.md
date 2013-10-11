Provenance Collector Agent (Pentaho Kettle)
===========================================

Author: Rogers Reiche de Mendonça (rogers.rj@gmail.com)

- Installation
A) Temporary provenance table
1- Create a database in MySQL
2- Execute the script ./mysql/SQL/PROVENANCE__COLLECTOR_DDL_CREATE.sql

B) Kettle Job Entry
1- Update the pdi.home property in ./kettle_plugin/pom.xml to the Kettle home directory
2- Run "mvn install" in ./kettle_plugin
