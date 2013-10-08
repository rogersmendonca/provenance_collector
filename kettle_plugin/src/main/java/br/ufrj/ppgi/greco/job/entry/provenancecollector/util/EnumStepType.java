package br.ufrj.ppgi.greco.job.entry.provenancecollector.util;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.accessinput.AccessInputMeta;
import org.pentaho.di.trans.steps.accessoutput.AccessOutputMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.di.trans.steps.formula.FormulaMeta;
import org.pentaho.di.trans.steps.insertupdate.InsertUpdateMeta;
import org.pentaho.di.trans.steps.janino.JaninoMeta;
import org.pentaho.di.trans.steps.javafilter.JavaFilterMeta;
import org.pentaho.di.trans.steps.joinrows.JoinRowsMeta;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta;
import org.pentaho.di.trans.steps.ldapinput.LDAPInputMeta;
import org.pentaho.di.trans.steps.ldapoutput.LDAPOutputMeta;
import org.pentaho.di.trans.steps.ldifinput.LDIFInputMeta;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;
import org.pentaho.di.trans.steps.mondrianinput.MondrianInputMeta;
import org.pentaho.di.trans.steps.olapinput.OlapInputMeta;
import org.pentaho.di.trans.steps.propertyinput.PropertyInputMeta;
import org.pentaho.di.trans.steps.propertyoutput.PropertyOutputMeta;
import org.pentaho.di.trans.steps.replacestring.ReplaceStringMeta;
import org.pentaho.di.trans.steps.rssinput.RssInputMeta;
import org.pentaho.di.trans.steps.rssoutput.RssOutputMeta;
import org.pentaho.di.trans.steps.sapinput.SapInputMeta;
import org.pentaho.di.trans.steps.script.ScriptMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.trans.steps.xmlinput.XMLInputMeta;
import org.pentaho.di.trans.steps.xmloutput.XMLOutputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.JobEntryProvenanceCollector;
import br.ufrj.ppgi.greco.lodbr.plugin.sparql.SparqlStepMeta;

/**
 * List of potential step types to collect fine grained provenance data.
 * 
 * @author Rogers Reiche de Mendonca
 * @since mar-2013
 * 
 */
public enum EnumStepType
{
    // Input
    CSV_INPUT("BaseStep.TypeTooltipDesc.CsvInput", EnumETLOperation.E, CsvInputMeta.class),
    DATA_GRID("BaseStep.TypeTooltipDesc.DataGrid", EnumETLOperation.E, DataGridMeta.class),    
    JSON_INPUT("BaseStep.TypeTooltipDesc.JsonInput", EnumETLOperation.E, JsonInputMeta.class),
    LDAP_INPUT("BaseStep.TypeTooltipDesc.LDAPInput", EnumETLOperation.E, LDAPInputMeta.class),
    LDIF_INPUT("BaseStep.TypeTooltipDesc.LDIFInput", EnumETLOperation.E, LDIFInputMeta.class),
    MICROSOFT_ACCESS_INPUT("BaseStep.TypeTooltipDesc.AccessInput", EnumETLOperation.E, AccessInputMeta.class),
    MICROSOFT_EXCEL_INPUT("BaseStep.TypeTooltipDesc.ExcelInput", EnumETLOperation.E, ExcelInputMeta.class),
    MONDRIAN_INPUT("BaseStep.TypeTooltipDesc.MondrianInput", EnumETLOperation.E, MondrianInputMeta.class),
    OLAP_INPUT("BaseStep.TypeTooltipDesc.OlapInput", EnumETLOperation.E, OlapInputMeta.class),
    PROPERTY_INPUT("BaseStep.TypeTooltipDesc.PropertyInput", EnumETLOperation.E, PropertyInputMeta.class),
    RSS_INPUT("BaseStep.TypeLongDesc.RssInput", EnumETLOperation.E, RssInputMeta.class),
    SAP_INPUT("BaseStep.TypeTooltipDesc.SapInput", EnumETLOperation.E, SapInputMeta.class),
    SPARQL_INPUT("CustomStep.TypeTooltipDesc.SparqlEndpoint", EnumETLOperation.E, SparqlStepMeta.class), // implementado
    TABLE_INPUT("BaseStep.TypeTooltipDesc.TableInput", EnumETLOperation.E, TableInputMeta.class), // implementado
    TEXT_INPUT("BaseStep.TypeTooltipDesc.TextInputFile", EnumETLOperation.E, TextFileInputMeta.class),
    XML_INPUT("BaseStep.TypeTooltipDesc.XMLInput", EnumETLOperation.E, XMLInputMeta.class),
    
