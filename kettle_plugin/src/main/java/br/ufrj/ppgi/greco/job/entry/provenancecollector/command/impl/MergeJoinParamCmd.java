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
    public void populateStepParamMap(Map<String, String> stepParamMap, StepMeta sm)
    {
        MergeJoinMeta mjm = (MergeJoinMeta) sm.getStepMetaInterface();
        
        // First Step
        String[] steps = mjm.getStepIOMeta().getInfoStepnames();
        stepParamMap.put("step1", steps[0]);

        // Second Step
        stepParamMap.put("step2", steps[1]);

        // Join Type
        String joinType = mjm.getJoinType();
        stepParamMap.put("join_type", joinType);

        // Keys 1
        String[] keyFields1 = mjm.getKeyFields1();
        if (keyFields1 != null)
        {
            for (int k = 0; k < keyFields1.length; k++)
            {
                stepParamMap.put("keys_1_" + k, keyFields1[k]);
            }
        }

        // Keys 2
        String[] keyFields2 = mjm.getKeyFields2();
        if (keyFields2 != null)
        {
            for (int k = 0; k < keyFields2.length; k++)
            {
                stepParamMap.put("keys_2_" + k, keyFields2[k]);
            }
        }
    }
}
