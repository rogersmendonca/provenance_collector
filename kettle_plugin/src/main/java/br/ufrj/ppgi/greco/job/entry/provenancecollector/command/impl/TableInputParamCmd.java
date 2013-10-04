package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.ParentProspStepParamCmd;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;

public class TableInputParamCmd extends ParentProspStepParamCmd
{
    public void insertProvenance(JobDecorator jobRoot, Database db,
            StepMeta sm, Long processId) throws KettleException
    {
        TableInputMeta tim = (TableInputMeta) sm.getStepMetaInterface();

        // Database connection
        DatabaseMeta databaseMeta = tim.getDatabaseMeta();
        if (databaseMeta != null)
        {
            insertProspStepParam(jobRoot, db, sm, processId, "CONNECTION",
                    databaseMeta.getName());
        }

        // SQL
        String sqlInput = tim.getSQL();
        if (sqlInput != null)
        {
            insertProspStepParam(jobRoot, db, sm, processId, "SQL", sqlInput);
        }

        // LOOKUP_STEP
        StepMeta lookupStep = tim.getLookupFromStep();
        if (lookupStep != null)
        {
            insertProspStepParam(jobRoot, db, sm, processId, "LOOKUP_STEP",
                    lookupStep.getName());
        }

    }
}
