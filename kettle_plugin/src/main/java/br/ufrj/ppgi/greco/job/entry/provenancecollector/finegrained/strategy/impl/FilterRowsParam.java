package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class FilterRowsParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        FilterRowsMeta meta = (FilterRowsMeta) sm.getStepMetaInterface();

        stepParamMap.put("condition", meta.getCondition().toString());

        String[] stepNames = meta.getStepIOMeta().getTargetStepnames();
        if ((stepNames != null) && (stepNames.length >= 2))
        {
            stepParamMap.put("send_true_to", stepNames[0]);
            stepParamMap.put("send_false_to", stepNames[1]);
        }
    }
}
