package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.List;
import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class DataGridParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        DataGridMeta meta = (DataGridMeta) sm.getStepMetaInterface();

        putListParamInStepParamMap(stepParamMap, "FIELD", meta.getFieldName());

        if (meta.getDataLines() != null)
        {
            stepParamMap.put("ROW_COUNT",
                    String.valueOf(meta.getDataLines().size()));

            for (int i = 0; i < meta.getDataLines().size(); i++)
            {
                List<String> row = meta.getDataLines().get(i);
                for (int j = 0; j < row.size(); j++)
                {
                    String paramName = String.format("ROW_%s#%d",
                            meta.getFieldName()[j], i);
                    stepParamMap.put(paramName, row.get(j));
                }
            }
        }
    }
}
