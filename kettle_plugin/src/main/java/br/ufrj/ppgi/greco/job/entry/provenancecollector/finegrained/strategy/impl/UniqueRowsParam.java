package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.uniquerows.UniqueRowsMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class UniqueRowsParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        UniqueRowsMeta meta = (UniqueRowsMeta) sm.getStepMetaInterface();

        stepParamMap.put("count_rows", boolToStr(meta.isCountRows()));
        stepParamMap.put("count_field", meta.getCountField());
        stepParamMap.put("reject_duplicate_row", boolToStr(meta.isRejectDuplicateRow()));
        stepParamMap.put("error_description", meta.getErrorDescription());
        
        putListParamInStepParamMap(stepParamMap, "compare_fields", meta.getCompareFields());
        if (meta.getCaseInsensitive() != null) 
        {
            String[] arr = new String[meta.getCaseInsensitive().length];
            for(int i = 0; i < arr.length; i++)
            {
                arr[i] = boolToStr(meta.getCaseInsensitive()[i]);
            }
            putListParamInStepParamMap(stepParamMap, "compare_case_insensitive", arr);
        }
    }
}
