package br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization;

import org.pentaho.di.core.Result;
import org.pentaho.di.job.entries.job.JobEntryJobRunner;

public class JobRunnableAdapter implements IJobRunnable
{
    private JobEntryJobRunner runner;

    public JobRunnableAdapter(JobEntryJobRunner runner)
    {
        this.runner = runner;
    }

    @Override
    public void run()
    {
        this.runner.run();
    }

    @Override
    public boolean isFinished()
    {
        return this.runner.isFinished();
    }

    @Override
    public Result getResult()
    {
        return this.runner.getResult();
    }

    @Override
    public void waitUntilFinished()
    {
        this.runner.waitUntilFinished();
    }

}
