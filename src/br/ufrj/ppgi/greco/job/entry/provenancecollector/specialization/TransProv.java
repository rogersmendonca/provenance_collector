package br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.TransStoppedListener;
import org.pentaho.di.trans.step.RunThread;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaDataCombi;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.StepDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.ParentProvenanceListener;

public class TransProv extends Trans
{
    private static Class<?> PKG = Trans.class; // for i18n purposes, needed by
                                               // Translator2!! $NON-NLS-1$

    protected AtomicInteger errors;
    protected Set<Database> connectionPool;
    protected Map<StepInterface, Long> mapStepMetaSeq;
    private long seq;

    private void initialize()
    {
        this.errors = new AtomicInteger(0);
        this.connectionPool = new HashSet<Database>();
        this.mapStepMetaSeq = new HashMap<StepInterface, Long>();
        this.seq = 1;
    }

    public TransProv()
    {
        super();
        initialize();
    }

    public TransProv(TransMeta transMeta)
    {
        super(transMeta);
        initialize();

    }

    public TransProv(TransMeta transMeta, LoggingObjectInterface parent)
    {
        super(transMeta, parent);
        initialize();
    }

    public TransProv(VariableSpace parentVariableSpace, Repository rep,
            String name, String dirname, String filename)
            throws KettleException
    {
        super(parentVariableSpace, rep, name, dirname, filename);
        initialize();
    }

    public void startThreads() throws KettleException
    {
        // Rogers: dispara o metodo transActive dos listeners
        List<TransListener> transListeners = getTransListeners();
        for (TransListener transListener : transListeners)
        {
            transListener.transActive(this);
        }

        // Seta o tipo de transformation como SingleThreaded para que as threads
        // nao sejam iniciadas no metodo startThreads da super classe
        final TransMeta transMeta = getTransMeta();
        TransformationType transType = transMeta.getTransformationType();
        transMeta.setTransformationType(TransformationType.SingleThreaded);

        // Coloca o nivel de log como minimal
        final LogChannelInterface log = getLogChannel();
        LogLevel logLevel = log.getLogLevel();
        log.setLogLevel(LogLevel.MINIMAL);

        // Executa o super-metodo startThreads
        super.startThreads();

        // Retorna os valores originais
        transMeta.setTransformationType(transType);
        log.setLogLevel(logLevel);

        // Rogers: obtem a lista de steps, utilizando o encapsulamento com a
        // classe StepDecorator
        final List<StepMetaDataCombi> steps = getSteps();

        // Rogers: Executa a parte final do super-metodo startThreads,
        // utilizando as classes de proveniencia
        switch (transMeta.getTransformationType())
        {
            case Normal:
                // Now start all the threads...
                //
                for (int i = 0; i < steps.size(); i++)
                {
                    StepMetaDataCombi combi = steps.get(i);

                    RunThread runThread = new RunThread(combi);
                    Thread thread = new Thread(runThread);
                    thread.setName(getName() + " - " + combi.stepname);
                    thread.start();
                }
                break;

            case SerialSingleThreaded:
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            // Always disable thread priority management, it
                            // will always slow us down...
                            //
                            for (StepMetaDataCombi combi : steps)
                            {
                                combi.step
                                        .setUsingThreadPriorityManagment(false);
                            }

                            //
                            // This is a single threaded version...
                            //

                            // Sort the steps from start to finish...
                            //
                            Collections.sort(steps,
                                    new Comparator<StepMetaDataCombi>()
                                    {
                                        public int compare(
                                                StepMetaDataCombi c1,
                                                StepMetaDataCombi c2)
                                        {

                                            boolean c1BeforeC2 = transMeta
                                                    .findPrevious(c2.stepMeta,
                                                            c1.stepMeta);
                                            if (c1BeforeC2)
                                            {
                                                return -1;
                                            }
                                            else
                                            {
                                                return 1;
                                            }
                                        }
                                    });

                            boolean[] stepDone = new boolean[steps.size()];
                            int nrDone = 0;
                            while (nrDone < steps.size() && !isStopped())
                            {
                                for (int i = 0; i < steps.size()
                                        && !isStopped(); i++)
                                {
                                    StepMetaDataCombi combi = steps.get(i);
                                    if (!stepDone[i])
                                    {
                                        // if (combi.step.canProcessOneRow() ||
                                        // !combi.step.isRunning()) {
                                        boolean cont = combi.step.processRow(
                                                combi.meta, combi.data);
                                        if (!cont)
                                        {
                                            stepDone[i] = true;
                                            nrDone++;
                                        }
                                        // }
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            errors.addAndGet(1);
                            log.logError("Error executing single threaded", e);
                        }
                        finally
                        {
                            for (int i = 0; i < steps.size(); i++)
                            {
                                StepMetaDataCombi combi = steps.get(i);
                                combi.step.dispose(combi.meta, combi.data);
                                combi.step.markStop();
                            }
                        }
                    }
                }).start();
                break;

            case SingleThreaded:
                // Don't do anything, this needs to be handled by the
                // transformation executor!
                //
                break;

        }

