package br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization;

import org.pentaho.di.core.Result;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 *
 */
public interface IJobRunnable extends Runnable
{
    public boolean isFinished();

    public Result getResult();

    public void waitUntilFinished();
}
