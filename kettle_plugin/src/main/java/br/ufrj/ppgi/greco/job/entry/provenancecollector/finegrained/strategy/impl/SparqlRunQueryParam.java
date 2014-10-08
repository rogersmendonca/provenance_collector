package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

public class SparqlRunQueryParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        StepMetaInterface smi = sm.getStepMetaInterface();

        try
        {
            // Obtem os valores por reflexion    
            String queryFieldName = (String) smi.getClass().getMethod("getQueryTextContentFieldName").invoke(smi);
            String endpointUrl = (String) smi.getClass().getMethod("getEndpointUrl").invoke(smi);
            String username = (String) smi.getClass().getMethod("getUsername").invoke(smi);
            String resultCodeFieldName = (String) smi.getClass().getMethod("getResultCodeFieldName").invoke(smi);
            String resultMessageFieldName = (String) smi.getClass().getMethod("getResultMessageFieldName").invoke(smi);

            // Seta os valores no mapeamento
            // Seta os valores no mapeamento
            stepParamMap.put("QUERY_FIELD", queryFieldName);
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
