rem Remove dictionary output directory
rmdir /S D:/projetos/provenance_collector/mysql/dictionary/output

rem Execute schemaSpy and generate dictionary of pdi_provenance
java -jar D:/programas/schemaSpy/schemaSpy_5.0.0.jar -t mysql -host localhost:3306 -db pdi_dicionario -u kettle -p kettle -o D:/projetos/provenance_collector/mysql/dictionary/output -dp C:/Users/rogers/.m2/repository/mysql/mysql-connector-java/5.1.12/mysql-connector-java-5.1.12.jar -gv D:/programas/schemaSpy/graphviz-2.34/release