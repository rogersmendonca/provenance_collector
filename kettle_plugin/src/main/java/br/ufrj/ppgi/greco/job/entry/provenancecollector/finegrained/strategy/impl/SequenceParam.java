package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.addsequence.AddSequenceMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2014
 * 
 */
public class SequenceParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        AddSequenceMeta meta = (AddSequenceMeta) sm.getStepMetaInterface();
        
        stepParamMap.put("valuename", meta.getValuename());
        stepParamMap.put("useDatabase", boolToStr(meta.isDatabaseUsed()));
        if (meta.isDatabaseUsed()) 
        {
            stepParamMap.put("database",  meta.getDatabase().getDatabaseName());
            stepParamMap.put("schemaName", meta.getSchemaName());
            stepParamMap.put("sequenceName", meta.getSequenceName());
        }
        
        stepParamMap.put("useCounter", boolToStr(meta.isCounterUsed()));
        if (meta.isCounterUsed()) 
        {
            stepParamMap.put("counterName",  meta.getCounterName());
            stepParamMap.put("startAt", meta.getStartAt());
            stepParamMap.put("incrementBy", meta.getIncrementBy());
            stepParamMap.put("maxValue", meta.getMaxValue());
        }
    }
}
