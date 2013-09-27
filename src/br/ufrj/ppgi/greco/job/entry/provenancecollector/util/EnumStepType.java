package br.ufrj.ppgi.greco.job.entry.provenancecollector.util;

import org.pentaho.di.i18n.BaseMessages;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.JobEntryProvenanceCollector;

/**
 * List of potential step types to collect fine grained provenance data.
 * 
 * @author rogers (maio/2013)
 * 
 */
public enum EnumStepType
{
    // Input
    CSV_INPUT("BaseStep.TypeTooltipDesc.CsvInput", EnumETLOperation.E),
    DATA_GRID("BaseStep.TypeTooltipDesc.DataGrid", EnumETLOperation.E),    
    JSON_INPUT("BaseStep.TypeTooltipDesc.JsonInput", EnumETLOperation.E),
    LDAP_INPUT("BaseStep.TypeTooltipDesc.LDAPInput", EnumETLOperation.E),
    LDIF_INPUT("BaseStep.TypeTooltipDesc.LDIFInput", EnumETLOperation.E),
    MICROSOFT_ACCESS_INPUT("BaseStep.TypeTooltipDesc.AccessInput", EnumETLOperation.E),
    MICROSOFT_EXCEL_INPUT("BaseStep.TypeTooltipDesc.ExcelInput", EnumETLOperation.E),
    MONDRIAN_INPUT("BaseStep.TypeTooltipDesc.MondrianInput", EnumETLOperation.E),
    OLAP_INPUT("BaseStep.TypeTooltipDesc.OlapInput", EnumETLOperation.E),
    PROPERTY_INPUT("BaseStep.TypeTooltipDesc.PropertyInput", EnumETLOperation.E),
    RSS_INPUT("BaseStep.TypeLongDesc.RssInput", EnumETLOperation.E),
    SAP_INPUT("BaseStep.TypeTooltipDesc.SapInput", EnumETLOperation.E),
    SPARQL_INPUT("CustomStep.TypeTooltipDesc.SparqlEndpoint", EnumETLOperation.E), // implementado
    TABLE_INPUT("BaseStep.TypeTooltipDesc.TableInput", EnumETLOperation.E), // implementado
    TEXT_INPUT("BaseStep.TypeTooltipDesc.TextInputFile", EnumETLOperation.E),
    XML_INPUT("BaseStep.TypeTooltipDesc.XMLInput", EnumETLOperation.E),
    
    // Transform
    CALCULATOR("BaseStep.TypeTooltipDesc.Calculator", EnumETLOperation.T),
    FILTER_ROWS("BaseStep.TypeTooltipDesc.FilterRows", EnumETLOperation.T),
    FORMULA("BaseStep.TypeTooltipDesc.Formula", EnumETLOperation.T),
    JAVA_FILTER("BaseStep.TypeTooltipDesc.JavaFilter", EnumETLOperation.T),
    JAVACRIPT("BaseStep.TypeTooltipDesc.JavaScriptValueMod", EnumETLOperation.T),
    JAVA_EXPRESSION("BaseStep.TypeTooltipDesc.Janino", EnumETLOperation.T),
    JOIN_ROWS("BaseStep.TypeTooltipDesc.JoinRows", EnumETLOperation.T),
    MERGE_JOIN("BaseStep.TypeTooltipDesc.MergeJoin", EnumETLOperation.T), // implementado
    RDF_DATA_PROPERTY_MAPPING("CustomStep.TypeTooltipDesc.RDFDataMapping", EnumETLOperation.T),
    RDF_NTRIPLE_GENERATOR("CustomStep.TypeTooltipDesc.NTripleGenerator", EnumETLOperation.T),
    RDF_OBJECT_PROPERTY_MAPPING("CustomStep.TypeTooltipDesc.RDFObjectMapping", EnumETLOperation.T),
    REPLACE_STRING("BaseStep.TypeTooltipDesc.ReplaceString", EnumETLOperation.T),
    SELECT_VALUES("BaseStep.TypeTooltipDesc.SelectValues", EnumETLOperation.T),
    STRING_OPERATIONS("BaseStep.TypeTooltipDesc.StringOperations", EnumETLOperation.T),    
    
    // Load      
    INSERT_UPDATE_OUTPUT("BaseStep.TypeTooltipDesc.InsertOrUpdate", EnumETLOperation.L),    
    JSON_OUTPUT("BaseStep.TypeLongDesc.JsonOutput", EnumETLOperation.L),
    LDAP_OUTPUT("BaseStep.TypeTooltipDesc.LDAPOutput", EnumETLOperation.L),
    MICROSOFT_ACCESS_OUTPUT("BaseStep.TypeTooltipDesc.AccessOutput", EnumETLOperation.L),
    MICROSOFT_EXCEL_OUTPUT("BaseStep.TypeTooltipDesc.ExcelOutput", EnumETLOperation.L),
    PROPERTY_OUTPUT("BaseStep.TypeTooltipDesc.PropertyOutput", EnumETLOperation.L),
    RSS_OUTPUT("BaseStep.TypeTooltipDesc.RssOutput", EnumETLOperation.L),
    SPARQL_OUTPUT("CustomStep.TypeTooltipDesc.SparqlUpdateInsert", EnumETLOperation.L),
    TABLE_OUTPUT("BaseStep.TypeTooltipDesc.TableOutput", EnumETLOperation.L),
    TEXT_OUTPUT("BaseStep.TypeTooltipDesc.TextOutputFile", EnumETLOperation.L),
    XML_OUTPUT("BaseStep.TypeTooltipDesc.XMLOutput", EnumETLOperation.L),    
    ;     

    private String description;
    private EnumETLOperation operation;

    EnumStepType(String descriptionKey, EnumETLOperation operation)
    {
        // for i18n purposes, needed by Translator2!! $NON-NLS-1$
        Class<?> PKG = JobEntryProvenanceCollector.class;
        this.description = BaseMessages.getString(PKG, descriptionKey);
        this.operation = operation;
    }

    public String getDescription()
    {
        return this.description;
    }

    public EnumETLOperation getOperation()
    {
        return this.operation;
    }
}
