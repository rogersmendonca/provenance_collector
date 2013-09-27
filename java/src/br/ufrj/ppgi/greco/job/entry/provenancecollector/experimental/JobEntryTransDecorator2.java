package br.ufrj.ppgi.greco.job.entry.provenancecollector.experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.w3c.dom.Node;

/* Experimental: JobEntryTransDecorator extendendo JobEntryBase ... 
 * Outra opcao (JobEntryTransDecorator): implementar as interfaces do JobEntryBase 
 * */
public class JobEntryTransDecorator2 extends JobEntryBase implements Cloneable,
        JobEntryInterface
{
    private static Class<?> PKG = JobEntryTrans.class; // for i18n purposes,
                                                       // needed by
                                                       // Translator2!!
                                                       // $NON-NLS-1$
    private JobEntryTrans jobEntryTrans;

    public JobEntryTransDecorator2(JobEntryTrans jobEntryTrans)
    {
        this.jobEntryTrans = jobEntryTrans;
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
    public Result execute(Result result, int nr) throws KettleException
    {
        result.setEntryNr(nr);

        Log4jFileAppender appender = null;

        LogLevel transLogLevel = parentJob.getLogLevel();

        String realLogFilename = "";
        if (jobEntryTrans.setLogfile)
        {
            transLogLevel = jobEntryTrans.logFileLevel;

            realLogFilename = environmentSubstitute(getLogFilename());

            // We need to check here the log filename
            // if we do not have one, we must fail
            if (Const.isEmpty(realLogFilename))
            {
                logError(BaseMessages.getString(PKG,
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
                logError(BaseMessages.getString(PKG,
                        "JobTrans.Error.UnableOpenAppender", realLogFilename,
                        e.toString()));

                logError(Const.getStackTracker(e));
                result.setNrErrors(1);
                result.setResult(false);
                return result;
            }
        }

        // Figure out the remote slave server...
        //
        SlaveServer remoteSlaveServer = null;
        if (!Const.isEmpty(getRemoteSlaveServerName()))
        {
            String realRemoteSlaveServerName = environmentSubstitute(getRemoteSlaveServerName());
            remoteSlaveServer = parentJob.getJobMeta().findSlaveServer(
                    realRemoteSlaveServerName);
            if (remoteSlaveServer == null)
            {
                throw new KettleException(BaseMessages.getString(PKG,
                        "JobTrans.Exception.UnableToFindRemoteSlaveServer",
                        realRemoteSlaveServerName));
            }
        }

        // Open the transformation...
        //
        switch (getSpecificationMethod())
        {
        case FILENAME:
            if (isDetailed())
            {
                logDetailed(BaseMessages.getString(PKG,
                        "JobTrans.Log.OpeningTrans",
                        environmentSubstitute(getFilename())));
            }
            break;
        case REPOSITORY_BY_NAME:
            if (isDetailed())
            {
                logDetailed(BaseMessages.getString(PKG,
                        "JobTrans.Log.OpeningTransInDirec",
                        environmentSubstitute(getFilename()),
                        environmentSubstitute(getDirectory())));
            }
            break;
        case REPOSITORY_BY_REFERENCE:
            if (isDetailed())
            {
                logDetailed(BaseMessages.getString(PKG,
                        "JobTrans.Log.OpeningTransByReference",
                        getTransObjectId()));
            }
            break;
        }

        // Load the transformation only once for the complete loop!
        // Throws an exception if it was not possible to load the
        // transformation. For example, the XML file doesn't exist or the
        // repository is down.
        // Log the stack trace and return an error condition from this
        //
        TransMeta transMeta = getTransMeta(rep, this);

        int iteration = 0;
        String args1[] = jobEntryTrans.arguments;
        if (args1 == null || args1.length == 0) // No arguments set, look at the
                                                // parent job.
        {
            args1 = parentJob.getJobMeta().getArguments();
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
                args[idx] = environmentSubstitute(args1[idx]);
            }
        }

        RowMetaAndData resultRow = null;
        boolean first = true;
        List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>(
                result.getRows());

        while ((first && !jobEntryTrans.execPerRow)
                || (jobEntryTrans.execPerRow && rows != null
                        && iteration < rows.size() && result.getNrErrors() == 0)
                && !parentJob.isStopped())
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
                                    .NVL(environmentSubstitute(jobEntryTrans.parameterValues[idx]),
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
                if (isDetailed())
                    logDetailed(BaseMessages.getString(PKG,
                            "JobTrans.StartingTrans", getFilename(), getName(),
                            getDescription()));

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
                                                                environmentSubstitute(jobEntryTrans.parameterValues[idx]),
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
                                                                environmentSubstitute(jobEntryTrans.parameterValues[idx]),
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
                        if (isPassingAllParameters())
                        {
                            String parentValue = parentJob
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
                if (isClustering())
                {
                    TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
                    executionConfiguration.setClusterPosting(true);
                    executionConfiguration.setClusterPreparing(true);
                    executionConfiguration.setClusterStarting(true);
                    executionConfiguration
                            .setClusterShowingTransformation(false);
                    executionConfiguration.setSafeModeEnabled(false);
                    executionConfiguration.setRepository(rep);
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
                        transSplitter = Trans.executeClustered(transMeta,
                                executionConfiguration);

                        // Monitor the running transformations, wait until they
                        // are done.
                        // Also kill them all if anything goes bad
                        // Also clean up afterwards...
                        //
                        errors += Trans.monitorClusteredTransformation(log,
                                transSplitter, parentJob);

                    }
                    catch (Exception e)
                    {
                        logError(
                                "Error during clustered execution. Cleaning up clustered execution.",
                                e);
                        // In case something goes wrong, make sure to clean up
                        // afterwards!
                        //
                        errors++;
                        if (transSplitter != null)
                        {
                            Trans.cleanupCluster(log, transSplitter);
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
                        Result clusterResult = Trans
                                .getClusteredTransformationResult(log,
                                        transSplitter, parentJob);
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
                    remoteSlaveServer.shareVariablesWith(this);

                    // Remote execution...
                    //
                    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
                    transExecutionConfiguration.setPreviousResult(transMeta
                            .getPreviousResult().clone());
                    transExecutionConfiguration.setArgumentStrings(args);
                    transExecutionConfiguration.setVariables(this);
                    transExecutionConfiguration
                            .setRemoteServer(remoteSlaveServer);
                    transExecutionConfiguration.setLogLevel(transLogLevel);
                    transExecutionConfiguration.setRepository(rep);

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
                    String carteObjectId = Trans.sendToSlaveServer(transMeta,
                            transExecutionConfiguration, rep);

                    // Now start the monitoring...
                    //
                    SlaveServerTransStatus transStatus = null;
                    while (!parentJob.isStopped()
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

                            logError(BaseMessages
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

                    if (parentJob.isStopped())
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
                    Trans trans = new Trans(transMeta, this);
                    trans.setLogLevel(transLogLevel);

                    // Pass the socket repository as early as possible...
                    //
                    trans.setSocketRepository(parentJob.getSocketRepository());

                    if (parentJob.getJobMeta().isBatchIdPassed())
                    {
                        trans.setPassedBatchId(parentJob.getPassedBatchId());
                    }

                    // set the parent job on the transformation, variables are
                    // taken from here...
                    //
                    trans.setParentJob(parentJob);
                    trans.setParentVariableSpace(parentJob);

                    // Mappings need the repository to load from
                    //
                    trans.setRepository(rep);

                    // First get the root job
                    //
                    Job rootJob = parentJob;
                    while (rootJob.getParentJob() != null)
                        rootJob = rootJob.getParentJob();

                    // Get the start and end-date from the root job...
                    //
                    trans.setJobStartDate(rootJob.getStartDate());
                    trans.setJobEndDate(rootJob.getEndDate());

                    try
                    {
                        // Start execution...
                        //
                        trans.execute(args);

                        // Wait until we're done with it...
                        //
                        while (!trans.isFinished() && !parentJob.isStopped()
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

                        if (parentJob.isStopped() || trans.getErrors() != 0)
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
                                            this), parentJob.getJobname(),
                                    toString());
                            result.getResultFiles()
                                    .put(resultFile.getFile().toString(),
                                            resultFile);
                        }
                    }
                    catch (KettleException e)
                    {

                        logError(BaseMessages.getString(PKG,
                                "JobTrans.Error.UnablePrepareExec"), e);
                        result.setNrErrors(1);
                    }
                }
            }
            catch (Exception e)
            {

                logError(BaseMessages.getString(PKG,
                        "JobTrans.ErrorUnableOpenTrans", e.getMessage()));
                logError(Const.getStackTracker(e));
                result.setNrErrors(1);
            }
            iteration++;
        }

        /*
         * for (String childId :
         * LoggingRegistry.getInstance().getLogChannelChildren
         * (parentJob.getLogChannelId())) { LoggingObjectInterface loggingObject
         * = LoggingRegistry.getInstance().getLoggingObject(childId);
         * LoggingObjectInterface parent = loggingObject.getParent();
         * System.out.
         * println("child log channel id="+childId+" : "+loggingObject
         * .getObjectName
         * ()+":"+loggingObject.getObjectType()+"  parent="+(parent
         * !=null?parent.getObjectName():"")); }
         * 
         * CentralLogStore.getAppender().getBuffer(parentJob.getLogChannelId(),
         * false);
         */

        if (jobEntryTrans.setLogfile)
        {
            if (appender != null)
            {
                LogWriter.getInstance().removeAppender(appender);
                appender.close();

                ResultFile resultFile = new ResultFile(
                        ResultFile.FILE_TYPE_LOG, appender.getFile(),
                        parentJob.getJobname(), getName());
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

    public Object clone()
    {
        return jobEntryTrans.clone();
    }

    public void setFileName(String n)
    {
        jobEntryTrans.setFileName(n);
    }

    /**
     * @deprecated use getFilename() instead
     * @return the filename
     */
    public String getFileName()
    {
        return jobEntryTrans.getFileName();
    }

    public String getFilename()
    {
        return jobEntryTrans.getFilename();
    }

    public String getRealFilename()
    {
        return jobEntryTrans.getRealFilename();
    }

    public void setTransname(String transname)
    {
        jobEntryTrans.setTransname(transname);
    }

    public String getTransname()
    {
        return jobEntryTrans.getTransname();
    }

    public String getDirectory()
    {
        return jobEntryTrans.getDirectory();
    }

    public void setDirectory(String directory)
    {
        jobEntryTrans.setDirectory(directory);
    }

    public String getLogFilename()
    {
        return jobEntryTrans.getLogFilename();
    }

    public String getXML()
    {
        return jobEntryTrans.getXML();
    }

    public void loadXML(Node entrynode, List<DatabaseMeta> databases,
            List<SlaveServer> slaveServers, Repository rep)
            throws KettleXMLException
    {
        jobEntryTrans.loadXML(entrynode, databases, slaveServers, rep);
    }

    // Load the jobentry from repository
    //
    public void loadRep(Repository rep, ObjectId id_jobentry,
            List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
            throws KettleException
    {
        jobEntryTrans.loadRep(rep, id_jobentry, databases, slaveServers);
    }

    // Save the attributes of this job entry
    //
    public void saveRep(Repository rep, ObjectId id_job) throws KettleException
    {
        jobEntryTrans.saveRep(rep, id_job);
    }

    public void clear()
    {
        jobEntryTrans.clear();
    }

    public TransMeta getTransMeta(Repository rep, VariableSpace space)
            throws KettleException
    {
        return jobEntryTrans.getTransMeta(rep, space);
    }

    public boolean evaluates()
    {
        return jobEntryTrans.evaluates();
    }

    public boolean isUnconditional()
    {
        return jobEntryTrans.isUnconditional();
    }

    public List<SQLStatement> getSQLStatements(Repository repository,
            VariableSpace space) throws KettleException
    {
        return jobEntryTrans.getSQLStatements(repository, space);
    }

    public List<SQLStatement> getSQLStatements(Repository repository)
            throws KettleException
    {
        return jobEntryTrans.getSQLStatements(repository);
    }

    public String getDirectoryPath()
    {
        return jobEntryTrans.getDirectoryPath();
    }

    public void setDirectoryPath(String directoryPath)
    {
        jobEntryTrans.setDirectoryPath(directoryPath);
    }

    public boolean isClustering()
    {
        return jobEntryTrans.isClustering();
    }

    public void setClustering(boolean clustering)
    {
        jobEntryTrans.setClustering(clustering);
    }

    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
    {
        jobEntryTrans.check(remarks, jobMeta);
    }

    public List<ResourceReference> getResourceDependencies(JobMeta jobMeta)
    {
        return jobEntryTrans.getResourceDependencies(jobMeta);
    }

    public String exportResources(VariableSpace space,
            Map<String, ResourceDefinition> definitions,
            ResourceNamingInterface namingInterface, Repository repository)
            throws KettleException
    {
        return jobEntryTrans.exportResources(space, definitions,
                namingInterface, repository);
    }

    public String getRemoteSlaveServerName()
    {
        return jobEntryTrans.getRemoteSlaveServerName();
    }

    public void setRemoteSlaveServerName(String remoteSlaveServerName)
    {
        jobEntryTrans.setRemoteSlaveServerName(remoteSlaveServerName);
    }

    public boolean isWaitingToFinish()
    {
        return jobEntryTrans.isWaitingToFinish();
    }

    public void setWaitingToFinish(boolean waitingToFinish)
    {
        jobEntryTrans.setWaitingToFinish(waitingToFinish);
    }

    public boolean isFollowingAbortRemotely()
    {
        return jobEntryTrans.isFollowingAbortRemotely();
    }

    public void setFollowingAbortRemotely(boolean followingAbortRemotely)
    {
        jobEntryTrans.setFollowingAbortRemotely(followingAbortRemotely);
    }

    public boolean isPassingAllParameters()
    {
        return jobEntryTrans.isPassingAllParameters();
    }

    public void setPassingAllParameters(boolean passingAllParameters)
    {
        jobEntryTrans.setPassingAllParameters(passingAllParameters);
    }

    public Trans getTrans()
    {
        return jobEntryTrans.getTrans();
    }

    public ObjectId getTransObjectId()
    {
        return jobEntryTrans.getTransObjectId();
    }

    public void setTransObjectId(ObjectId transObjectId)
    {
        jobEntryTrans.setTransObjectId(transObjectId);
    }

    public ObjectLocationSpecificationMethod getSpecificationMethod()
    {
        return jobEntryTrans.getSpecificationMethod();
    }

    public void setSpecificationMethod(
            ObjectLocationSpecificationMethod specificationMethod)
    {
        jobEntryTrans.setSpecificationMethod(specificationMethod);
    }

    public boolean hasRepositoryReferences()
    {
        return jobEntryTrans.hasRepositoryReferences();

    }

    public void lookupRepositoryReferences(Repository repository)
            throws KettleException
    {
        jobEntryTrans.lookupRepositoryReferences(repository);
    }

    private boolean createParentFolder(String filename)
    {
        // Check for parent folder
        FileObject parentfolder = null;
        boolean resultat = true;
        try
        {
            // Get parent folder
            parentfolder = KettleVFS.getFileObject(filename, this).getParent();
            if (!parentfolder.exists())
            {
                if (jobEntryTrans.createParentFolder)
                {
                    if (isDebug())
                        logDebug(BaseMessages.getString(PKG,
                                "JobTrans.Log.ParentLogFolderNotExist",
                                parentfolder.getName().toString()));
                    parentfolder.createFolder();
                    if (isDebug())
                        logDebug(BaseMessages.getString(PKG,
                                "JobTrans.Log.ParentLogFolderCreated",
                                parentfolder.getName().toString()));
                }
                else
                {
                    logError(BaseMessages.getString(PKG,
                            "JobTrans.Log.ParentLogFolderNotExist",
                            parentfolder.getName().toString()));
                    resultat = false;
                }
            }
            else
            {
                if (isDebug())
                    logDebug(BaseMessages.getString(PKG,
                            "JobTrans.Log.ParentLogFolderExists", parentfolder
                                    .getName().toString()));
            }
        }
        catch (Exception e)
        {
            resultat = false;
            logError(BaseMessages.getString(PKG,
                    "JobTrans.Error.ChekingParentLogFolderTitle"),
                    BaseMessages.getString(PKG,
                            "JobTrans.Error.ChekingParentLogFolder",
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
                ;
            }
        }

        return resultat;
    }
}