        if (log.isDetailed())
            log.logDetailed(BaseMessages
                    .getString(
                            PKG,
                            "Trans.Log.TransformationHasAllocated", String.valueOf(steps.size()), String.valueOf(getRowsets().size()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public int getErrors()
    {
        int err = super.getErrors();
        return err + errors.get();
    }

    /**
     * @return Returns the steps.
     */
    @Override
    public List<StepMetaDataCombi> getSteps()
    {
        List<StepMetaDataCombi> originalSteps = super.getSteps();
        List<StepMetaDataCombi> steps = new ArrayList<StepMetaDataCombi>();

        for (int i = 0; i < originalSteps.size(); i++)
        {
            StepMetaDataCombi combi = originalSteps.get(i);

            // Rogers: Enpacota o step com a classe StepDecorator
            combi.step = new StepDecorator(combi.step, this);
            steps.add(combi);
        }
        return steps;
    }

    /**
     * Gerenciamento das conexoes com o Banco de Dados para persistir a
     * proveniencia
     */
    public void addDatabaseInConnectionPool(Database db)
    {
        this.connectionPool.add(db);
    }

    public void removeDatabaseFromConnectionPool(Database db)
    {
        this.connectionPool.remove(db);
    }

    public void disconnectConnectionPool()
    {
        // Desconecta a lista de conexoes com o banco de dados
        if (this.connectionPool != null)
        {
            for (Database db : this.connectionPool)
            {
                if (db != null)
                {
                    db.disconnect();
                }
            }
        }
        this.connectionPool.clear();
    }

    /**
     * Override listener para adicionar conexao no Pool
     */
    public void addTransListener(TransListener transListener)
    {
        synchronized (getTransListeners())
        {
            super.addTransListener(transListener);
            if (transListener instanceof ParentProvenanceListener)
            {
                addDatabaseInConnectionPool(((ParentProvenanceListener) transListener)
                        .getDb());
            }
        }
    }

    public void addTransStoppedListener(
            TransStoppedListener transStoppedListener)
    {
        synchronized (getTransStoppedListeners())
        {
            super.addTransStoppedListener(transStoppedListener);
            if (transStoppedListener instanceof ParentProvenanceListener)
            {
                addDatabaseInConnectionPool(((ParentProvenanceListener) transStoppedListener)
                        .getDb());
            }
        }
    }

    // Rogers: Sequencial do step
    public synchronized long generateStepMetaSeq(StepInterface step)
    {
        this.mapStepMetaSeq.put(step, this.seq++);
        return getStepMetaSeq(step);
    }

    public synchronized long getStepMetaSeq(StepInterface step)
    {
        return this.mapStepMetaSeq.get(step);
    }
}
