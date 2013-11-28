package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.scriptvalues_mod.ScriptValuesMetaMod;
import org.pentaho.di.trans.steps.scriptvalues_mod.ScriptValuesScript;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class ScriptValuesParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        ScriptValuesMetaMod meta = (ScriptValuesMetaMod) sm.getStepMetaInterface();

        for (ScriptValuesScript jsScript : meta.getJSScripts())
        {
            if (jsScript.isTransformScript())
            {
                stepParamMap.put("jsScript_script", jsScript.getScript());
            }
        }

        putListParamInStepParamMap(stepParamMap, "field", meta.getFieldname());
        putListParamInStepParamMap(stepParamMap, "field_rename",
                meta.getRename());
        
        stepParamMap.put("compatible", boolToStr(meta.isCompatible()));
        stepParamMap.put("optimizationLevel", meta.getOptimizationLevel());
    }
}
