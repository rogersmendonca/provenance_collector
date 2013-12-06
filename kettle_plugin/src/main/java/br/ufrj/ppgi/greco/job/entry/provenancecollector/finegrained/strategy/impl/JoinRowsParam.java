package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.joinrows.JoinRowsMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class JoinRowsParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        JoinRowsMeta meta = (JoinRowsMeta) sm.getStepMetaInterface();

        stepParamMap.put("directory", meta.getDirectory());
        stepParamMap.put("prefix", meta.getPrefix());
        stepParamMap.put("cache_size", String.valueOf(meta.getCacheSize()));

        stepParamMap.put("main", meta.getLookupStepname());

        if (meta.getCondition() != null)
        {
            stepParamMap.put("condition", meta.getCondition().toString());
        }
    }
}
