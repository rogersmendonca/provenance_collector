package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 *
 */
public class ProvenanceTransListener extends ParentProvenanceTransListener
        implements TransListener
{
    public ProvenanceTransListener(Database db, JobDecorator rootJob)
    {
        super(db, rootJob);
    }

    @Override
    public void transActive(Trans trans)
    {
        try
        {
            transStarted(trans);
        }
        catch (KettleException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void transIdle(Trans trans)
    {
        
    }

    @Override
    public void transFinished(Trans trans) throws KettleException
    {
        super.transFinished(trans);
    }
}
