package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

public class GraphSemanticLevelMarkerParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        StepMetaInterface smi = sm.getStepMetaInterface();

        try
        {
            // Obtem os valores por reflexion
            String inputGraph = (String) smi.getClass().getMethod("getInputGraph").invoke(smi);
            String outputSubject = (String) smi.getClass().getMethod("getOutputSubject").invoke(smi);
            String outputPredicate = (String) smi.getClass().getMethod("getOutputPredicate").invoke(smi);
            String outputObject = (String) smi.getClass().getMethod("getOutputObject").invoke(smi);            
            String LOVFile = (String) smi.getClass().getMethod("getBrowseFilename").invoke(smi);
            String rulesFile = (String) smi.getClass().getMethod("getRulesFilename").invoke(smi);

            // Seta os valores no mapeamento
            stepParamMap.put("INPUT_GRAPH", inputGraph);
            stepParamMap.put("OUTPUT_SUBJECT", outputSubject);
            stepParamMap.put("OUTPUT_PREDICATE", outputPredicate);
            stepParamMap.put("OUTPUT_OBJECT", outputObject);
            stepParamMap.put("INPUT_LOV_FILE", LOVFile);
            stepParamMap.put("INPUT_RULES_FILE", rulesFile);            
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
