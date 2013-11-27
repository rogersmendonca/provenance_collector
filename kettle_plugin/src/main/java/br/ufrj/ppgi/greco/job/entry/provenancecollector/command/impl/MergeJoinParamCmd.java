package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
public class MergeJoinParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        MergeJoinMeta mjm = (MergeJoinMeta) sm.getStepMetaInterface();

        // Join Type
        String joinType = mjm.getJoinType();
        stepParamMap.put("join_type", joinType);

        String[] steps = mjm.getStepIOMeta().getInfoStepnames();
        if ((steps != null) && (steps.length >= 2))
        {
            // First Step
            stepParamMap.put("step1", steps[0]);

            // Second Step
            stepParamMap.put("step2", steps[1]);

            // Keys 1
            putListParamInStepParamMap(stepParamMap, "key1",
                    mjm.getKeyFields1());

            // Keys 2
            putListParamInStepParamMap(stepParamMap, "key2",
                    mjm.getKeyFields2());
        }

    }
}
