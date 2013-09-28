package br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.ParentProvenanceListener;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization.TransProv;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 *
 */
public class StepDecorator implements StepInterface
{
    private StepInterface step;
    private TransProv transProv;

    /** The list of RowListener interfaces to capture provenance */
    private List<RowListener> provRowListeners;

    /** The list of StepListener interfaces to capture provenance */
    private List<StepListener> provStepListeners;

    public StepDecorator(StepInterface step, TransProv trans)
    {
        this.step = step;
        this.transProv = trans;
        this.provRowListeners = new ArrayList<RowListener>();
        this.provStepListeners = new ArrayList<StepListener>();
    }

    /* Metodos especificos (INICIO) */

    public List<RowListener> getProvRowListeners()
    {
        return provRowListeners;
    }

    public void setProvRowListeners(List<RowListener> provRowListeners)
    {
        this.provRowListeners = provRowListeners;
    }

    public boolean addProvRowListener(RowListener rowListener)
    {
        return this.provRowListeners.add(rowListener);
    }

    public boolean removeProvRowListener(RowListener rowListener)
    {
        return this.provRowListeners.remove(rowListener);
    }

    public List<StepListener> getProvStepListeners()
    {
        return provStepListeners;
    }

    public void setProvStepListeners(List<StepListener> provStepListeners)
    {
        this.provStepListeners = provStepListeners;
    }

    public boolean addProvStepListeners(StepListener stepListener)
    {
        boolean retorno = this.provStepListeners.add(stepListener);
        if (stepListener instanceof ParentProvenanceListener)
        {
            this.transProv
                    .addDatabaseInConnectionPool(((ParentProvenanceListener) stepListener)
                            .getDb());
        }
        return retorno;

    }

    public boolean removeProvStepListeners(StepListener stepListener)
    {
        boolean retorno = this.provStepListeners.remove(stepListener);
        if (stepListener instanceof ParentProvenanceListener)
        {
            this.transProv
                    .removeDatabaseFromConnectionPool(((ParentProvenanceListener) stepListener)
                            .getDb());
        }
        return retorno;
    }

    /* Metodos especificos (FIM) */

    /* Metodos decorator incrementados (INICIO) */

    @Override
    public void setRunning(boolean running)
    {
        this.step.setRunning(running);

        // Rogers: Executar o metodo stepActive do provStepListener...
        synchronized (provStepListeners)
        {
            for (int i = 0; i < provStepListeners.size(); i++)
            {
                StepListener stepListener = provStepListeners.get(i);
                stepListener.stepActive(this.getTrans(), this.getStepMeta(),
                        this);
            }
        }
    }

    @Override
    public void setStopped(boolean stopped)
    {
        this.step.setStopped(stopped);
    }

    @Override
    public void pauseRunning()
    {
        this.step.pauseRunning();

        // Rogers: Executar o metodo stepFinished do provStepListener...
        synchronized (provStepListeners)
        {
            for (int i = 0; i < provStepListeners.size(); i++)
            {
                StepListener stepListener = provStepListeners.get(i);
                stepListener
                        .stepIdle(this.getTrans(), this.getStepMeta(), this);
            }
        }
    }

    @Override
    public void resumeRunning()
    {
        this.step.resumeRunning();
    }

    @Override
    public Object[] getRow() throws KettleException
    {
        Object[] data = this.step.getRow();

        if (data != null)
        {
            List<RowSet> rowSets = this.step.getInputRowSets();

            if ((rowSets != null) && (rowSets.size() > 0))
            {
                // Rogers: Executar o metodo stepActive do provRowListeners...
                synchronized (provRowListeners)
                {
                    for (int i = 0; i < provRowListeners.size(); i++)
                    {
                        RowListener rowListener = provRowListeners.get(i);
                        rowListener.rowReadEvent(rowSets.get(0).getRowMeta(),
                                data);
                    }
                }
            }
        }

        return data;
    }

    @Override
    public void putRow(RowMetaInterface row, Object[] data)
            throws KettleException
    {
        this.step.putRow(row, data);

        if (data != null)
        {
            if ((row != null) && (row.size() > 0))
            {
                // Rogers: Executar o metodo stepActive do provRowListeners...
                synchronized (provRowListeners)
                {
                    for (int i = 0; i < provRowListeners.size(); i++)
                    {
                        RowListener rowListener = provRowListeners.get(i);
                        rowListener.rowWrittenEvent(row, data);
                    }
                }
            }
        }
    }

