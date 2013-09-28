package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 *
 */
public interface IRetrospJobListener
{
    public void jobStarted(Job job) throws KettleException;

    public void jobFinished(Job job, boolean success)
            throws KettleException;
}
