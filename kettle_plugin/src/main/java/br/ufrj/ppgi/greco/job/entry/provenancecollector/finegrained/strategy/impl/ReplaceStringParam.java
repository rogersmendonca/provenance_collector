package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.replacestring.ReplaceStringMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class ReplaceStringParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        ReplaceStringMeta meta = (ReplaceStringMeta) sm.getStepMetaInterface();
        
        putListParamInStepParamMap(stepParamMap, "field_in_stream", meta.getFieldInStream());
        putListParamInStepParamMap(stepParamMap, "field_out_stream", meta.getFieldOutStream());
        if (meta.getUseRegEx() != null) 
        {
            String[] arr = new String[meta.getUseRegEx().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = ReplaceStringMeta.getUseRegExDesc(i);
                putListParamInStepParamMap(stepParamMap, "use_regex", arr);        
            }
        }
        putListParamInStepParamMap(stepParamMap, "replace_string", meta.getReplaceString());
        putListParamInStepParamMap(stepParamMap, "replace_by_string", meta.getReplaceByString());
        putListParamInStepParamMap(stepParamMap, "replace_by_field", meta.getFieldReplaceByString());
        if (meta.getWholeWord() != null) 
        {
            String[] arr = new String[meta.getWholeWord().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = ReplaceStringMeta.getWholeWordDesc(i);
                putListParamInStepParamMap(stepParamMap, "whole_word", arr);        
            }
        }
        if (meta.getCaseSensitive() != null) 
        {
            String[] arr = new String[meta.getCaseSensitive().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = ReplaceStringMeta.getCaseSensitiveDesc(i);
                putListParamInStepParamMap(stepParamMap, "case_sensitive", arr);        
            }
        }
    }
}
