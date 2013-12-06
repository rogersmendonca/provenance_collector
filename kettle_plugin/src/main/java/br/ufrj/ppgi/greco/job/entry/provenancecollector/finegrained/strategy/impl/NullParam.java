package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
public class NullParam extends StepParameter
{

    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap, StepMeta sm)
    {

    }

}
