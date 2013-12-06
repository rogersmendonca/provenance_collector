package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

public class SparqlUpdateOutputParam extends StepParameter
{

    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap, StepMeta sm)
    {
        StepMetaInterface smi = sm.getStepMetaInterface();
        
        try
        {
            // Obtem valores dos campos por reflection
            String rdfContentFieldName = (String) smi.getClass().getMethod("getRdfContentFieldName").invoke(smi);
            String graphUriValue = (String) smi.getClass().getMethod("getGraphUriValue").invoke(smi);
            String clearGraph = boolToStr((Boolean) smi.getClass().getMethod("getClearGraph").invoke(smi));
            String endpointUrl = (String) smi.getClass().getMethod("getEndpointUrl").invoke(smi);
            String username = (String) smi.getClass().getMethod("getUsername").invoke(smi);
            String resultCodeFieldName = (String) smi.getClass().getMethod("getResultCodeFieldName").invoke(smi);
            String resultMessageFieldName = (String) smi.getClass().getMethod("getResultMessageFieldName").invoke(smi);
            
            // Seta os valores no mapeamento
            stepParamMap.put("RDF_CONTENT", rdfContentFieldName);
            stepParamMap.put("GRAPH_URI", graphUriValue);
            stepParamMap.put("CLEAR_GRAPH", clearGraph);
            stepParamMap.put("ENDPOINT_URI", endpointUrl);
            stepParamMap.put("USERNAME", username);
            stepParamMap.put("OUT_CODE", resultCodeFieldName);
            stepParamMap.put("OUT_MESSAGE", resultMessageFieldName);        
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
