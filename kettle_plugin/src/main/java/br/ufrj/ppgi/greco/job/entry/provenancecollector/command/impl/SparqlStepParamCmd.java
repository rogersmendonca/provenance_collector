package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

public class SparqlStepParamCmd extends StepParameterCmd
{

    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap, StepMeta sm)
    {
        StepMetaInterface smi = sm.getStepMetaInterface();

        try
        {
            // Obtem valores dos campos por reflection
            String endpoint = (String) smi.getClass()
                    .getMethod("getEndpointUri").invoke(smi);
            String defaultGraph = (String) smi.getClass()
                    .getMethod("getDefaultGraph").invoke(smi);
            String sparqlInput = (String) smi.getClass()
                    .getMethod("getQueryString").invoke(smi);

            // Endpoint URI
            stepParamMap.put("ENDPOINT_URI", endpoint);

            // Default Graph
            stepParamMap.put("DEFAULT_GRAPH", defaultGraph);

            // SPARQL
            stepParamMap.put("SPARQL", sparqlInput);
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
