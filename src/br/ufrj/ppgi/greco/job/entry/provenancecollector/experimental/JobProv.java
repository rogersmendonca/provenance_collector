package br.ufrj.ppgi.greco.job.entry.provenancecollector.experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.Log4jBufferAppender;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryListener;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.Trans;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobEntryTransDecorator;

/* Experimental: JobProv extendendo Job ... 
 * Outra opcao (JobDecorator): decorator do Job 
 * */
public class JobProv extends Job
{
    // for i18n purposes, needed by Translator2!! $NON-NLS-1$
    private static Class<?> PKG = Job.class;

    private AtomicBoolean initialized2;
    private AtomicBoolean active2;
    private int maxJobEntriesLogged;
    
    /**
     * Metodos sobrescritos para tratamento dos atributos initialized2 e active2
     */
    @Override    
    public void init()
    {
        super.init();
        
        initialized2 = new AtomicBoolean(false);;
        active2 = new AtomicBoolean(false);
    }
    
    @Override
    public boolean isInitialized()
    {
        return super.isInitialized() || initialized2.get();
    }
    
    @Override    
    public boolean isActive()
    {
        return super.isActive() || active2.get();
    }

    @Override
    public String getStatus()
    {
        String message;

        if (!isInitialized())
        {
            message = Trans.STRING_WAITING;
        }
        else
        {
            if (isActive())
            {
                if (isStopped())
                {
                    message = Trans.STRING_HALTING;
                }
                else
                {
                    message = Trans.STRING_RUNNING;
                }
            }
            else
            {
                if (isStopped())
                {
                    message = Trans.STRING_STOPPED;
                }
                else
                {
                    message = Trans.STRING_FINISHED;
                }
                if (getResult() != null && getResult().getNrErrors() > 0)
                {
                    message += " (with errors)";
                }
            }
        }

        return message;
    }
    
    // Threads main loop: called by Thread.start();
    @Override
    public void run()
    {
        try
        {
            setStopped(false);
            setFinished(false);
            initialized2 = new AtomicBoolean(true);

            // Create a new variable name space as we want jobs to have their
            // own set of variables.
            // initialize from parentJob or null
            //
            initializeVariablesFrom(getParentJob());
            
            // Rogers: Parei aqui, pois variables é um atributo privado e nao tem metodo publico para obte-lo...
            //setInternalKettleVariables(variables);
            copyParametersFrom(getJobMeta());
            activateParameters();

            // Run the job
            //
            // Rogers: Parei aqui, pois execute é um metodo privado...
            //setResult(execute());
        }
        catch (Throwable je)
        {
            getLogChannel().logError(BaseMessages
                    .getString(PKG, "Job.Log.ErrorExecJob", je));
            // log.logError(Const.getStackTracker(je));
            //
            // we don't have result object because execute() threw a curve-ball.
            // So we create a new error object.
            //
            setResult(new Result());
            getResult().setNrErrors(1L);
            getResult().setResult(false);
            addErrors(1); // This can be before actual execution
            active2.set(false);
            setFinished(true);
            setStopped(false);
        }
        finally
        {
            try
            {
                fireJobListeners();
            }
            catch (KettleException e)
            {
                getResult().setNrErrors(1);
                getResult().setResult(false);
                getLogChannel().logError(
                        BaseMessages.getString(PKG, "Job.Log.ErrorExecJob",
                                e.getMessage()), e);
            }
        }
    }
    

    /**
     * Metodo execute especifico para captura de Proveniencia
     */

