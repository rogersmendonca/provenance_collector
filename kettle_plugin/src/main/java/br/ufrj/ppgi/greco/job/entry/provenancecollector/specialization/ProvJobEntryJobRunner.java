package br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.IRetrospJobListener;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 *
 */
public class ProvJobEntryJobRunner implements IJobRunnable
{
    private static Class<?> PKG = Job.class; // for i18n purposes, needed by
                                             // Translator2!! $NON-NLS-1$

    protected JobDecorator job;
    protected Result result;
    protected LogChannelInterface log;
    protected int entryNr;
    protected boolean finished;

    public ProvJobEntryJobRunner(JobDecorator job, Result result, int entryNr,
            LogChannelInterface log)
    {
        this.job = job;
        this.result = result;
        this.log = log;
        this.entryNr = entryNr;
        this.finished = false;
    }

    // This JobEntryRunner is a replacement for the Job thread.
    // The job thread is never started because we simply want to wait for the
    // result.
    public void run()
    {
        try
        {
            if (job.isStopped() || job.getParentJob() != null
                    && job.getParentJob().isStopped())
                return;

            // Executa o metodo jobStarted dos listeners de proveniencia
            // retrospectiva
            for (IRetrospJobListener retrospJobListener : job
                    .getRetrospJobListeners())
            {
                retrospJobListener.jobStarted(job);
            }

            result = job.execute(entryNr + 1, result);
        }
        catch (KettleException e)
        {
            e.printStackTrace();
            log.logError("An error occurred executing this job entry : ", e);
            result.setResult(false);
            result.setNrErrors(1);
        }
        finally
        {
            // Rogers (Nov/2012): Insere uma linha com o id do job executado
            RowMetaInterface fields = new RowMeta();
            fields.addValueMeta(new ValueMeta("id_job",
                    ValueMetaInterface.TYPE_INTEGER));
            result.getRows().clear();
            RowMetaAndData row = new RowMetaAndData(fields, job.getBatchId());
            result.getRows().add(row);

            try
            {
                job.fireJobListeners();

                // Executa o metodo jobFinished dos listeners de proveniencia
                // retrospectiva
                for (IRetrospJobListener retrospJobListener : job
                        .getRetrospJobListeners())
                {
                    // Se nrErrors == 0, terminou com sucesso.
                    // Caso contrario, terminou com falha.
                    retrospJobListener.jobFinished(job,
                            (result.getNrErrors() == 0));
                }

            }
            catch (KettleException e)
            {
                result.setNrErrors(1);
                result.setResult(false);
                log.logError(
                        BaseMessages.getString(PKG, "Job.Log.ErrorExecJob",
                                e.getMessage()), e);
            }
        }
        finished = true;
        job.setFinished(true);
    }

    /**
     * @param result
     *            The result to set.
     */
    public void setResult(Result result)
    {
        this.result = result;
    }

    /**
     * @return Returns the result.
     */
    public Result getResult()
    {
        return result;
    }

    /**
     * @return Returns the log.
     */
    public LogChannelInterface getLog()
    {
        return log;
    }

    /**
     * @param log
     *            The log to set.
     */
    public void setLog(LogChannelInterface log)
    {
        this.log = log;
    }

    /**
     * @return Returns the job.
     */
    public JobDecorator getJob()
    {
        return job;
    }

    /**
     * @param job
     *            The job to set.
     */
    public void setJob(JobDecorator job)
    {
        this.job = job;
    }

    /**
     * @return Returns the entryNr.
     */
    public int getEntryNr()
    {
        return entryNr;
    }

    /**
     * @param entryNr
     *            The entryNr to set.
     */
    public void setEntryNr(int entryNr)
    {
        this.entryNr = entryNr;
    }

    /**
     * @return Returns the finished.
     */
    public boolean isFinished()
    {
        return finished;
    }

    public void waitUntilFinished()
    {
        while (!isFinished() && !job.isStopped())
        {
            try
            {
                Thread.sleep(0, 1);
            }
            catch (InterruptedException e)
            {
            }
        }
    }
}
