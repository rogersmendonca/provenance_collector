package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

public class RDFDataPropertyMappingParamCmd extends StepParameterCmd
{

    @SuppressWarnings("unchecked")
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        StepMetaInterface smi = sm.getStepMetaInterface();

        try
        {
            // Obtem os valores por reflexion            
            List<String> rdfTypeUris = (List<String>) smi.getClass().getMethod("getRdfTypeUris").invoke(smi);

            String subjectUriFieldName = (String) smi.getClass().getMethod("getSubjectUriFieldName").invoke(smi);
                        
            Object mapTable = smi.getClass().getMethod("getMapTable").invoke(smi);

            String subjectOutputFieldName = (String) smi.getClass().getMethod("getSubjectOutputFieldName").invoke(smi);
            String predicateOutputFieldName = (String) smi.getClass().getMethod("getPredicateOutputFieldName").invoke(smi);
            String objectOutputFieldName = (String) smi.getClass().getMethod("getObjectOutputFieldName").invoke(smi);
            String datatypeOutputFieldName = (String) smi.getClass().getMethod("getDatatypeOutputFieldName").invoke(smi);
            String langTagOutputFieldName = (String) smi.getClass().getMethod("getLangTagOutputFieldName").invoke(smi);
            String keepInputFields = boolToStr((Boolean) smi.getClass().getMethod("isKeepInputFields").invoke(smi));                    

            // Seta os valores no mapeamento
            if (rdfTypeUris != null)
            {
                putListParamInStepParamMap(stepParamMap, "RDF_TYPE_URI", rdfTypeUris.toArray(new String[0]));
            }
            stepParamMap.put("SUBJECT_URI_FIELD_NAME", subjectUriFieldName);
            
            if (mapTable != null) 
            {                
                List<List<String>> data = (List<List<String>>) mapTable.getClass().getMethod("getData").invoke(mapTable);
                List<String> header = (List<String>) mapTable.getClass().getMethod("getHeader").invoke(mapTable);
                for(int i = 0; i < data.size(); i++)
                {
                    List<String> row = data.get(i);
                    for(int j = 0; j < row.size(); j++) 
                    {
                        String paramName = String.format("ROW_%s#%d", header.get(j), i);
                        stepParamMap.put(paramName, row.get(j));
                    }
                }
            }
            
            stepParamMap.put("SUBJECT_OUT_FIELD_NAME", subjectOutputFieldName);
            stepParamMap.put("PREDICATE_OUT_FIELD_NAME", predicateOutputFieldName);
            stepParamMap.put("OBJECT_OUT_FIELD_NAME", objectOutputFieldName);
            stepParamMap.put("DATATYPE_OUT_FIELD_NAME", datatypeOutputFieldName);
            stepParamMap.put("LANGTAG_OUT_FIELD_NAME", langTagOutputFieldName);
            stepParamMap.put("KEEP_INPUT_FIELDS", keepInputFields);
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