    @Override
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
            throws KettleException
    {
        return this.step.processRow(smi, sdi);
    }

    /* Metodos decorator incrementados (FIM) */

    /* Demais metodos decorator ... */

    public StepInterface getOriginalStep()
    {
        return this.step;
    }

    @Override
    public void initializeVariablesFrom(VariableSpace parent)
    {
        this.step.initializeVariablesFrom(parent);
    }

    @Override
    public void copyVariablesFrom(VariableSpace space)
    {
        this.step.copyVariablesFrom(space);
    }

    @Override
    public void shareVariablesWith(VariableSpace space)
    {
        this.step.shareVariablesWith(space);
    }

    @Override
    public VariableSpace getParentVariableSpace()
    {
        return this.step.getParentVariableSpace();
    }

    @Override
    public void setParentVariableSpace(VariableSpace parent)
    {
        this.step.setParentVariableSpace(parent);
    }

    @Override
    public void setVariable(String variableName, String variableValue)
    {
        this.step.setVariable(variableName, variableValue);
    }

    @Override
    public String getVariable(String variableName, String defaultValue)
    {
        return this.step.getVariable(variableName, defaultValue);
    }

    @Override
    public String getVariable(String variableName)
    {
        return this.step.getVariable(variableName);
    }

    @Override
    public boolean getBooleanValueOfVariable(String variableName,
            boolean defaultValue)
    {
        return this.step.getBooleanValueOfVariable(variableName, defaultValue);
    }

    @Override
    public String[] listVariables()
    {
        return this.step.listVariables();
    }

    @Override
    public String environmentSubstitute(String aString)
    {
        return this.step.environmentSubstitute(aString);
    }

    @Override
    public String[] environmentSubstitute(String[] string)
    {
        return this.step.environmentSubstitute(string);
    }

    @Override
    public void injectVariables(Map<String, String> prop)
    {
        this.step.injectVariables(prop);
    }

    @Override
    // Alterado para retornar o objeto do tipo TransProv
    public Trans getTrans()
    {
        return this.transProv;
    }

    @Override
    public boolean canProcessOneRow()
    {
        return this.step.canProcessOneRow();
    }

    @Override
    public boolean init(StepMetaInterface stepMetaInterface,
            StepDataInterface stepDataInterface)
    {
        return this.step.init(stepMetaInterface, stepDataInterface);
    }

    @Override
    public void dispose(StepMetaInterface sii, StepDataInterface sdi)
    {
        this.step.dispose(sii, sdi);
    }

    @Override
    public void markStart()
    {
        this.step.markStart();
    }

    @Override
    public void markStop()
    {
        this.step.markStop();
        
        // Rogers: Executar o metodo stepFinished do provStepListener...
        synchronized (provStepListeners)
        {
            for (int i = 0; i < provStepListeners.size(); i++)
            {
                StepListener stepListener = provStepListeners.get(i);
                stepListener.stepFinished(this.getTrans(), this.getStepMeta(),
                        this);
            }
        }        

        // Rogers (Fev/2013): Se todos os steps do transformation estiverem
        // parados, fecha as conexoes utilizadas para persistir a proveniencia
        List<StepMetaDataCombi> steps = getTrans().getSteps();
        boolean hasStepRunning = false;
        for (StepMetaDataCombi smdc : steps)
        {
            if (smdc.step.isRunning())
            {
                hasStepRunning = true;
                break;
            }
        }
        if (!hasStepRunning)
        {
            this.transProv.disconnectConnectionPool();
        }
    }

    @Override
    public void stopRunning(StepMetaInterface stepMetaInterface,
            StepDataInterface stepDataInterface) throws KettleException
    {
        this.step.stopRunning(stepMetaInterface, stepDataInterface);
    }

    @Override
    public boolean isRunning()
    {
        return this.step.isRunning();
    }

    @Override
    public boolean isStopped()
    {
        return this.step.isStopped();
    }

    @Override
    public boolean isPaused()
    {
        return this.step.isPaused();
    }

    @Override
    public void stopAll()
    {
        this.step.stopAll();
    }

    @Override
    public String getStepname()
    {
        return this.step.getStepname();
    }

    @Override
    public int getCopy()
    {
        return this.step.getCopy();
    }

