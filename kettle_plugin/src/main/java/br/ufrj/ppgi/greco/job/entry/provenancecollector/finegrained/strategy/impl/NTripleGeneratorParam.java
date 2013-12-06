package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

public class NTripleGeneratorParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        StepMetaInterface smi = sm.getStepMetaInterface();

        try
        {
            // Obtem os valores por reflexion    
            String inputSubject = (String) smi.getClass().getMethod("getInputSubject").invoke(smi);
            String inputPredicate = (String) smi.getClass().getMethod("getInputPredicate").invoke(smi);
            String inputObject = (String) smi.getClass().getMethod("getInputObject").invoke(smi);
            String inputDataType = (String) smi.getClass().getMethod("getInputDataType").invoke(smi);
            String inputLangTag = (String) smi.getClass().getMethod("getInputLangTag").invoke(smi);
            String outputNTriple = (String) smi.getClass().getMethod("getOutputNTriple").invoke(smi);
            String innerIsLiteral = boolToStr((Boolean) smi.getClass().getMethod("getInnerIsLiteral").invoke(smi));
            String innerKeepInputFields = boolToStr((Boolean) smi.getClass().getMethod("getInnerKeepInputFields").invoke(smi));

            // Seta os valores no mapeamento
            stepParamMap.put("INPUT_SUBJECT", inputSubject);
            stepParamMap.put("INPUT_PREDICATE", inputPredicate);
            stepParamMap.put("INPUT_OBJECT", inputObject);
            stepParamMap.put("INPUT_DATA_TYPE", inputDataType);
            stepParamMap.put("INPUT_LANG_TAG", inputLangTag);
            stepParamMap.put("OUTPUT_NTRIPLE", outputNTriple);
            stepParamMap.put("INNER_IS_LITERAL", innerIsLiteral);
            stepParamMap.put("INNER_KEEP_INPUT_FIELDS", innerKeepInputFields);
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
