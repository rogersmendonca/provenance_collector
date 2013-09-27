package br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceHolderInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.w3c.dom.Node;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.ParentProvenanceListener;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.ProvenanceRowListener;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.ProvenanceStepListener;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.ProvenanceTransListener;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.ProvenanceTransStoppedListener;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization.TransProv;

//public class JobEntryTransDecorator extends JobEntryTrans
public class JobEntryTransDecorator implements VariableSpace,
        CheckResultSourceInterface, ResourceHolderInterface,
        LoggingObjectInterface, Cloneable, JobEntryInterface
{
    // for i18n purposes, needed by Translator2!! $NON-NLS-1$
    private static Class<?> PKG = JobEntryTrans.class;

    private JobEntryTrans jobEntryTrans;
    private JobDecorator rootJobDec;

    public JobEntryTransDecorator(JobEntryTrans jobEntryTrans,
            JobDecorator rootJobDec)
    {
        this.jobEntryTrans = jobEntryTrans;
        this.rootJobDec = rootJobDec;
    }

    public JobEntryTrans getJobEntryTrans()
    {
        return jobEntryTrans;
    }

    /**
     * Execute this job entry and return the result. In this case it means, just
     * set the result boolean in the Result class.
     * 
     * @param result
     *            The result of the previous execution
     * @param nr
     *            the job entry number
     * @param rep
     *            the repository connection to use
     * @param parentJob
     *            the parent job
     * @return The Result of the execution.
     */
    @Override
    public Result execute(Result result, int nr) throws KettleException
    {
        result.setEntryNr(nr);

        Log4jFileAppender appender = null;

        LogLevel transLogLevel = jobEntryTrans.getParentJob().getLogLevel();

        String realLogFilename = "";
        if (jobEntryTrans.setLogfile)
        {
            transLogLevel = jobEntryTrans.logFileLevel;

            realLogFilename = jobEntryTrans.environmentSubstitute(jobEntryTrans
                    .getLogFilename());

            // We need to check here the log filename
            // if we do not have one, we must fail
            if (Const.isEmpty(realLogFilename))
            {
                jobEntryTrans.logError(BaseMessages.getString(PKG,
                        "JobTrans.Exception.LogFilenameMissing"));
                result.setNrErrors(1);
                result.setResult(false);
                return result;
            }
            // create parent folder?
            if (!createParentFolder(realLogFilename))
            {
                result.setNrErrors(1);
                result.setResult(false);
                return result;
            }
            try
            {
                appender = LogWriter.createFileAppender(realLogFilename, true,
                        jobEntryTrans.setAppendLogfile);
                LogWriter.getInstance().addAppender(appender);
            }
            catch (KettleException e)
            {
                jobEntryTrans.logError(BaseMessages.getString(PKG,
                        "JobTrans.Error.UnableOpenAppender", realLogFilename,
                        e.toString()));

                jobEntryTrans.logError(Const.getStackTracker(e));
                result.setNrErrors(1);
                result.setResult(false);
                return result;
            }
        }

        // Figure out the remote slave server...
        //
        SlaveServer remoteSlaveServer = null;
        if (!Const.isEmpty(jobEntryTrans.getRemoteSlaveServerName()))
        {
            String realRemoteSlaveServerName = jobEntryTrans
                    .environmentSubstitute(jobEntryTrans
                            .getRemoteSlaveServerName());
            remoteSlaveServer = jobEntryTrans.getParentJob().getJobMeta()
                    .findSlaveServer(realRemoteSlaveServerName);
            if (remoteSlaveServer == null)
            {
                throw new KettleException(BaseMessages.getString(PKG,
                        "JobTrans.Exception.UnableToFindRemoteSlaveServer",
                        realRemoteSlaveServerName));
            }
        }

        // Open the transformation...
        //
        switch (jobEntryTrans.getSpecificationMethod())
        {
            case FILENAME:
                if (jobEntryTrans.isDetailed())
                {
                    jobEntryTrans
                            .logDetailed(BaseMessages.getString(
                                    PKG,
                                    "JobTrans.Log.OpeningTrans",
                                    jobEntryTrans
                                            .environmentSubstitute(getFilename())));
                }
                break;
            case REPOSITORY_BY_NAME:
                if (jobEntryTrans.isDetailed())
                {
                    jobEntryTrans.logDetailed(BaseMessages.getString(PKG,
                            "JobTrans.Log.OpeningTransInDirec", jobEntryTrans
                                    .environmentSubstitute(getFilename()),
                            jobEntryTrans.environmentSubstitute(jobEntryTrans
                                    .getDirectory())));
                }
                break;
            case REPOSITORY_BY_REFERENCE:
                if (jobEntryTrans.isDetailed())
                {
                    jobEntryTrans.logDetailed(BaseMessages.getString(PKG,
                            "JobTrans.Log.OpeningTransByReference",
                            jobEntryTrans.getTransObjectId()));
                }
                break;
        }

        // Load the transformation only once for the complete loop!
        // Throws an exception if it was not possible to load the
        // transformation. For example, the XML file doesn't exist or the
        // repository is down.
        // Log the stack trace and return an error condition from this
        //
        TransMeta transMeta = jobEntryTrans.getTransMeta(
                jobEntryTrans.getRepository(), jobEntryTrans);

        int iteration = 0;
        String args1[] = jobEntryTrans.arguments;
        if (args1 == null || args1.length == 0) // No arguments set, look at the
                                                // parent job.
        {
            args1 = jobEntryTrans.getParentJob().getJobMeta().getArguments();
        }
        // initializeVariablesFrom(parentJob);

        //
        // For the moment only do variable translation at the start of a job,
        // not
        // for every input row (if that would be switched on). This is for
        // safety,
        // the real argument setting is later on.
        //
        String args[] = null;
        if (args1 != null)
        {
            args = new String[args1.length];
            for (int idx = 0; idx < args1.length; idx++)
            {
                args[idx] = jobEntryTrans.environmentSubstitute(args1[idx]);
            }
        }

        RowMetaAndData resultRow = null;
        boolean first = true;
        List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>(
                result.getRows());

        while ((first && !jobEntryTrans.execPerRow)
                || (jobEntryTrans.execPerRow && rows != null
                        && iteration < rows.size() && result.getNrErrors() == 0)
                && !jobEntryTrans.getParentJob().isStopped())
        {
            // Clear the result rows of the result
            // Otherwise we double the amount of rows every iteration in the
            // simple cases.
            //
            if (jobEntryTrans.execPerRow)
            {
                result.getRows().clear();
            }
            if (rows != null && jobEntryTrans.execPerRow)
            {
                resultRow = rows.get(iteration);
            }
            else
            {
                resultRow = null;
            }

            NamedParams namedParam = new NamedParamsDefault();
            if (jobEntryTrans.parameters != null)
            {
                for (int idx = 0; idx < jobEntryTrans.parameters.length; idx++)
                {
                    if (!Const.isEmpty(jobEntryTrans.parameters[idx]))
                    {
                        // We have a parameter
                        //
                        namedParam.addParameterDefinition(
                                jobEntryTrans.parameters[idx], "",
                                "Job entry runtime");
                        if (Const.isEmpty(Const
                                .trim(jobEntryTrans.parameterFieldNames[idx])))
                        {
                            // There is no field name specified.
                            //
                            String value = Const
                                    .NVL(jobEntryTrans
                                            .environmentSubstitute(jobEntryTrans.parameterValues[idx]),
                                            "");
                            namedParam.setParameterValue(
                                    jobEntryTrans.parameters[idx], value);
                        }
                        else
                        {
                            // something filled in, in the field column...
                            //
                            String value = "";
                            if (resultRow != null)
                            {
                                value = resultRow.getString(
                                        jobEntryTrans.parameterFieldNames[idx],
                                        "");
                            }
                            namedParam.setParameterValue(
                                    jobEntryTrans.parameters[idx], value);
                        }
                    }
                }
            }

            first = false;

            try
            {
                if (jobEntryTrans.isDetailed())
                    jobEntryTrans.logDetailed(BaseMessages.getString(PKG,
                            "JobTrans.StartingTrans",
                            jobEntryTrans.getFilename(),
                            jobEntryTrans.getName(),
                            jobEntryTrans.getDescription()));

                // Set the result rows for the next one...
                transMeta.setPreviousResult(result);

                if (jobEntryTrans.clearResultRows)
                {
                    transMeta.getPreviousResult().setRows(
                            new ArrayList<RowMetaAndData>());
                }

                if (jobEntryTrans.clearResultFiles)
                {
                    transMeta.getPreviousResult().getResultFiles().clear();
                }

                /*
                 * Set one or more "result" rows on the transformation...
                 */
                if (jobEntryTrans.execPerRow) // Execute for each input row
                {
                    if (jobEntryTrans.argFromPrevious) // Copy the input row to
                                                       // the (command
                    // line) arguments
                    {
                        args = null;
                        if (resultRow != null)
                        {
                            args = new String[resultRow.size()];
                            for (int i = 0; i < resultRow.size(); i++)
                            {
                                args[i] = resultRow.getString(i, null);
                            }
                        }
                    }
                    else
                    {
                        // Just pass a single row
                        List<RowMetaAndData> newList = new ArrayList<RowMetaAndData>();
                        newList.add(resultRow);

                        // This previous result rows list can be either empty or
                        // not.
                        // Depending on the checkbox "clear result rows"
                        // In this case, it would execute the transformation
                        // with one extra row each time
                        // Can't figure out a real use-case for it, but hey, who
                        // am I to decide that, right?
                        // :-)
                        //
                        transMeta.getPreviousResult().getRows().addAll(newList);
                    }

                    if (jobEntryTrans.paramsFromPrevious)
                    { // Copy the input the parameters

                        if (jobEntryTrans.parameters != null)
                        {
                            for (int idx = 0; idx < jobEntryTrans.parameters.length; idx++)
                            {
                                if (!Const
                                        .isEmpty(jobEntryTrans.parameters[idx]))
                                {
                                    // We have a parameter
                                    if (Const
                                            .isEmpty(Const
                                                    .trim(jobEntryTrans.parameterFieldNames[idx])))
                                    {
                                        namedParam
                                                .setParameterValue(
                                                        jobEntryTrans.parameters[idx],
                                                        Const.NVL(
                                                                jobEntryTrans
                                                                        .environmentSubstitute(jobEntryTrans.parameterValues[idx]),
                                                                ""));
                                    }
                                    else
                                    {
                                        String fieldValue = "";

                                        if (resultRow != null)
                                        {
                                            fieldValue = resultRow
                                                    .getString(
                                                            jobEntryTrans.parameterFieldNames[idx],
                                                            "");
                                        }
                                        // Get the value from the input stream
                                        namedParam.setParameterValue(
                                                jobEntryTrans.parameters[idx],
                                                Const.NVL(fieldValue, ""));
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    if (jobEntryTrans.argFromPrevious)
                    {
                        // Only put the first Row on the arguments
                        args = null;
                        if (resultRow != null)
                        {
                            args = new String[resultRow.size()];
                            for (int i = 0; i < resultRow.size(); i++)
                            {
                                args[i] = resultRow.getString(i, null);
                            }
                        }
                    }
                    else
                    {
                        // do nothing
                    }
                    if (jobEntryTrans.paramsFromPrevious)
                    { // Copy the input the parameters
                        if (jobEntryTrans.parameters != null)
                        {
                            for (int idx = 0; idx < jobEntryTrans.parameters.length; idx++)
                            {
                                if (!Const
                                        .isEmpty(jobEntryTrans.parameters[idx]))
                                {
                                    // We have a parameter
                                    if (Const
                                            .isEmpty(Const
                                                    .trim(jobEntryTrans.parameterFieldNames[idx])))
                                    {
                                        namedParam
                                                .setParameterValue(
                                                        jobEntryTrans.parameters[idx],
                                                        Const.NVL(
                                                                jobEntryTrans
                                                                        .environmentSubstitute(jobEntryTrans.parameterValues[idx]),
                                                                ""));
                                    }
                                    else
                                    {
                                        String fieldValue = "";

                                        if (resultRow != null)
                                        {
                                            fieldValue = resultRow
                                                    .getString(
                                                            jobEntryTrans.parameterFieldNames[idx],
                                                            "");
                                        }
                                        // Get the value from the input stream
                                        namedParam.setParameterValue(
                                                jobEntryTrans.parameters[idx],
                                                Const.NVL(fieldValue, ""));
                                    }
                                }
                            }
                        }
                    }
                }

                // Handle the parameters...
                //
                transMeta.clearParameters();
                String[] parameterNames = transMeta.listParameters();
                for (int idx = 0; idx < parameterNames.length; idx++)
                {
                    // Grab the parameter value set in the Trans job entry
                    //
                    String thisValue = namedParam
                            .getParameterValue(parameterNames[idx]);
                    if (!Const.isEmpty(thisValue))
                    {
                        // Set the value as specified by the user in the job
                        // entry
                        //
                        transMeta.setParameterValue(parameterNames[idx],
                                thisValue);
                    }
                    else
                    {
                        // See if the parameter had a value set in the parent
                        // job...
                        // This value should pass down to the transformation if
                        // that's what we opted to do.
                        //
                        if (jobEntryTrans.isPassingAllParameters())
                        {
                            String parentValue = jobEntryTrans.getParentJob()
                                    .getParameterValue(parameterNames[idx]);
                            if (!Const.isEmpty(parentValue))
                            {
                                transMeta.setParameterValue(
                                        parameterNames[idx], parentValue);
                            }
                        }
                    }
                }

                // Execute this transformation across a cluster of servers
                //
                if (jobEntryTrans.isClustering())
                {
                    TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
                    executionConfiguration.setClusterPosting(true);
                    executionConfiguration.setClusterPreparing(true);
                    executionConfiguration.setClusterStarting(true);
                    executionConfiguration
                            .setClusterShowingTransformation(false);
                    executionConfiguration.setSafeModeEnabled(false);
                    executionConfiguration.setRepository(jobEntryTrans
                            .getRepository());
                    executionConfiguration.setLogLevel(transLogLevel);

                    // Also pass the variables from the transformation into the
                    // execution configuration
                    // That way it can go over the HTTP connection to the slave
                    // server.
                    //
                    executionConfiguration.setVariables(transMeta);

                    // Also set the arguments...
                    //
                    executionConfiguration.setArgumentStrings(args);

                    TransSplitter transSplitter = null;
                    long errors = 0;
                    try
                    {
                        transSplitter = TransProv.executeClustered(transMeta,
                                executionConfiguration);

                        // Monitor the running transformations, wait until they
                        // are done.
                        // Also kill them all if anything goes bad
                        // Also clean up afterwards...
                        //
                        errors += TransProv.monitorClusteredTransformation(
                                jobEntryTrans.getLogChannel(), transSplitter,
                                jobEntryTrans.getParentJob());

                    }
                    catch (Exception e)
                    {
                        jobEntryTrans
                                .logError(
                                        "Error during clustered execution. Cleaning up clustered execution.",
                                        e);
                        // In case something goes wrong, make sure to clean up
                        // afterwards!
                        //
                        errors++;
                        if (transSplitter != null)
                        {
                            TransProv.cleanupCluster(
                                    jobEntryTrans.getLogChannel(),
                                    transSplitter);
                        }
                        else
                        {
                            // Try to clean anyway...
                            //
                            SlaveServer master = null;
                            for (StepMeta stepMeta : transMeta.getSteps())
                            {
                                if (stepMeta.isClustered())
                                {
                                    for (SlaveServer slaveServer : stepMeta
                                            .getClusterSchema()
                                            .getSlaveServers())
                                    {
                                        if (slaveServer.isMaster())
                                        {
                                            master = slaveServer;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (master != null)
                            {
                                master.deAllocateServerSockets(
                                        transMeta.getName(), null);
                            }
                        }
                    }

                    result.clear();

                    if (transSplitter != null)
                    {
                        Result clusterResult = TransProv
                                .getClusteredTransformationResult(
                                        jobEntryTrans.getLogChannel(),
                                        transSplitter,
                                        jobEntryTrans.getParentJob());
                        result.add(clusterResult);
                    }

                    result.setNrErrors(result.getNrErrors() + errors);
                }
                // Execute this transformation remotely
                //
                else if (remoteSlaveServer != null)
                {

                    // Make sure we can parameterize the slave server connection
                    //
                    remoteSlaveServer.shareVariablesWith(jobEntryTrans);

                    // Remote execution...
                    //
                    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
                    transExecutionConfiguration.setPreviousResult(transMeta
                            .getPreviousResult().clone());
                    transExecutionConfiguration.setArgumentStrings(args);
                    transExecutionConfiguration.setVariables(jobEntryTrans);
                    transExecutionConfiguration
                            .setRemoteServer(remoteSlaveServer);
                    transExecutionConfiguration.setLogLevel(transLogLevel);
                    transExecutionConfiguration.setRepository(jobEntryTrans
                            .getRepository());

                    Map<String, String> params = transExecutionConfiguration
                            .getParams();
                    for (String param : transMeta.listParameters())
                    {
                        String value = Const.NVL(transMeta
                                .getParameterValue(param), Const.NVL(
                                transMeta.getParameterDefault(param),
                                transMeta.getVariable(param)));
                        params.put(param, value);
                    }

                    // Send the XML over to the slave server
                    // Also start the transformation over there...
                    //
                    String carteObjectId = TransProv.sendToSlaveServer(
                            transMeta, transExecutionConfiguration,
                            jobEntryTrans.getRepository());

                    // Now start the monitoring...
                    //
                    SlaveServerTransStatus transStatus = null;
                    while (!jobEntryTrans.getParentJob().isStopped()
                            && jobEntryTrans.waitingToFinish)
                    {
                        try
                        {
                            transStatus = remoteSlaveServer.getTransStatus(
                                    transMeta.getName(), carteObjectId, 0);
                            if (!transStatus.isRunning())
                            {
                                // The transformation is finished, get the
                                // result...
                                //
                                Result remoteResult = transStatus.getResult();
                                result.clear();
                                result.add(remoteResult);

                                // In case you manually stop the remote trans
                                // (browser etc), make sure it's marked as an
                                // error
                                //
                                if (remoteResult.isStopped())
                                {
                                    result.setNrErrors(result.getNrErrors() + 1); //
                                }

                                // Make sure to clean up : write a log record
                                // etc, close any left-over sockets etc.
                                //
                                remoteSlaveServer.cleanupTransformation(
                                        transMeta.getName(), carteObjectId);

                                break;
                            }
                        }
                        catch (Exception e1)
                        {

                            jobEntryTrans
                                    .logError(BaseMessages
                                            .getString(
                                                    PKG,
                                                    "JobTrans.Error.UnableContactSlaveServer",
                                                    "" + remoteSlaveServer,
                                                    transMeta.getName()));
                            result.setNrErrors(result.getNrErrors() + 1L);
                            break; // Stop looking too, chances are too low the
                                   // server will come back on-line
                        }

                        try
                        {
                            Thread.sleep(2000);
                        }
                        catch (InterruptedException e)
                        {
                        }
                        ; // sleep for 2 seconds
                    }

                    if (jobEntryTrans.getParentJob().isStopped())
                    {
                        // See if we have a status and if we need to stop the
                        // remote execution here...
                        //
                        if (transStatus == null || transStatus.isRunning())
                        {
                            // Try a remote abort ...
                            //
                            remoteSlaveServer.stopTransformation(
                                    transMeta.getName(), transStatus.getId());

                            // And a cleanup...
                            //
                            remoteSlaveServer.cleanupTransformation(
                                    transMeta.getName(), transStatus.getId());

                            // Set an error state!
                            //
                            result.setNrErrors(result.getNrErrors() + 1L);
                        }
                    }
                }
                // Execute this transformation on the local machine
                //
                else
                // Local execution...
                {
                    // Create the transformation from meta-data
                    //
                    TransProv trans = new TransProv(transMeta, jobEntryTrans);
                    trans.setLogLevel(transLogLevel);

                    // Pass the socket repository as early as possible...
                    //
                    trans.setSocketRepository(jobEntryTrans.getParentJob()
                            .getSocketRepository());

                    if (jobEntryTrans.getParentJob().getJobMeta()
                            .isBatchIdPassed())
                    {
                        trans.setPassedBatchId(jobEntryTrans.getParentJob()
                                .getPassedBatchId());
                    }

                    // set the parent job on the transformation, variables are
                    // taken from here...
                    //
                    trans.setParentJob(jobEntryTrans.getParentJob());
                    trans.setParentVariableSpace(jobEntryTrans.getParentJob());

                    // Mappings need the repository to load from
                    //
                    trans.setRepository(jobEntryTrans.getRepository());

                    // First get the root job
                    //
                    Job rootJob = jobEntryTrans.getParentJob();
                    while (rootJob.getParentJob() != null)
                        rootJob = rootJob.getParentJob();

                    // Get the start and end-date from the root job...
                    //
                    trans.setJobStartDate(rootJob.getStartDate());
                    trans.setJobEndDate(rootJob.getEndDate());

                    Database db = null;
                    try
                    {
                        // Start execution...
                        //
                        // Rogers: Quebra a execucao para inserir os listeners
                        // trans.execute(args);
                        // Rogers: prepara a execucao
                        trans.prepareExecution(args);

                        DatabaseMeta dbTransMeta = this.rootJobDec
                                .getProvConnection();

                        // Rogers: Insere os TransListeners
                        db = ParentProvenanceListener.getDatabase(trans, trans,
                                dbTransMeta);
                        trans.addTransListener(new ProvenanceTransListener(db,
                                this.rootJobDec));

                        // Rogers: Insere os TransStoppedListener
                        trans.addTransStoppedListener(new ProvenanceTransStoppedListener(
                                db, this.rootJobDec));

                        // Rogers: Insere os RowListeners e StepListeners nos
                        // steps
                        List<StepMetaDataCombi> steps = trans.getSteps();
                        if ((steps != null) && (steps.size() > 0)
                                && (steps.get(0).step != null)
                                && (steps.get(0).step instanceof StepDecorator))
                        {
                            db = ParentProvenanceListener.getDatabase(trans,
                                    trans, dbTransMeta);
                            for (StepMetaDataCombi combi : steps)
                            {
                                StepDecorator stepDecorator = (StepDecorator) combi.step;

                                stepDecorator
                                        .addProvStepListeners(new ProvenanceStepListener(
                                                db, combi.step,
                                                this.rootJobDec, trans));
                                stepDecorator
                                        .addRowListener(new ProvenanceRowListener(
                                                db, combi.step,
                                                this.rootJobDec, trans));
                            }
                        }

                        // Rogers: Inicia as threads
                        trans.startThreads();

                        // Wait until we're done with it...
                        //
                        while (!trans.isFinished()
                                && !jobEntryTrans.getParentJob().isStopped()
                                && trans.getErrors() == 0)
                        {
                            try
                            {
                                Thread.sleep(0, 500);
                            }
                            catch (InterruptedException e)
                            {
                            }
                        }

                        if (jobEntryTrans.getParentJob().isStopped()
                                || trans.getErrors() != 0)
                        {
                            trans.stopAll();
                            trans.waitUntilFinished();
                            result.setNrErrors(1);
                        }
                        Result newResult = trans.getResult();

                        result.clear(); // clear only the numbers, NOT the files
                                        // or rows.
                        result.add(newResult);

                        // Set the result rows too...
                        result.setRows(newResult.getRows());

                        if (jobEntryTrans.setLogfile)
                        {
                            ResultFile resultFile = new ResultFile(
                                    ResultFile.FILE_TYPE_LOG,
                                    KettleVFS.getFileObject(realLogFilename,
                                            jobEntryTrans), jobEntryTrans
                                            .getParentJob().getJobname(),
                                    toString());
                            result.getResultFiles()
                                    .put(resultFile.getFile().toString(),
                                            resultFile);
                        }
                    }
                    catch (KettleException e)
                    {
                        jobEntryTrans.logError(BaseMessages.getString(PKG,
                                "JobTrans.Error.UnablePrepareExec"), e);
                        result.setNrErrors(1);
                    }
                }
            }
            catch (Exception e)
            {

                jobEntryTrans.logError(BaseMessages.getString(PKG,
                        "JobTrans.ErrorUnableOpenTrans", e.getMessage()));
                jobEntryTrans.logError(Const.getStackTracker(e));
                result.setNrErrors(1);
            }
            iteration++;
        }

        if (jobEntryTrans.setLogfile)
        {
            if (appender != null)
            {
                LogWriter.getInstance().removeAppender(appender);
                appender.close();

                ResultFile resultFile = new ResultFile(
                        ResultFile.FILE_TYPE_LOG, appender.getFile(),
                        jobEntryTrans.getParentJob().getJobname(),
                        jobEntryTrans.getName());
                result.getResultFiles().put(resultFile.getFile().toString(),
                        resultFile);
            }
        }

        if (result.getNrErrors() == 0)
        {
            result.setResult(true);
        }
        else
        {
            result.setResult(false);
        }

        return result;
    }

    @Override
    public void loadXML(Node entrynode, List<DatabaseMeta> databases,
            List<SlaveServer> slaveServers, Repository rep)
            throws KettleXMLException
    {
        jobEntryTrans.loadXML(entrynode, databases, slaveServers, rep);
    }

    private boolean createParentFolder(String filename)
    {
        // Check for parent folder
        FileObject parentfolder = null;
        boolean resultat = true;
        try
        {
            // Get parent folder
            parentfolder = KettleVFS.getFileObject(filename, jobEntryTrans)
                    .getParent();
            if (!parentfolder.exists())
            {
                if (jobEntryTrans.createParentFolder)
                {
                    if (jobEntryTrans.isDebug())
                        jobEntryTrans.logDebug(BaseMessages.getString(PKG,
                                "JobTrans.Log.ParentLogFolderNotExist",
                                parentfolder.getName().toString()));
                    parentfolder.createFolder();
                    if (jobEntryTrans.isDebug())
                        jobEntryTrans.logDebug(BaseMessages.getString(PKG,
                                "JobTrans.Log.ParentLogFolderCreated",
                                parentfolder.getName().toString()));
                }
                else
                {
                    jobEntryTrans.logError(BaseMessages.getString(PKG,
                            "JobTrans.Log.ParentLogFolderNotExist",
                            parentfolder.getName().toString()));
                    resultat = false;
                }
            }
            else
            {
                if (jobEntryTrans.isDebug())
                    jobEntryTrans.logDebug(BaseMessages.getString(PKG,
                            "JobTrans.Log.ParentLogFolderExists", parentfolder
                                    .getName().toString()));
            }
        }
        catch (Exception e)
        {
            resultat = false;
            jobEntryTrans.logError(BaseMessages.getString(PKG,
                    "JobTrans.Error.ChekingParentLogFolderTitle"), BaseMessages
                    .getString(PKG, "JobTrans.Error.ChekingParentLogFolder",
                            parentfolder.getName().toString()), e);
        }
        finally
        {
            if (parentfolder != null)
            {
                try
                {
                    parentfolder.close();
                    parentfolder = null;
                }
                catch (Exception ex)
                {
                }
            }
        }

        return resultat;
    }

    @Override
    public void setParentJob(Job job)
    {
        jobEntryTrans.setParentJob(job);
    }

    @Override
    public Job getParentJob()
    {
        return jobEntryTrans.getParentJob();
    }

    @Override
    public LogChannelInterface getLogChannel()
    {
        return jobEntryTrans.getLogChannel();
    }

    @Override
    public void setRepository(Repository repository)
    {
        jobEntryTrans.setRepository(repository);
    }

    @Override
    public void clear()
    {
        jobEntryTrans.clear();

    }

    @Override
    public ObjectId getObjectId()
    {
        return jobEntryTrans.getObjectId();
    }

    @Override
    public void setObjectId(ObjectId id)
    {
        jobEntryTrans.setObjectId(id);

    }

    @Override
    public String getName()
    {
        return jobEntryTrans.getName();
    }

    @Override
    public void setName(String name)
    {
        jobEntryTrans.setName(name);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getTypeId()
    {
        return jobEntryTrans.getTypeId();
    }

    @Override
    public String getPluginId()
    {
        return jobEntryTrans.getPluginId();
    }

    @Override
    public void setPluginId(String pluginId)
    {
        jobEntryTrans.setPluginId(pluginId);
    }

    @Override
    public String getDescription()
    {
        return jobEntryTrans.getDescription();
    }

    @Override
    public void setDescription(String description)
    {
        jobEntryTrans.setDescription(description);
    }

    @Override
    public void setChanged()
    {
        jobEntryTrans.setChanged();

    }

    @Override
    public void setChanged(boolean ch)
    {
        jobEntryTrans.setChanged(ch);

    }

    @Override
    public boolean hasChanged()
    {
        return jobEntryTrans.hasChanged();
    }

    @Override
    public String getXML()
    {
        return jobEntryTrans.getXML();
    }

    @Override
    public void saveRep(Repository rep, ObjectId id_job) throws KettleException
    {
        jobEntryTrans.saveRep(rep, id_job);
    }

    @Override
    public void loadRep(Repository rep, ObjectId id_jobentry,
            List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
            throws KettleException
    {
        jobEntryTrans.loadRep(rep, id_jobentry, databases, slaveServers);
    }

    @Override
    public boolean isStart()
    {
        return jobEntryTrans.isStart();
    }

    @Override
    public boolean isDummy()
    {
        return jobEntryTrans.isDummy();
    }

    @Override
    public boolean resetErrorsBeforeExecution()
    {
        return jobEntryTrans.resetErrorsBeforeExecution();
    }

    @Override
    public boolean evaluates()
    {
        return jobEntryTrans.evaluates();
    }

    @Override
    public boolean isUnconditional()
    {
        return jobEntryTrans.isUnconditional();
    }

    @Override
    public boolean isEvaluation()
    {
        return jobEntryTrans.isEvaluation();
    }

    @Override
    public boolean isTransformation()
    {
        return jobEntryTrans.isTransformation();
    }

    @Override
    public boolean isJob()
    {
        return jobEntryTrans.isJob();
    }

    @Override
    public boolean isShell()
    {
        return jobEntryTrans.isShell();
    }

    @Override
    public boolean isMail()
    {
        return jobEntryTrans.isMail();
    }

    @Override
    public boolean isSpecial()
    {
        return jobEntryTrans.isSpecial();
    }

    @Override
    public List<SQLStatement> getSQLStatements(Repository repository)
            throws KettleException
    {
        return jobEntryTrans.getSQLStatements(repository);
    }

    @Override
    public List<SQLStatement> getSQLStatements(Repository repository,
            VariableSpace space) throws KettleException
    {
        return jobEntryTrans.getSQLStatements(repository, space);
    }

    @Override
    public String getDialogClassName()
    {
        return jobEntryTrans.getDialogClassName();
    }

    @Override
    public String getFilename()
    {
        return jobEntryTrans.getFilename();
    }

    @Override
    public String getRealFilename()
    {
        return jobEntryTrans.getRealFilename();
    }

    @Override
    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        return jobEntryTrans.getUsedDatabaseConnections();
    }

    @Override
    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
    {
        jobEntryTrans.check(remarks, jobMeta);
    }

    @Override
    public List<ResourceReference> getResourceDependencies(JobMeta jobMeta)
    {
        return jobEntryTrans.getResourceDependencies(jobMeta);
    }

    @Override
    public String exportResources(VariableSpace space,
            Map<String, ResourceDefinition> definitions,
            ResourceNamingInterface namingInterface, Repository repository)
            throws KettleException
    {
        return jobEntryTrans.exportResources(space, definitions,
                namingInterface, repository);
    }

    @Override
    public boolean hasRepositoryReferences()
    {
        return jobEntryTrans.hasRepositoryReferences();
    }

    @Override
    public void lookupRepositoryReferences(Repository repository)
            throws KettleException
    {
        jobEntryTrans.lookupRepositoryReferences(repository);
    }

    @Override
    public Object clone()
    {
        JobEntryTransDecorator je;
        try
        {
            je = (JobEntryTransDecorator) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            je = null;
        }
        return je;
    }

    @Override
    public String getObjectName()
    {
        return jobEntryTrans.getObjectName();
    }

    @Override
    public RepositoryDirectory getRepositoryDirectory()
    {
        return jobEntryTrans.getRepositoryDirectory();
    }

    @Override
    public ObjectRevision getObjectRevision()
    {
        return jobEntryTrans.getObjectRevision();
    }

    @Override
    public String getLogChannelId()
    {
        return jobEntryTrans.getLogChannelId();
    }

    @Override
    public LoggingObjectInterface getParent()
    {
        return jobEntryTrans.getParent();
    }

    @Override
    public LoggingObjectType getObjectType()
    {
        return jobEntryTrans.getObjectType();
    }

    @Override
    public String getObjectCopy()
    {
        return jobEntryTrans.getObjectCopy();
    }

    @Override
    public LogLevel getLogLevel()
    {
        return jobEntryTrans.getLogLevel();
    }

    @Override
    public String getContainerObjectId()
    {
        return jobEntryTrans.getContainerObjectId();
    }

    @Override
    public Date getRegistrationDate()
    {
        return jobEntryTrans.getRegistrationDate();
    }

    @Override
    public String getHolderType()
    {
        return jobEntryTrans.getHolderType();
    }

    @Override
    public void initializeVariablesFrom(VariableSpace parent)
    {
        jobEntryTrans.initializeVariablesFrom(parent);
    }

    @Override
    public void copyVariablesFrom(VariableSpace space)
    {
        jobEntryTrans.copyVariablesFrom(space);
    }

    @Override
    public void shareVariablesWith(VariableSpace space)
    {
        jobEntryTrans.shareVariablesWith(space);
    }

    @Override
    public VariableSpace getParentVariableSpace()
    {
        return jobEntryTrans.getParentVariableSpace();
    }

    @Override
    public void setParentVariableSpace(VariableSpace parent)
    {
        jobEntryTrans.setParentVariableSpace(parent);

    }

    @Override
    public void setVariable(String variableName, String variableValue)
    {
        jobEntryTrans.setVariable(variableName, variableValue);
    }

    @Override
    public String getVariable(String variableName, String defaultValue)
    {
        return jobEntryTrans.getVariable(variableName);
    }

    @Override
    public String getVariable(String variableName)
    {
        return jobEntryTrans.getVariable(variableName);
    }

    @Override
    public boolean getBooleanValueOfVariable(String variableName,
            boolean defaultValue)
    {
        return jobEntryTrans.getBooleanValueOfVariable(variableName,
                defaultValue);
    }

    @Override
    public String[] listVariables()
    {
        return jobEntryTrans.listVariables();
    }

    @Override
    public String environmentSubstitute(String aString)
    {
        return jobEntryTrans.environmentSubstitute(aString);
    }

    @Override
    public String[] environmentSubstitute(String[] string)
    {
        return jobEntryTrans.environmentSubstitute(string);
    }

    @Override
    public void injectVariables(Map<String, String> prop)
    {
        jobEntryTrans.injectVariables(prop);

    }
}