    @Override
    public String getStepID()
    {
        return this.step.getStepID();
    }

    @Override
    public long getErrors()
    {
        return this.step.getErrors();
    }

    @Override
    public void setErrors(long errors)
    {
        this.step.setErrors(errors);

    }

    @Override
    public long getLinesInput()
    {
        return this.step.getLinesInput();
    }

    @Override
    public long getLinesOutput()
    {
        return this.step.getLinesOutput();
    }

    @Override
    public long getLinesRead()
    {
        return this.step.getLinesRead();
    }

    @Override
    public long getLinesWritten()
    {
        return this.step.getLinesWritten();
    }

    @Override
    public long getLinesUpdated()
    {
        return this.step.getLinesUpdated();
    }

    @Override
    public void setLinesRejected(long linesRejected)
    {
        this.step.setLinesRejected(linesRejected);
    }

    @Override
    public long getLinesRejected()
    {
        return this.step.getLinesRejected();
    }

    @Override
    public void setOutputDone()
    {
        this.step.setOutputDone();
    }

    @Override
    public void addRowListener(RowListener rowListener)
    {
        this.step.addRowListener(rowListener);
        if (rowListener instanceof ParentProvenanceListener)
        {
            this.transProv
                    .addDatabaseInConnectionPool(((ParentProvenanceListener) rowListener)
                            .getDb());
        }
    }

    @Override
    public void removeRowListener(RowListener rowListener)
    {
        this.step.removeRowListener(rowListener);
        if (rowListener instanceof ParentProvenanceListener)
        {
            this.transProv
                    .removeDatabaseFromConnectionPool(((ParentProvenanceListener) rowListener)
                            .getDb());
        }
    }

    @Override
    public List<RowListener> getRowListeners()
    {
        return this.step.getRowListeners();
    }

    @Override
    public List<RowSet> getInputRowSets()
    {
        return this.step.getInputRowSets();
    }

    @Override
    public List<RowSet> getOutputRowSets()
    {
        return this.step.getOutputRowSets();
    }

    @Override
    public boolean isPartitioned()
    {
        return this.step.isPartitioned();
    }

    @Override
    public void setPartitionID(String partitionID)
    {
        this.step.setPartitionID(partitionID);
    }

    @Override
    public String getPartitionID()
    {
        return this.step.getPartitionID();
    }

    @Override
    public void cleanup()
    {
        this.step.cleanup();
    }

    @Override
    public void initBeforeStart() throws KettleStepException
    {
        this.step.initBeforeStart();
    }

    @Override
    public void addStepListener(StepListener stepListener)
    {
        this.step.addStepListener(stepListener);
    }

    @Override
    public boolean isMapping()
    {
        return this.step.isMapping();
    }

    @Override
    public StepMeta getStepMeta()
    {
        return this.step.getStepMeta();
    }

    @Override
    public LogChannelInterface getLogChannel()
    {
        return this.step.getLogChannel();
    }

    @Override
    public void setUsingThreadPriorityManagment(
            boolean usingThreadPriorityManagment)
    {
        this.step.setUsingThreadPriorityManagment(usingThreadPriorityManagment);
    }

    @Override
    public boolean isUsingThreadPriorityManagment()
    {
        return this.step.isUsingThreadPriorityManagment();
    }

    @Override
    public int rowsetInputSize()
    {
        return this.step.rowsetInputSize();
    }

    @Override
    public int rowsetOutputSize()
    {
        return this.step.rowsetOutputSize();
    }

    @Override
    public long getProcessed()
    {
        return this.step.getProcessed();
    }

    @Override
    public Map<String, ResultFile> getResultFiles()
    {
        return this.step.getResultFiles();
    }

    @Override
    public StepExecutionStatus getStatus()
    {
        return this.step.getStatus();
    }

    @Override
    public long getRuntime()
    {
        return this.step.getRuntime();
    }

    @Override
    public void identifyErrorOutput()
    {
        this.step.identifyErrorOutput();
    }

    @Override
    public void setPartitioned(boolean partitioned)
    {
        this.step.setPartitioned(partitioned);
    }

    @Override
    public void setRepartitioning(int partitioningMethod)
    {
        this.step.setRepartitioning(partitioningMethod);
    }

    @Override
    public void batchComplete() throws KettleException
    {
        this.step.batchComplete();
    }
}
