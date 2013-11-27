package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class CalculatorParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        CalculatorMeta meta = (CalculatorMeta) sm.getStepMetaInterface();

        if (meta.getCalculation() != null)
        {
            stepParamMap.put("calculation_count",
                    String.valueOf(meta.getCalculation().length));
            for (int i = 0; i < meta.getCalculation().length; i++)
            {
                stepParamMap.put("newField#" + i,
                        meta.getCalculation()[i].getFieldName());
                stepParamMap.put("calcType#" + i,
                        meta.getCalculation()[i].getCalcTypeLongDesc());
                stepParamMap.put("fieldA#" + i,
                        meta.getCalculation()[i].getFieldA());
                stepParamMap.put("fieldB#" + i,
                        meta.getCalculation()[i].getFieldB());
                stepParamMap.put("fieldC#" + i,
                        meta.getCalculation()[i].getFieldC());
            }
        }
    }
}
