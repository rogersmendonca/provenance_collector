package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransStoppedListener;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;

public class ProvenanceTransStoppedListener extends
        ParentProvenanceTransListener implements TransStoppedListener
{
    public ProvenanceTransStoppedListener(Database db, JobDecorator rootJob)
    {
        super(db, rootJob);
    }

    @Override
    public void transStopped(Trans trans)
    {
        try
        {
            transFinished(trans);
        }
        catch (KettleException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
