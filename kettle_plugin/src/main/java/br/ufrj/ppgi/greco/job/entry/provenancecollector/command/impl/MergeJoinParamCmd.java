package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.ParentProspStepParamCmd;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;

public class MergeJoinParamCmd extends ParentProspStepParamCmd
{

    @Override
    public void insertProvenance(JobDecorator jobRoot, Database db,
            StepMeta sm, Long processId) throws KettleException
    {
        MergeJoinMeta mjm = (MergeJoinMeta) sm.getStepMetaInterface();

        // First Step
        String[] steps = mjm.getStepIOMeta().getInfoStepnames();
        if (steps[0] != null)
        {
            insertProspStepParam(jobRoot, db, sm, processId, "FIRST_STEP",
                    steps[0]);
        }

        // Second Step
        if (steps[1] != null)
        {
            insertProspStepParam(jobRoot, db, sm, processId, "SECOND_STEP",
                    steps[1]);
        }

        // Join Type
        String joinType = mjm.getJoinType();
        if (joinType != null)
        {
            insertProspStepParam(jobRoot, db, sm, processId, "JOIN_TYPE",
                    joinType);
        }

        // Keys 1
        String[] keyFields1 = mjm.getKeyFields1();
        if (keyFields1 != null)
        {
            for (int k = 0; k < keyFields1.length; k++)
            {
                insertProspStepParam(jobRoot, db, sm, processId, "KEYS1_" + k,
                        keyFields1[k]);
            }
        }

        // Keys 2
        String[] keyFields2 = mjm.getKeyFields2();
        if (keyFields2 != null)
        {
            for (int k = 0; k < keyFields2.length; k++)
            {
                insertProspStepParam(jobRoot, db, sm, processId, "KEYS2_" + k,
                        keyFields2[k]);
            }
        }
    }
}