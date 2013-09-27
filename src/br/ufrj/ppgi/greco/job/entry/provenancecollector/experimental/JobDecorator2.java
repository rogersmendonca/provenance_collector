package br.ufrj.ppgi.greco.job.entry.provenancecollector.experimental;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.Log4jBufferAppender;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingHierarchy;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryListener;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.www.SocketRepository;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobEntryTransDecorator;

/* Experimental: JobDecorator implementando as interfaces de Job ... (nao foi util pois no JobEntryProvenanceCollector, o tipo do objeto é Job)  
 * Outra opcao (JobDecorator): decorator do Job extendendo Job 
 * */
public class JobDecorator2 extends Thread implements VariableSpace,
        NamedParams, HasLogChannelInterface, LoggingObjectInterface
{
    // for i18n purposes, needed by Translator2!! $NON-NLS-1$
    private static Class<?> PKG = Job.class;

    private AtomicBoolean initialized2;
    private AtomicBoolean active2;
    private int maxJobEntriesLogged;

    private Job job;

    public JobDecorator2(Job job)
    {
        this.job = job;
        initialized2 = new AtomicBoolean(false);
        active2 = new AtomicBoolean(false);
        maxJobEntriesLogged = Const.toInt(
                EnvUtil.getSystemProperty(Const.KETTLE_MAX_JOB_ENTRIES_LOGGED),
                1000);
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
    public Result execute(int nr, Result result) throws KettleException
    {
        job.setFinished(false);
        active2.set(true);
        initialized2.set(true);

        // Perhaps there is already a list of input rows available?
        if (job.getSourceRows() != null)
        {
            result.setRows(job.getSourceRows());
        }

        // Where do we start?
        JobEntryCopy startpoint = job.getJobMeta().findJobEntry(
                JobMeta.STRING_SPECIAL_START, 0, false);
        if (startpoint == null)
        {
            throw new KettleJobException(BaseMessages.getString(PKG,
                    "Job.Log.CounldNotFindStartingPoint"));
        }

        Result res = execute(nr, result, startpoint, null,
                BaseMessages.getString(PKG, "Job.Reason.StartOfJobentry"));

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

        if (job.isStopped())
        {
            res = new Result(nr);
            res.stopped = true;
            return res;
        }

        if (job.getLogChannel().isDetailed())
            job.getLogChannel().logDetailed(
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
        jobEntryInterface.getLogChannel().setLogLevel(job.getLogLevel());

        // Track the fact that we are going to launch the next job entry...
        JobEntryResult jerBefore = new JobEntryResult(null, null,
                BaseMessages.getString(PKG, "Job.Comment.JobStarted"), reason,
                jobEntryCopy.getName(), jobEntryCopy.getNr(),
                environmentSubstitute(jobEntryCopy.getEntry().getFilename()));
        job.getJobTracker().addJobTracker(
                new JobTracker(job.getJobMeta(), jerBefore));

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
        cloneJei.setRepository(job.getRep());
        cloneJei.setParentJob(job);
        final long start = System.currentTimeMillis();

        cloneJei.getLogChannel().logDetailed("Starting job entry");
        for (JobEntryListener jobEntryListener : job.getJobEntryListeners())
        {
            jobEntryListener.beforeExecution(job, jobEntryCopy, cloneJei);
        }
        if (job.isInteractive())
        {
            if (jobEntryCopy.isTransformation())
            {
                // Rogers: Obtem o objeto JobEntryTrans empacotado pelo objeto
                // da classe JobEntryTransDecorator
                job.getActiveJobEntryTransformations().put(jobEntryCopy,
                        ((JobEntryTransDecorator) cloneJei).getJobEntryTrans());

            }
            if (jobEntryCopy.isJob())
            {
                job.getActiveJobEntryJobs().put(jobEntryCopy,
                        (JobEntryJob) cloneJei);
            }
        }
        final Result result = cloneJei.execute(prevResult, nr);
        final long end = System.currentTimeMillis();
        if (job.isInteractive())
        {
            if (jobEntryCopy.isTransformation())
            {
                job.getActiveJobEntryTransformations().remove(jobEntryCopy);
            }
            if (jobEntryCopy.isJob())
            {
                job.getActiveJobEntryJobs().remove(jobEntryCopy);
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
                job.getLogChannel().logMinimal(throughput);
            }
        }
        for (JobEntryListener jobEntryListener : job.getJobEntryListeners())
        {
            jobEntryListener
                    .afterExecution(job, jobEntryCopy, cloneJei, result);
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
                "Job.Comment.JobFinished"), null, jobEntryCopy.getName(),
                jobEntryCopy.getNr(), environmentSubstitute(jobEntryCopy
                        .getEntry().getFilename()));
        job.getJobTracker().addJobTracker(
                new JobTracker(job.getJobMeta(), jerAfter));
        job.getJobEntryResults().add(jerAfter);

        // Only keep the last X job entry results in memory
        //
        if (maxJobEntriesLogged > 0
                && job.getJobEntryResults().size() > maxJobEntriesLogged)
        {
            job.getJobEntryResults().remove(0); // Remove the oldest.
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
        int nrNext = job.getJobMeta().findNrNextJobEntries(jobEntryCopy);
        for (int i = 0; i < nrNext && !job.isStopped(); i++)
        {
            // The next entry is...
            final JobEntryCopy nextEntry = job.getJobMeta().findNextJobEntry(
                    jobEntryCopy, i);

            // See if we need to execute this...
            final JobHopMeta hi = job.getJobMeta().findJobHop(jobEntryCopy,
                    nextEntry);

            // The next comment...
            final String nextComment;
            if (hi.isUnconditional())
            {
                nextComment = BaseMessages.getString(PKG,
                        "Job.Comment.FollowedUnconditional");
            }
            else
            {
                if (result.getResult())
                {
                    nextComment = BaseMessages.getString(PKG,
                            "Job.Comment.FollowedSuccess");
                }
                else
                {
                    nextComment = BaseMessages.getString(PKG,
                            "Job.Comment.FollowedFailure");
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
                if (job.getLogChannel().isBasic())
                    job.getLogChannel().logBasic(
                            BaseMessages.getString(PKG,
                                    "Job.Log.StartingEntry",
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
                                job.getLogChannel().logError(
                                        Const.getStackTracker(e));
                                threadExceptions.add(new KettleException(
                                        BaseMessages.getString(PKG,
                                                "Job.Log.UnexpectedError",
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
                    if (job.getLogChannel().isBasic())
                        job.getLogChannel().logBasic(
                                BaseMessages.getString(PKG,
                                        "Job.Log.LaunchedJobEntryInParallel",
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
                        job.getLogChannel().logError(Const.getStackTracker(e));
                        throw new KettleException(
                                BaseMessages.getString(PKG,
                                        "Job.Log.UnexpectedError",
                                        nextEntry.toString()), e);
                    }
                    if (job.getLogChannel().isBasic())
                        job.getLogChannel().logBasic(
                                BaseMessages.getString(PKG,
                                        "Job.Log.FinishedJobEntry",
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
                    job.getLogChannel()
                            .logError(
                                    job.getJobMeta().toString(),
                                    BaseMessages
                                            .getString(
                                                    PKG,
                                                    "Job.Log.UnexpectedErrorWhileWaitingForJobEntry",
                                                    nextEntry.getName()));
                    threadExceptions
                            .add(new KettleException(
                                    BaseMessages
                                            .getString(
                                                    PKG,
                                                    "Job.Log.UnexpectedErrorWhileWaitingForJobEntry",
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
                job.getLogChannel().logError(job.getJobMeta().toString(),
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

    /**
     * Metodos das interfaces implementadas pela classe Job
     */

    @Override
    public String getContainerObjectId()
    {
        if (job != null)
        {
            return job.getContainerObjectId();
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getFilename()
    {
        if (job == null)
        {
            return null;
        }
        else
        {
            return job.getFilename();
        }
    }

    @Override
    public String getLogChannelId()
    {
        return job.getLogChannelId();
    }

    @Override
    public LogLevel getLogLevel()
    {
        if (job != null)
        {
            return job.getLogLevel();
        }
        else
        {
            return DefaultLogLevel.getLogLevel();
        }
    }

    @Override
    public String getObjectCopy()
    {
        return null;
    }

    @Override
    public ObjectId getObjectId()
    {
        if (job != null)
        {
            return job.getObjectId();
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getObjectName()
    {
        if (job != null)
        {
            return job.getJobname();
        }
        else
        {
            return null;
        }
    }

    @Override
    public ObjectRevision getObjectRevision()
    {
        if (job != null)
        {
            return job.getObjectRevision();
        }
        else
        {
            return null;
        }
    }

    @Override
    public LoggingObjectType getObjectType()
    {
        return LoggingObjectType.JOB;
    }

    @Override
    public LoggingObjectInterface getParent()
    {
        if (job != null)
        {
            return job.getParent();
        }
        else
        {
            return null;
        }
    }

    @Override
    public Date getRegistrationDate()
    {
        return job.getRegistrationDate();
    }

    @Override
    public RepositoryDirectoryInterface getRepositoryDirectory()
    {
        if (job == null)
            return null;
        return job.getRepositoryDirectory();
    }

    @Override
    public LogChannelInterface getLogChannel()
    {
        return job.getLogChannel();
    }

    @Override
    public void activateParameters()
    {
        job.activateParameters();
    }

    @Override
    public void addParameterDefinition(String key, String defValue,
            String description) throws DuplicateParamException
    {
        job.addParameterDefinition(key, defValue, description);
    }

    @Override
    public void clearParameters()
    {
        job.clearParameters();
    }

    @Override
    public void copyParametersFrom(NamedParams params)
    {
        job.copyParametersFrom(params);
    }

    @Override
    public void eraseParameters()
    {
        job.eraseParameters();
    }

    @Override
    public String getParameterDefault(String key) throws UnknownParamException
    {
        return job.getParameterDefault(key);
    }

    @Override
    public String getParameterDescription(String key)
            throws UnknownParamException
    {
        return job.getParameterDescription(key);
    }

    @Override
    public String getParameterValue(String key) throws UnknownParamException
    {
        return job.getParameterValue(key);
    }

    @Override
    public String[] listParameters()
    {
        return job.listParameters();
    }

    @Override
    public void setParameterValue(String key, String value)
            throws UnknownParamException
    {
        job.setParameterValue(key, value);
    }

    @Override
    public void copyVariablesFrom(VariableSpace space)
    {
        job.copyVariablesFrom(space);
    }

    @Override
    public String environmentSubstitute(String aString)
    {
        return job.environmentSubstitute(aString);
    }

    @Override
    public String[] environmentSubstitute(String[] aString)
    {
        return job.environmentSubstitute(aString);
    }

    @Override
    public boolean getBooleanValueOfVariable(String variableName,
            boolean defaultValue)
    {
        return job.getBooleanValueOfVariable(variableName, defaultValue);
    }

    @Override
    public VariableSpace getParentVariableSpace()
    {
        return job.getParentVariableSpace();
    }

    @Override
    public String getVariable(String variableName)
    {
        return job.getVariable(variableName);
    }

    @Override
    public String getVariable(String variableName, String defaultValue)
    {
        return job.getVariable(variableName, defaultValue);
    }

    @Override
    public void initializeVariablesFrom(VariableSpace parent)
    {
        job.initializeVariablesFrom(parent);
    }

    @Override
    public void injectVariables(Map<String, String> prop)
    {
        job.injectVariables(prop);
    }

    @Override
    public String[] listVariables()
    {
        return job.listVariables();
    }

    @Override
    public void setParentVariableSpace(VariableSpace parent)
    {
        job.setParentVariableSpace(parent);
    }

    @Override
    public void setVariable(String variableName, String variableValue)
    {
        job.setVariable(variableName, variableValue);
    }

    @Override
    public void shareVariablesWith(VariableSpace space)
    {
        job.shareVariablesWith(space);
    }

    /**
     * Metodos especificos da classe Job
     */

    public void init()
    {
        if (job != null)
        {
            job.init();
        }
    }

    @Override
    public String toString()
    {
        return job.toString();
    }

    public String getJobname()
    {
        if (job != null)
        {
            return job.getJobname();
        }
        else
        {
            return null;
        }
    }

    public void setRepository(Repository rep)
    {
        job.setRepository(rep);
    }

    // Threads main loop: called by Thread.start();
    public void run()
    {
        job.run();
    }

    /**
     * Sets the finished flag.<b> Then launch all the job listeners and call the
     * jobFinished method for each.<br>
     * 
     * @see JobListener#jobFinished(Job)
     */
    public void fireJobListeners() throws KettleException
    {
        job.fireJobListeners();
    }

    /**
     * Wait until this job has finished.
     */
    public void waitUntilFinished()
    {
        job.waitUntilFinished();
    }

    /**
     * Wait until this job has finished.
     * 
     * @param maxMiliseconds
     *            the maximum number of ms to wait
     */
    public void waitUntilFinished(long maxMiliseconds)
    {
        job.waitUntilFinished(maxMiliseconds);
    }

    /**
     * Get the number of errors that happened in the job.
     * 
     * @return nr of error that have occurred during execution. During execution
     *         of a job the number can change.
     */
    public int getErrors()
    {
        return job.getErrors();
    }

    /**
     * Set the number of occured errors to 0.
     */
    public void resetErrors()
    {
        job.resetErrors();
    }

    /**
     * Add a number of errors to the total number of erros that occured during
     * execution.
     * 
     * @param nrToAdd
     *            nr of errors to add.
     */
    public void addErrors(int nrToAdd)
    {
        job.addErrors(nrToAdd);
    }

    /**
     * Handle logging at start
     * 
     * @return true if it went OK.
     * 
     * @throws KettleException
     */
    public boolean beginProcessing() throws KettleException
    {
        return job.beginProcessing();
    }

    public boolean isActive()
    {
        return this.active2.get() ? this.active2.get() : job.isActive();
    }

    // Stop all activity!
    public void stopAll()
    {
        job.stopAll();
    }

    public void setStopped(boolean stopped)
    {
        job.setStopped(stopped);
    }

    /**
     * @return Returns the stopped status of this Job...
     */
    public boolean isStopped()
    {
        return job.isStopped();
    }

    /**
     * @return Returns the startDate.
     */
    public Date getStartDate()
    {
        return job.getStartDate();
    }

    /**
     * @return Returns the endDate.
     */
    public Date getEndDate()
    {
        return job.getEndDate();
    }

    /**
     * @return Returns the currentDate.
     */
    public Date getCurrentDate()
    {
        return job.getCurrentDate();
    }

    /**
     * @return Returns the depDate.
     */
    public Date getDepDate()
    {
        return job.getDepDate();
    }

    /**
     * @return Returns the logDate.
     */
    public Date getLogDate()
    {
        return job.getLogDate();
    }

    /**
     * @return Returns the jobinfo.
     */
    public JobMeta getJobMeta()
    {
        return job.getJobMeta();
    }

    /**
     * @return Returns the rep.
     */
    public Repository getRep()
    {
        return job.getRep();
    }

    public Thread getThread()
    {
        return job.getThread();
    }

    /**
     * @return Returns the jobTracker.
     */
    public JobTracker getJobTracker()
    {
        return job.getJobTracker();
    }

    /**
     * @param jobTracker
     *            The jobTracker to set.
     */
    public void setJobTracker(JobTracker jobTracker)
    {
        job.setJobTracker(jobTracker);
    }

    public void setSourceRows(List<RowMetaAndData> sourceRows)
    {
        job.setSourceRows(sourceRows);
    }

    public List<RowMetaAndData> getSourceRows()
    {
        return job.getSourceRows();
    }

    /**
     * @return Returns the parentJob.
     */
    public Job getParentJob()
    {
        return job.getParentJob();
    }

    /**
     * @param parentJob
     *            The parentJob to set.
     */
    public void setParentJob(Job parentJob)
    {
        job.setParentJob(parentJob);
    }

    public Result getResult()
    {
        return job.getResult();
    }

    public void setResult(Result result)
    {
        job.setResult(result);
    }

    /**
     * @return Returns the initialized.
     */
    public boolean isInitialized()
    {
        return this.initialized2.get() ? this.initialized2.get() : job
                .isInitialized();
    }

    /**
     * @return Returns the batchId.
     */
    public long getBatchId()
    {
        return job.getBatchId();
    }

    /**
     * @param batchId
     *            The batchId to set.
     */
    public void setBatchId(long batchId)
    {
        job.setBatchId(batchId);
    }

    /**
     * @return the jobBatchId
     */
    public long getPassedBatchId()
    {
        return job.getPassedBatchId();
    }

    /**
     * @param jobBatchId
     *            the jobBatchId to set
     */
    public void setPassedBatchId(long jobBatchId)
    {
        job.setPassedBatchId(jobBatchId);
    }

    public void setInternalKettleVariables(VariableSpace var)
    {
        job.setInternalKettleVariables(var);
    }

    public String getStatus()
    {
        return job.getStatus();
    }

    /**
     * Add a job listener to the job
     * 
     * @param jobListener
     *            the job listener to add
     */
    public void addJobListener(JobListener jobListener)
    {
        job.addJobListener(jobListener);
    }

    public void addJobEntryListener(JobEntryListener jobEntryListener)
    {
        job.addJobEntryListener(jobEntryListener);
    }

    /**
     * Remove a job listener from the job
     * 
     * @param jobListener
     *            the job listener to remove
     */
    public void removeJobListener(JobListener jobListener)
    {
        job.removeJobListener(jobListener);
    }

    /**
     * Remove a job entry listener from the job
     * 
     * @param jobListener
     *            the job entry listener to remove
     */
    public void removeJobEntryListener(JobEntryListener jobEntryListener)
    {
        job.removeJobEntryListener(jobEntryListener);
    }

    public List<JobEntryListener> getJobEntryListeners()
    {
        return job.getJobEntryListeners();
    }

    public List<JobListener> getJobListeners()
    {
        return job.getJobListeners();
    }

    /**
     * @return the finished
     */
    public boolean isFinished()
    {
        return job.isFinished();
    }

    /**
     * @param finished
     *            the finished to set
     */
    public void setFinished(boolean finished)
    {
        job.setFinished(finished);
    }

    public void setSocketRepository(SocketRepository socketRepository)
    {
        job.setSocketRepository(socketRepository);
    }

    public SocketRepository getSocketRepository()
    {
        return job.getSocketRepository();
    }

    public void setLogLevel(LogLevel logLevel)
    {
        job.setLogLevel(logLevel);
    }

    public List<LoggingHierarchy> getLoggingHierarchy()
    {
        return job.getLoggingHierarchy();
    }

    /**
     * @return the interactive
     */
    public boolean isInteractive()
    {
        return job.isInteractive();
    }

    /**
     * @param interactive
     *            the interactive to set
     */
    public void setInteractive(boolean interactive)
    {
        job.setInteractive(interactive);
    }

    /**
     * @return the activeJobEntryTransformations
     */
    public Map<JobEntryCopy, JobEntryTrans> getActiveJobEntryTransformations()
    {
        return job.getActiveJobEntryTransformations();
    }

    /**
     * @return the activeJobEntryJobs
     */
    public Map<JobEntryCopy, JobEntryJob> getActiveJobEntryJobs()
    {
        return job.getActiveJobEntryJobs();
    }

    /**
     * @return A flat list of results in THIS job, in the order of execution of
     *         job entries
     */
    public List<JobEntryResult> getJobEntryResults()
    {
        return job.getJobEntryResults();
    }

    /**
     * @param containerObjectId
     *            the execution container object id to set
     */
    public void setContainerObjectId(String containerObjectId)
    {
        job.setContainerObjectId(containerObjectId);
    }

    public LoggingObjectInterface getParentLoggingObject()
    {
        return job.getParentLoggingObject();
    }

    /**
     * @return the startJobEntryCopy
     */
    public JobEntryCopy getStartJobEntryCopy()
    {
        return job.getStartJobEntryCopy();
    }

    /**
     * @param startJobEntryCopy
     *            the startJobEntryCopy to set
     */
    public void setStartJobEntryCopy(JobEntryCopy startJobEntryCopy)
    {
        job.setStartJobEntryCopy(startJobEntryCopy);
    }
}
