package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
public class TableInputParamCmd extends StepParameterCmd
{
    @Override
    public void populaStepParamMap(Map<String, String> stepParamMap, StepMeta sm)
    {
        TableInputMeta tim = (TableInputMeta) sm.getStepMetaInterface();

        // Database connection
        DatabaseMeta databaseMeta = tim.getDatabaseMeta();

        stepParamMap.put("connection",
                (databaseMeta != null) ? databaseMeta.getName() : null);

        // SQL
        String sqlInput = tim.getSQL();
        stepParamMap.put("sql", sqlInput);

        // LOOKUP_STEP
        StepMeta lookupStep = tim.getLookupFromStep();
        stepParamMap.put("lookup", (lookupStep != null) ? lookupStep.getName()
                : null);
    }
}
