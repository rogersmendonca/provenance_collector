package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
public class NullParamCmd extends StepParameterCmd
{

    @Override
    public void populaStepParamMap(Map<String, String> stepParamMap, StepMeta sm)
    {

    }

}
