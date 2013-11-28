package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.constant.ConstantMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class ConstantParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        ConstantMeta meta = (ConstantMeta) sm.getStepMetaInterface();
        
        putListParamInStepParamMap(stepParamMap, "field", meta.getFieldName());
        putListParamInStepParamMap(stepParamMap, "value", meta.getValue());
    }
}
