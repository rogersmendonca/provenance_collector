package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.switchcase.SwitchCaseMeta;
import org.pentaho.di.trans.steps.switchcase.SwitchCaseTarget;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class SwitchCaseParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        SwitchCaseMeta meta = (SwitchCaseMeta) sm.getStepMetaInterface();

        stepParamMap.put("fieldname", meta.getFieldname());
        stepParamMap.put("use_contains", boolToStr(meta.isContains()));
        stepParamMap.put("case_value_type",
                ValueMeta.getTypeDesc(meta.getCaseValueType()));
        stepParamMap.put("case_value_format", meta.getCaseValueFormat());
        stepParamMap.put("case_value_decimal", meta.getCaseValueDecimal());
        stepParamMap.put("case_value_group", meta.getCaseValueGroup());
        stepParamMap
                .put("default_target_step", meta.getDefaultTargetStepname());

        if (meta.getCaseTargets() != null)
        {
            for (int i = 0; i < meta.getCaseTargets().size(); i++)
            {
                SwitchCaseTarget obj = meta.getCaseTargets().get(i);
                stepParamMap.put("case_value#" + i, obj.caseValue);
                stepParamMap.put("case_step#" + i, obj.caseTargetStepname);
            }
        }
    }
}
