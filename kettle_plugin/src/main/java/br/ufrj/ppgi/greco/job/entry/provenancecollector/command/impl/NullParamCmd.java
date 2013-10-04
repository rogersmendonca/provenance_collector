package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.ParentProspStepParamCmd;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;

public class NullParamCmd extends ParentProspStepParamCmd
{
    public void insertProvenance(JobDecorator jobRoot, Database db,
            StepMeta sm, Long processId) throws KettleException
    {
    }
}
