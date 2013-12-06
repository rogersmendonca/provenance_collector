package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class TableOutputParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        TableOutputMeta meta = (TableOutputMeta) sm.getStepMetaInterface();

        // Database connection
        DatabaseMeta databaseMeta = meta.getDatabaseMeta();

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
        
        stepParamMap.put("schema", meta.getSchemaName());
        stepParamMap.put("table", meta.getTablename());
        stepParamMap.put("commit", meta.getCommitSize());
        stepParamMap.put("truncate", boolToStr(meta.truncateTable()));
        stepParamMap.put("ignore_errors", boolToStr(meta.ignoreErrors()));
        stepParamMap.put("use_batch", boolToStr(meta.useBatchUpdate()));
        stepParamMap.put("specify_fields", boolToStr(meta.specifyFields()));

        stepParamMap.put("partitioning_enabled", boolToStr(meta.isPartitioningEnabled()));
        stepParamMap.put("partitioning_field", meta.getPartitioningField());
        stepParamMap.put("partitioning_daily", boolToStr(meta.isPartitioningDaily()));
        stepParamMap.put("partitioning_monthly", boolToStr(meta.isPartitioningMonthly()));
        
        stepParamMap.put("tablename_in_field", boolToStr(meta.isTableNameInField()));
        stepParamMap.put("tablename_field", meta.getTableNameField());
        stepParamMap.put("tablename_in_table", boolToStr(meta.isTableNameInTable()));

        stepParamMap.put("return_keys", boolToStr(meta.isReturningGeneratedKeys()));
        stepParamMap.put("return_field", meta.getGeneratedKeyField());
        
        putListParamInStepParamMap(stepParamMap, "table_field", meta.getFieldDatabase());
        putListParamInStepParamMap(stepParamMap, "stream_field", meta.getFieldStream());
    }
}
