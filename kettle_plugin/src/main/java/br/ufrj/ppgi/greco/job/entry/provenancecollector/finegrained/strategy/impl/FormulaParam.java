package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.formula.FormulaMeta;
import org.pentaho.di.trans.steps.formula.FormulaMetaFunction;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2014
 * 
 */
public class FormulaParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        FormulaMeta meta = (FormulaMeta) sm.getStepMetaInterface();

        FormulaMetaFunction[] arrFormula = meta.getFormula();

        String[] newField = new String[arrFormula.length];
        String[] formula = new String[arrFormula.length];

        for (int i = 0; i < arrFormula.length; i++)
        {
            newField[i] = arrFormula[i].getFieldName();
            formula[i] = arrFormula[i].getFormula();
        }

        putListParamInStepParamMap(stepParamMap, "newField", newField);
        putListParamInStepParamMap(stepParamMap, "formula", formula);
    }
}
