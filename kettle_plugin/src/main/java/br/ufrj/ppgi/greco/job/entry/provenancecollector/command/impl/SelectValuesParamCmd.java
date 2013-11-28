package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class SelectValuesParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        SelectValuesMeta meta = (SelectValuesMeta) sm.getStepMetaInterface();

        stepParamMap.put("select_unspecified", boolToStr(meta.isSelectingAndSortingUnspecifiedFields()));
        putListParamInStepParamMap(stepParamMap, "select_field", meta.getSelectName());
        putListParamInStepParamMap(stepParamMap, "select_rename", meta.getSelectRename());
        putListParamInStepParamMap(stepParamMap, "remove_field", meta.getDeleteName());
    }
}
