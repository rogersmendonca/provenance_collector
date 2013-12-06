package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.janino.JaninoMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class JaninoParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        JaninoMeta meta = (JaninoMeta) sm.getStepMetaInterface();

        if (meta.getFormula() != null)
        {
            for (int i = 0; i < meta.getFormula().length; i++)
            {
                stepParamMap.put("field#" + i,
                        meta.getFormula()[i].getFieldName());
                stepParamMap.put("formula#" + i,
                        meta.getFormula()[i].getFormula());
                stepParamMap.put("replaceField#" + i,
                        meta.getFormula()[i].getReplaceField());
            }
        }
    }
}