    /**
     * Execute a job with previous results passed in.<br>
     * <br>
     * Execute called by JobEntryJob: don't clear the jobEntryResults.
     * 
     * @param nr
     *            The job entry number
     * @param result
     *            the result of the previous execution
     * @return Result of the job execution
     * @throws KettleJobException
     */
    @Override
    public Result execute(int nr, Result result) throws KettleException
    {
        setFinished(false);
        active2.set(true);
        initialized2.set(true);

        // Where do we start?
        JobEntryCopy startpoint;

        // Perhaps there is already a list of input rows available?
        if (getSourceRows() != null)
        {
            result.setRows(getSourceRows());
        }

        startpoint = getJobMeta().findJobEntry(JobMeta.STRING_SPECIAL_START, 0,
                false);
        if (startpoint == null)
        {
            throw new KettleJobException(BaseMessages.getString(PKG,
                    "Log.CounldNotFindStartingPoint"));
        }

        Result res = execute(nr, result, startpoint, null,
                BaseMessages.getString(PKG, "Reason.StartOfJobentry"));

        active2.set(false);

        return res;
    }

    /**
     * Execute a job entry recursively and move to the next job entry
     * automatically.<br>
     * Uses a back-tracking algorithm.<br>
     * 
     * @param nr
     * @param prev_result
     * @param jobEntryCopy
     * @param previous
     * @param reason
     * @return
     * @throws KettleException
     */
    private Result execute(final int nr, Result prev_result,
            final JobEntryCopy jobEntryCopy, JobEntryCopy previous,
            String reason) throws KettleException
    {
        Result res = null;

        if (isStopped())
        {
            res = new Result(nr);
            res.stopped = true;
            return res;
        }

        if (getLogChannel().isDetailed())
            getLogChannel().logDetailed(
                    "exec("
                            + nr
                            + ", "
                            + (prev_result != null ? prev_result.getNrErrors()
                                    : 0)
                            + ", "
                            + (jobEntryCopy != null ? jobEntryCopy.toString()
                                    : "null") + ")");

        // Rogers: Se a Interface do Job Entry eh do tipo transformation,
        // empacota com o Decorator de Proveniencia
        if (jobEntryCopy.getEntry() instanceof JobEntryTrans)
        {
            jobEntryCopy.setEntry(new JobEntryTransDecorator(
                    (JobEntryTrans) jobEntryCopy.getEntry()));
        }

        // What entry is next?
        JobEntryInterface jobEntryInterface = jobEntryCopy.getEntry();
        jobEntryInterface.getLogChannel().setLogLevel(getLogLevel());

        // Track the fact that we are going to launch the next job entry...
        JobEntryResult jerBefore = new JobEntryResult(null, null,
                BaseMessages.getString(PKG, "Comment.JobStarted"), reason,
                jobEntryCopy.getName(), jobEntryCopy.getNr(),
                environmentSubstitute(jobEntryCopy.getEntry().getFilename()));
        getJobTracker().addJobTracker(new JobTracker(getJobMeta(), jerBefore));

        Result prevResult = null;
        if (prev_result != null)
        {
            prevResult = (Result) prev_result.clone();
        }
        else
        {
            prevResult = new Result();
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
                jobEntryInterface.getClass().getClassLoader());
        // Execute this entry...
        JobEntryInterface cloneJei = (JobEntryInterface) jobEntryInterface
                .clone();
        ((VariableSpace) cloneJei).copyVariablesFrom(this);
        cloneJei.setRepository(getRep());
        cloneJei.setParentJob(this);
        final long start = System.currentTimeMillis();

        cloneJei.getLogChannel().logDetailed("Starting job entry");
        for (JobEntryListener jobEntryListener : getJobEntryListeners())
        {
            jobEntryListener.beforeExecution(this, jobEntryCopy, cloneJei);
        }
        if (isInteractive())
        {
            if (jobEntryCopy.isTransformation())
            {
                // Rogers: Obtem o objeto JobEntryTrans empacotado pelo objeto
                // da classe JobEntryTransDecorator
                JobEntryTrans jet = ((JobEntryTransDecorator) cloneJei)
                        .getJobEntryTrans();
                getActiveJobEntryTransformations().put(jobEntryCopy, jet);

            }
            if (jobEntryCopy.isJob())
            {
                getActiveJobEntryJobs().put(jobEntryCopy,
                        (JobEntryJob) cloneJei);
            }
        }
        final Result result = cloneJei.execute(prevResult, nr);
        final long end = System.currentTimeMillis();
        if (isInteractive())
        {
            if (jobEntryCopy.isTransformation())
            {
                getActiveJobEntryTransformations().remove(jobEntryCopy);
            }
            if (jobEntryCopy.isJob())
            {
                getActiveJobEntryJobs().remove(jobEntryCopy);
            }
        }

        // Rogers: inclusao da interface JobEntryTransDecorator
        if ((cloneJei instanceof JobEntryTrans)
                || (cloneJei instanceof JobEntryTransDecorator))
        {
            String throughput = result
                    .getReadWriteThroughput((int) ((end - start) / 1000));
            if (throughput != null)
            {
                getLogChannel().logMinimal(throughput);
            }
        }
        for (JobEntryListener jobEntryListener : getJobEntryListeners())
        {
            jobEntryListener.afterExecution(this, jobEntryCopy, cloneJei,
                    result);
        }

        Thread.currentThread().setContextClassLoader(cl);
        addErrors((int) result.getNrErrors());

        // Also capture the logging text after the execution...
        //
        Log4jBufferAppender appender = CentralLogStore.getAppender();
        StringBuffer logTextBuffer = appender.getBuffer(cloneJei
                .getLogChannel().getLogChannelId(), false);
        result.setLogText(logTextBuffer.toString());

        // Save this result as well...
        //
        JobEntryResult jerAfter = new JobEntryResult(result, cloneJei
                .getLogChannel().getLogChannelId(), BaseMessages.getString(PKG,
                "Comment.JobFinished"), null, jobEntryCopy.getName(),
                jobEntryCopy.getNr(), environmentSubstitute(jobEntryCopy
                        .getEntry().getFilename()));
        getJobTracker().addJobTracker(new JobTracker(getJobMeta(), jerAfter));
        getJobEntryResults().add(jerAfter);

        // Only keep the last X job entry results in memory
        //
        if (maxJobEntriesLogged > 0
                && getJobEntryResults().size() > maxJobEntriesLogged)
        {
            getJobEntryResults().remove(0); // Remove the oldest.
        }

        // Try all next job entries.
        //
        // Keep track of all the threads we fired in case of parallel
        // execution...
        // Keep track of the results of these executions too.
        //
        final List<Thread> threads = new ArrayList<Thread>();
        final List<Result> threadResults = new ArrayList<Result>();
        final List<KettleException> threadExceptions = new ArrayList<KettleException>();
        final List<JobEntryCopy> threadEntries = new ArrayList<JobEntryCopy>();

        // Launch only those where the hop indicates true or false
        //
        int nrNext = getJobMeta().findNrNextJobEntries(jobEntryCopy);
        for (int i = 0; i < nrNext && !isStopped(); i++)
        {
            // The next entry is...
            final JobEntryCopy nextEntry = getJobMeta().findNextJobEntry(
                    jobEntryCopy, i);

            // See if we need to execute this...
            final JobHopMeta hi = getJobMeta().findJobHop(jobEntryCopy,
                    nextEntry);

            // The next comment...
            final String nextComment;
            if (hi.isUnconditional())
            {
                nextComment = BaseMessages.getString(PKG,
                        "Comment.FollowedUnconditional");
            }
            else
            {
                if (result.getResult())
                {
                    nextComment = BaseMessages.getString(PKG,
                            "Comment.FollowedSuccess");
                }
                else
                {
                    nextComment = BaseMessages.getString(PKG,
                            "Comment.FollowedFailure");
                }
            }

            //
            // If the link is unconditional, execute the next job entry
            // (entries).
            // If the start point was an evaluation and the link color is
            // correct: green or red, execute the next job entry...
            //
            if (hi.isUnconditional()
                    || (jobEntryCopy.evaluates() && (!(hi.getEvaluation() ^ result
                            .getResult()))))
            {
                // Start this next step!
                if (getLogChannel().isBasic())
                    getLogChannel().logBasic(
                            BaseMessages.getString(PKG, "Log.StartingEntry",
                                    nextEntry.getName()));

                // Pass along the previous result, perhaps the next job can use
                // it...
                // However, set the number of errors back to 0 (if it should be
                // reset)
                // When an evaluation is executed the errors e.g. should not be
                // reset.
                if (nextEntry.resetErrorsBeforeExecution())
                {
                    result.setNrErrors(0);
                }

                // Now execute!
                //
                // if (we launch in parallel, fire the execution off in a new
                // thread...
                //
                if (jobEntryCopy.isLaunchingInParallel())
                {
                    threadEntries.add(nextEntry);

                    Runnable runnable = new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                Result threadResult = execute(nr + 1, result,
                                        nextEntry, jobEntryCopy, nextComment);
                                threadResults.add(threadResult);
                            }
                            catch (Throwable e)
                            {
                                getLogChannel().logError(
                                        Const.getStackTracker(e));
                                threadExceptions.add(new KettleException(
                                        BaseMessages.getString(PKG,
                                                "Log.UnexpectedError",
                                                nextEntry.toString()), e));
                                Result threadResult = new Result();
                                threadResult.setResult(false);
                                threadResult.setNrErrors(1L);
                                threadResults.add(threadResult);
                            }
                        }
                    };
                    Thread thread = new Thread(runnable);
                    threads.add(thread);
                    thread.start();
                    if (getLogChannel().isBasic())
                        getLogChannel().logBasic(
                                BaseMessages.getString(PKG,
                                        "Log.LaunchedJobEntryInParallel",
                                        nextEntry.getName()));
                }
                else
                {
                    try
                    {
                        // Same as before: blocks until it's done
                        //
                        res = execute(nr + 1, result, nextEntry, jobEntryCopy,
                                nextComment);
                    }
                    catch (Throwable e)
                    {
                        getLogChannel().logError(Const.getStackTracker(e));
                        throw new KettleException(BaseMessages.getString(PKG,
                                "Log.UnexpectedError", nextEntry.toString()), e);
                    }
                    if (getLogChannel().isBasic())
                        getLogChannel().logBasic(
                                BaseMessages.getString(PKG,
                                        "Log.FinishedJobEntry",
                                        nextEntry.getName(), res.getResult()
                                                + ""));
                }
            }
        }

        // OK, if we run in parallel, we need to wait for all the job entries to
        // finish...
        //
        if (jobEntryCopy.isLaunchingInParallel())
        {
            for (int i = 0; i < threads.size(); i++)
            {
                Thread thread = threads.get(i);
                JobEntryCopy nextEntry = threadEntries.get(i);

                try
                {
                    thread.join();
                }
                catch (InterruptedException e)
                {
                    getLogChannel()
                            .logError(
                                    getJobMeta().toString(),
                                    BaseMessages
                                            .getString(
                                                    PKG,
                                                    "Log.UnexpectedErrorWhileWaitingForJobEntry",
                                                    nextEntry.getName()));
                    threadExceptions
                            .add(new KettleException(
                                    BaseMessages
                                            .getString(
                                                    PKG,
                                                    "Log.UnexpectedErrorWhileWaitingForJobEntry",
                                                    nextEntry.getName()), e));
                }
            }
        }

        // Perhaps we don't have next steps??
        // In this case, return the previous result.
        if (res == null)
        {
            res = prevResult;
        }

        // See if there where any errors in the parallel execution
        //
        if (threadExceptions.size() > 0)
        {
            res.setResult(false);
            res.setNrErrors(threadExceptions.size());

            for (KettleException e : threadExceptions)
            {
                getLogChannel().logError(getJobMeta().toString(),
                        e.getMessage(), e);
            }

            // Now throw the first Exception for good measure...
            //
            throw threadExceptions.get(0);
        }

        // In parallel execution, we aggregate all the results, simply add them
        // to the previous result...
        //
        for (Result threadResult : threadResults)
        {
            res.add(threadResult);
        }

        // If there have been errors, logically, we need to set the result to
        // "false"...
        //
        if (res.getNrErrors() > 0)
        {
            res.setResult(false);
        }

        return res;
    }
}
