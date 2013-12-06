package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.sort.SortRowsMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class SortRowsParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        SortRowsMeta meta = (SortRowsMeta) sm.getStepMetaInterface();

        stepParamMap.put("directory", meta.getDirectory());
        stepParamMap.put("prefix", meta.getPrefix());
        stepParamMap.put("sort_size", meta.getSortSize());
        stepParamMap.put("free_memory", meta.getFreeMemoryLimit());
        stepParamMap.put("compress", boolToStr(meta.getCompressFiles()));
        stepParamMap.put("compress_variable", meta.getCompressFilesVariable());
        stepParamMap.put("unique_rows", boolToStr(meta.isOnlyPassingUniqueRows()));

        putListParamInStepParamMap(stepParamMap, "field", meta.getFieldName());
        
        if (meta.getAscending() != null) 
        {
            String[] arrOrder = new String[meta.getAscending().length];
            for(int i = 0; i < arrOrder.length; i++)
            {
                arrOrder[i] = meta.getAscending()[i] ? "ASC" : "DESC";
            }
            putListParamInStepParamMap(stepParamMap, "field_ascending", arrOrder);
        }
        
        if (meta.getCaseSensitive() != null) 
        {
            String[] arr = new String[meta.getCaseSensitive().length];
            for(int i = 0; i < arr.length; i++)
            {
                arr[i] = boolToStr(meta.getCaseSensitive()[i]);
            }
            putListParamInStepParamMap(stepParamMap, "field_case_sensitive", arr);
        }
    }
}
