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
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        TableInputMeta tim = (TableInputMeta) sm.getStepMetaInterface();

        // Database connection
        DatabaseMeta databaseMeta = tim.getDatabaseMeta();

        if (databaseMeta != null)
        {
            stepParamMap.put("connection", databaseMeta.getName());
            stepParamMap.put("database_type", databaseMeta.getPluginId());
            stepParamMap.put("database_driver", databaseMeta.getDriverClass());
            stepParamMap.put("database_host", databaseMeta.getHostname());
            stepParamMap.put("database_name", databaseMeta.getDatabaseName());
            stepParamMap.put("database_port",
                    databaseMeta.getDatabasePortNumberString());
            stepParamMap.put("database_username", databaseMeta.getUsername());
        }

        stepParamMap.put("sql", tim.getSQL());
        stepParamMap.put("limit", tim.getRowLimit());

        // LOOKUP_STEP
        StepMeta lookupStep = tim.getLookupFromStep();
        stepParamMap.put("lookup", (lookupStep != null) ? lookupStep.getName()
                : null);

        stepParamMap.put("execute_each_row",
                boolToStr(tim.isExecuteEachInputRow()));
        stepParamMap.put("variables_active",
                boolToStr(tim.isVariableReplacementActive()));
        stepParamMap.put("lazy_conversion_active",
                boolToStr(tim.isLazyConversionActive()));
    }
}