    // Transform
    CALCULATOR("BaseStep.TypeTooltipDesc.Calculator", EnumETLOperation.T, CalculatorMeta.class),
    FILTER_ROWS("BaseStep.TypeTooltipDesc.FilterRows", EnumETLOperation.T, FilterRowsMeta.class),
    FORMULA("BaseStep.TypeTooltipDesc.Formula", EnumETLOperation.T, FormulaMeta.class),
    JAVA_FILTER("BaseStep.TypeTooltipDesc.JavaFilter", EnumETLOperation.T, JavaFilterMeta.class),
    JAVACRIPT("BaseStep.TypeTooltipDesc.JavaScriptValueMod", EnumETLOperation.T, ScriptMeta.class),
    JAVA_EXPRESSION("BaseStep.TypeTooltipDesc.Janino", EnumETLOperation.T, JaninoMeta.class),
    JOIN_ROWS("BaseStep.TypeTooltipDesc.JoinRows", EnumETLOperation.T, JoinRowsMeta.class),
    MERGE_JOIN("BaseStep.TypeTooltipDesc.MergeJoin", EnumETLOperation.T, MergeJoinMeta.class), // implementado
    //RDF_DATA_PROPERTY_MAPPING("CustomStep.TypeTooltipDesc.RDFDataMapping", EnumETLOperation.T, DataPropertyMappingStepMeta.class),
    //RDF_NTRIPLE_GENERATOR("CustomStep.TypeTooltipDesc.NTripleGenerator", EnumETLOperation.T, NTripleGeneratorStepMeta.class),
    //RDF_OBJECT_PROPERTY_MAPPING("CustomStep.TypeTooltipDesc.RDFObjectMapping", EnumETLOperation.T, ObjectPropertyMappingStepMeta.class),
    REPLACE_STRING("BaseStep.TypeTooltipDesc.ReplaceString", EnumETLOperation.T, ReplaceStringMeta.class),
    SELECT_VALUES("BaseStep.TypeTooltipDesc.SelectValues", EnumETLOperation.T, SelectValuesMeta.class),
    STRING_OPERATIONS("BaseStep.TypeTooltipDesc.StringOperations", EnumETLOperation.T, StringOperationsMeta.class),    
    
    // Load      
    INSERT_UPDATE_OUTPUT("BaseStep.TypeTooltipDesc.InsertOrUpdate", EnumETLOperation.L, InsertUpdateMeta.class),    
    JSON_OUTPUT("BaseStep.TypeLongDesc.JsonOutput", EnumETLOperation.L, JsonOutputMeta.class),
    LDAP_OUTPUT("BaseStep.TypeTooltipDesc.LDAPOutput", EnumETLOperation.L, LDAPOutputMeta.class),
    MICROSOFT_ACCESS_OUTPUT("BaseStep.TypeTooltipDesc.AccessOutput", EnumETLOperation.L, AccessOutputMeta.class),
    MICROSOFT_EXCEL_OUTPUT("BaseStep.TypeTooltipDesc.ExcelOutput", EnumETLOperation.L, ExcelOutputMeta.class),
    PROPERTY_OUTPUT("BaseStep.TypeTooltipDesc.PropertyOutput", EnumETLOperation.L, PropertyOutputMeta.class),
    RSS_OUTPUT("BaseStep.TypeTooltipDesc.RssOutput", EnumETLOperation.L, RssOutputMeta.class),
    //SPARQL_OUTPUT("CustomStep.TypeTooltipDesc.SparqlUpdateInsert", EnumETLOperation.L, SparqlUpdateInsertStepMeta.class),
    TABLE_OUTPUT("BaseStep.TypeTooltipDesc.TableOutput", EnumETLOperation.L, TableOutputMeta.class),
    TEXT_OUTPUT("BaseStep.TypeTooltipDesc.TextOutputFile", EnumETLOperation.L, TextFileOutputMeta.class),
    XML_OUTPUT("BaseStep.TypeTooltipDesc.XMLOutput", EnumETLOperation.L, XMLOutputMeta.class),    
    ;     

    private String description;
    private EnumETLOperation operation;
    private Class<?> clazz;

    EnumStepType(String descriptionKey, EnumETLOperation operation,
            Class<?> clazz)
    {
        // for i18n purposes, needed by Translator2!! $NON-NLS-1$
        Class<?> PKG = JobEntryProvenanceCollector.class;
        this.description = BaseMessages.getString(PKG, descriptionKey);
        this.operation = operation;
        this.clazz = clazz;
    }

    public String getDescription()
    {
        return this.description;
    }

    public EnumETLOperation getOperation()
    {
        return this.operation;
    }

    public Class<?> getClazz()
    {
        return clazz;
    }

    public static EnumStepType valueOf(Class<?> clazz)
    {
        EnumStepType ret = null;
        for (EnumStepType type : values())
        {
            if (type.getClazz().getName().equals(clazz.getName()))
            {
                ret = type;
                break;
            }
        }
        return ret;
    }
}
