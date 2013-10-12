package br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.Log4jBufferAppender;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingHierarchy;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.www.SocketRepository;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.ParentProspStepParamCmd;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.FineGrainedStep;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.FineGrainedStepMap;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.IRetrospJobListener;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.ParentProvenanceListener;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 * 
 */
public class JobDecorator extends Job
{
    // for i18n purposes, needed by Translator2!! $NON-NLS-1$
    private static Class<?> PKG = Job.class;

    protected AtomicBoolean initialized2;
    protected AtomicBoolean active2;
    protected int maxJobEntriesLogged;

    protected Job job;

    protected Map<JobMeta, Long> prospJobMetaMap;
    protected Map<TransMeta, Long> prospTransMetaMap;
    protected Map<JobMeta, Map<JobEntryCopy, Long>> prospJobEntryMetaMap;
    protected Map<TransMeta, Map<StepMeta, Long>> prospStepMetaMap;
    protected List<IRetrospJobListener> retrospJobListeners;
    protected Database db;
    protected Set<Database> connectionPool;
    protected RepositoryMeta repoMeta;
    protected String repoLoc;
    protected long prospJobId;
    protected long prospRepoId;
    protected Map<FineGrainedStep, Boolean> mapFineGrainedEnabled;

    public JobDecorator(Job job, Database db,
            Map<FineGrainedStep, Boolean> mapFineGrainedEnabled)
            throws KettleException
    {
        this.initialized2 = new AtomicBoolean(false);
        this.active2 = new AtomicBoolean(false);
        this.maxJobEntriesLogged = Const.toInt(
                EnvUtil.getSystemProperty(Const.KETTLE_MAX_JOB_ENTRIES_LOGGED),
                1000);

        this.job = job;
        this.prospJobMetaMap = new HashMap<JobMeta, Long>();
        this.prospTransMetaMap = new HashMap<TransMeta, Long>();
        this.prospJobEntryMetaMap = new HashMap<JobMeta, Map<JobEntryCopy, Long>>();
        this.prospStepMetaMap = new HashMap<TransMeta, Map<StepMeta, Long>>();
        this.retrospJobListeners = new ArrayList<IRetrospJobListener>();
        this.db = db;
        this.mapFineGrainedEnabled = mapFineGrainedEnabled;
        this.connectionPool = new HashSet<Database>();
        addDatabaseInConnectionPool(this.db);
        setRepoMetaAndLocation(this.db);
        setProspIdRepository(this.db, getRepoMeta().getName(), getRepoLoc());

        collectProspectiveProvenance(db, null, this.getJobMeta());
    }

    // Seta o valor dos atributos repoMeta e repoLoc
    private void setRepoMetaAndLocation(Database db) throws KettleException
    {
        this.repoMeta = this.getRep().getRepositoryMeta();
        this.repoLoc = null;
        if (this.repoMeta instanceof KettleFileRepositoryMeta)
        {
            this.repoLoc = ((KettleFileRepositoryMeta) this.repoMeta)
                    .getBaseDirectory();
        }
        else if (this.repoMeta instanceof KettleDatabaseRepositoryMeta)
        {
            try
            {
                this.repoLoc = ((KettleDatabaseRepositoryMeta) this.repoMeta)
                        .getConnection().getURL();
            }
            catch (KettleDatabaseException e)
            {
                throw new KettleException(e.toString());
            }
        }
        else
        {
            throw new KettleException("Kettle Repository Type not supported.");
        }
    }

    protected long getProspIdRepository(Database db, String repoName,
            String repoLoc) throws KettleDatabaseException
    {
        StringBuilder SQL = new StringBuilder();
        SQL.append("SELECT t1.id_repository ");
        SQL.append("FROM prosp_repository t1 ");
        SQL.append("WHERE t1.name = ? ");
        SQL.append("AND   t1.location = ? ");

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("name",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("location",
                ValueMetaInterface.TYPE_STRING));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = repoName;
        data[i++] = repoLoc;

        long id = -1;
        ResultSet res = db.openQuery(SQL.toString(), fields, data);
        try
        {
            id = res.next() ? res.getLong("id_repository") : 0;
        }
        catch (SQLException e)
        {
            throw new KettleDatabaseException(e.getMessage());
        }
        finally
        {
            db.closeQuery(res);
        }

        return id;
    }

    protected void setProspIdRepository(Database db, String repoName,
            String repoLoc) throws KettleException
    {
        this.prospRepoId = -1;
        long id = getProspIdRepository(db, repoName, repoLoc);

        // Se existir, seta o id. Senao, insere repositorio e seta o id.
        if (id > 0)
        {
            this.prospRepoId = id;
        }
        else
        {
            String tableName = "prosp_repository";
            id = generateId(db, tableName);

            RowMetaInterface fields = new RowMeta();
            fields.addValueMeta(new ValueMeta("id_repository",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("name",
                    ValueMetaInterface.TYPE_STRING));
            fields.addValueMeta(new ValueMeta("location",
                    ValueMetaInterface.TYPE_STRING));

            Object[] data = new Object[fields.size()];
            int i = 0;
            data[i++] = id;
            data[i++] = getRepoMeta().getName();
            data[i++] = getRepoLoc();

            db.insertRow(tableName, fields, data);
            this.prospRepoId = id;
        }
    }

    /**
     * Captura a proveniencia prospectiva
     */
    protected void collectProspectiveProvenance(Database db,
            LoggingObjectInterface parentMeta,
            LoggingObjectInterface processMeta) throws KettleException
    {
        try
        {
            // GET PROSPECTIVE PROVENANCE ID DO PROCESSO
            long prospProcessId = getProspIdProcess(db, parentMeta,
                    processMeta, true);

            if (prospProcessId > 0)
            {
                // ARMAZENA O ID DO JOB ROOT
                if ((processMeta instanceof JobMeta)
                        && ((JobMeta) processMeta).equals(this.getJobMeta()))
                {
                    this.prospJobId = prospProcessId;
                }

                // POPULA PROCESS MAP
                populateProspProcessMap(processMeta, prospProcessId);

                // POPULA STEP MAP
                populateProspStepMap(db, prospProcessId, processMeta);
            }
            else
            {
                // ARMAZENA ID ANTERIOR PARA USAR NO NOTE
                long previousProspProcessId = getProspIdProcess(db, parentMeta,
                        processMeta, false);

                // PROCESS (JOB ou TRANSFORMATION)
                prospProcessId = insertProspProcess(db, parentMeta, processMeta);

                // NOTE
                insertProspNote(db, previousProspProcessId, processMeta);

                // PASSOS DO PROCESSO
                if (processMeta instanceof JobMeta)
                {
                    // JOB ENTRIES e JOB HOPS
                    List<JobHopMeta> jobHops = ((JobMeta) processMeta)
                            .getJobhops();
                    for (JobHopMeta hop : jobHops)
                    {
                        // FROM
                        JobEntryCopy from = hop.getFromEntry();
                        insertProspHopStep(db, from, prospProcessId);

                        // TO
                        JobEntryCopy to = hop.getToEntry();
                        insertProspHopStep(db, to, prospProcessId);

                        // JOB HOP
                        insertProspHop(db, hop, from, to, prospProcessId);
                    }
                }
                else if (processMeta instanceof TransMeta)
                {
                    // KETTLE STEPS e TRANS HOPS
                    int totalHops = ((TransMeta) processMeta).nrTransHops();
                    for (int t = 0; t < totalHops; t++)
                    {
                        TransHopMeta hop = ((TransMeta) processMeta)
                                .getTransHop(t);

                        // FROM
                        StepMeta from = hop.getFromStep();
                        insertProspHopStep(db, from, prospProcessId);

                        // TO
                        StepMeta to = hop.getToStep();
                        insertProspHopStep(db, to, prospProcessId);

                        // JOB HOP
                        insertProspHop(db, hop, from, to, prospProcessId);
                    }
                }
                else
                {
                    throw new KettleException(
                            "The process is not a kettle job or kettle transformation");
                }
            }

            collectSubProcessProvenance(db, processMeta);

            if (processMeta.equals(this.getJobMeta())
                    && (!db.getConnection().getAutoCommit()))
            {
                db.commit(true);
            }
        }
        catch (KettleDatabaseException e)
        {
            throw new KettleException(e.toString());
        }
        catch (SQLException e)
        {
            throw new KettleException(e.toString());
        }
        catch (KettleException e)
        {
            db.rollback();
            throw new KettleException(e.toString());
        }
    }

    protected void collectSubProcessProvenance(Database db,
            LoggingObjectInterface parentMeta) throws KettleException
    {
        if (parentMeta instanceof JobMeta)
        {
            List<JobEntryCopy> jobEntryList = ((JobMeta) parentMeta)
                    .getJobCopies();
            for (JobEntryCopy jec : jobEntryList)
            {
                if (jec.getEntry() instanceof JobEntryJob)
                {
                    JobMeta jobMeta = ((JobEntryJob) jec.getEntry())
                            .getJobMeta(this.getRep(), null);
                    collectProspectiveProvenance(db, parentMeta, jobMeta);
                }
                else if (jec.getEntry() instanceof JobEntryTrans)
                {
                    TransMeta transMeta = ((JobEntryTrans) jec.getEntry())
                            .getTransMeta(this.getRep(), null);
                    collectProspectiveProvenance(db, parentMeta, transMeta);
                }
            }
        }
    }

    /* METODOS GET ID */
    public long generateId(Database db, String tableName)
            throws KettleException
    {
        return generateId(db, tableName, null);
    }

    public long generateId(Database db, String tableName,
            Map<String, Long> restriction) throws KettleException
    {
        synchronized (db)
        {
            try
            {
                StringBuilder SQL = new StringBuilder(String.format(
                        "SELECT COUNT(*) + 1 AS id FROM %s ", tableName));
                if (restriction != null && restriction.size() > 0)
                {
                    boolean where = true;
                    for (Map.Entry<String, Long> entry : restriction.entrySet())
                    {
                        if (where)
                        {
                            SQL.append(String.format(" WHERE %s = %d ",
                                    entry.getKey(), entry.getValue()));
                            where = false;
                        }
                        else
                        {
                            SQL.append(String.format(" AND %s = %d ",
                                    entry.getKey(), entry.getValue()));
                        }
                    }
                }
                ResultSet res = db.openQuery(SQL.toString());
                long id = res.next() ? res.getLong("id") : 0;
                db.closeQuery(res);
                return id;
            }
            catch (SQLException e)
            {
                throw new KettleException(e.toString());
            }
        }
    }

    protected long getProspIdProcess(Database db,
            LoggingObjectInterface parentMeta,
            LoggingObjectInterface processMeta, boolean filterByModifiedDate)
            throws KettleException, SQLException
    {
        StringBuilder SQL = new StringBuilder();
        SQL.append("SELECT t1.id_process as id ");
        SQL.append("FROM  prosp_process t1 ");
        SQL.append("WHERE t1.id_repository = ? ");
        SQL.append("AND   t1.object_id = ? ");
        if (parentMeta != null)
        {
            SQL.append("AND   t1.id_parent_repository = ? ");
            SQL.append("AND   t1.id_parent = ? ");
        }
        if (filterByModifiedDate)
        {
            SQL.append("AND   t1.modified_date = ? ");
        }
        SQL.append("ORDER BY t1.id_process DESC");

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("object_id",
                ValueMetaInterface.TYPE_STRING));
        if (parentMeta != null)
        {
            fields.addValueMeta(new ValueMeta("id_parent_repository",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_parent",
                    ValueMetaInterface.TYPE_INTEGER));
        }
        if (filterByModifiedDate)
        {
            fields.addValueMeta(new ValueMeta("modified_date",
                    ValueMetaInterface.TYPE_DATE));
        }

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = getProspRepoId();
        data[i++] = processMeta.getObjectId();
        if (parentMeta != null)
        {
            data[i++] = getProspRepoId();
            data[i++] = getProspProcessId(parentMeta);
        }
        if (filterByModifiedDate)
        {
            if (processMeta instanceof JobMeta)
            {
                data[i++] = ((JobMeta) processMeta).getModifiedDate();
            }
            else if (processMeta instanceof TransMeta)
            {
                data[i++] = ((TransMeta) processMeta).getModifiedDate();
            }
            else
            {
                throw new KettleException(
                        "The process is not a kettle job or a kettle transformation");
            }
        }

        ResultSet res = db.openQuery(SQL.toString(), fields, data);
        long id = res.next() ? res.getLong("id") : 0;
        db.closeQuery(res);

        return id;
    }

    protected long getProspIdNote(Database db, long processId,
            NotePadMeta notePad) throws KettleDatabaseException, SQLException
    {
        if (processId > 0)
        {
            StringBuilder SQL = new StringBuilder();
            SQL.append("SELECT t1.id_note as id ");
            SQL.append("FROM prosp_note t1, prosp_process_note t2 ");
            SQL.append("WHERE t1.id_note = t2.id_note ");
            SQL.append("AND t2.id_process = ? ");
            SQL.append("AND t1.text = ? ");

            RowMetaInterface fields = new RowMeta();
            fields.addValueMeta(new ValueMeta("id_process",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("text",
                    ValueMetaInterface.TYPE_STRING));

            Object[] data = new Object[fields.size()];
            int i = 0;
            data[i++] = processId;
            data[i++] = notePad.getNote();

            ResultSet res = db.openQuery(SQL.toString(), fields, data);
            long id = res.next() ? res.getLong("id") : 0;
            db.closeQuery(res);

            return id;
        }
        else
        {
            return -1;
        }
    }

    protected long insertProspProcess(Database db,
            LoggingObjectInterface parentMeta,
            LoggingObjectInterface processMeta) throws KettleException
    {
        String tableName = "prosp_process";
        HashMap<String, Long> restriction = new HashMap<String, Long>();
        restriction.put("id_repository", this.prospRepoId);
        long prospProcessId = generateId(db, tableName);

        if ((processMeta instanceof JobMeta)
                && ((JobMeta) processMeta).equals(this.getJobMeta()))
        {
            this.prospJobId = prospProcessId;
        }

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("object_id",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("name",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("description",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("created_user",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("created_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("modified_user",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("modified_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("id_root_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_root",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_parent_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_parent",
                ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        if (processMeta instanceof JobMeta)
        {
            data[i++] = getProspRepoId();
            data[i++] = prospProcessId;
            data[i++] = ((JobMeta) processMeta).getObjectId();
            data[i++] = ((JobMeta) processMeta).getName();
            data[i++] = ((JobMeta) processMeta).getDescription();
            data[i++] = ((JobMeta) processMeta).getCreatedUser();
            data[i++] = ((JobMeta) processMeta).getCreatedDate();
            data[i++] = ((JobMeta) processMeta).getModifiedUser();
            data[i++] = ((JobMeta) processMeta).getModifiedDate();
            data[i++] = getProspRepoId();
            data[i++] = getProspJobId();

            // Parent Process
            Long parentProcessId = this.getProspProcessId(parentMeta);
            if (parentProcessId != null)
            {
                data[i++] = getProspRepoId();
                data[i++] = parentProcessId;
            }
        }
        else if (processMeta instanceof TransMeta)
        {
            data[i++] = getProspRepoId();
            data[i++] = prospProcessId;
            data[i++] = ((TransMeta) processMeta).getObjectId();
            data[i++] = ((TransMeta) processMeta).getName();
            data[i++] = ((TransMeta) processMeta).getDescription();
            data[i++] = ((TransMeta) processMeta).getCreatedUser();
            data[i++] = ((TransMeta) processMeta).getCreatedDate();
            data[i++] = ((TransMeta) processMeta).getModifiedUser();
            data[i++] = ((TransMeta) processMeta).getModifiedDate();
            data[i++] = getProspRepoId();
            data[i++] = getProspJobId();

            // Parent Process
            Long parentProcessId = this.getProspProcessId(parentMeta);
            if (parentProcessId != null)
            {
                data[i++] = getProspRepoId();
                data[i++] = parentProcessId;
            }
        }
        else
        {
            throw new KettleException(
                    "The process is not a kettle job or kettle transformation");
        }
        db.insertRow(tableName, fields, data);

        // POPULA PROCESS MAP
        populateProspProcessMap(processMeta, prospProcessId);

        return prospProcessId;
    }

    protected long insertProspHop(Database db, Object hop, Object from,
            Object to, long prospProcessId) throws KettleException
    {
        String tableName = "prosp_hop";
        long prospHopId = 0;

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_step_from",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_step_to",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("enabled",
                ValueMetaInterface.TYPE_BOOLEAN));
        fields.addValueMeta(new ValueMeta("evaluation",
                ValueMetaInterface.TYPE_BOOLEAN));
        fields.addValueMeta(new ValueMeta("unconditional",
                ValueMetaInterface.TYPE_BOOLEAN));

        Object[] data = new Object[fields.size()];
        int i = 0;
        if (hop instanceof JobHopMeta)
        {
            data[i++] = this.prospRepoId;
            data[i++] = prospProcessId;
            data[i++] = getProspStepId(from);
            data[i++] = getProspStepId(to);
            data[i++] = ((JobHopMeta) hop).isEnabled();
            data[i++] = ((JobHopMeta) hop).getEvaluation();
            data[i++] = ((JobHopMeta) hop).isUnconditional();
        }
        else if (hop instanceof TransHopMeta)
        {

            data[i++] = this.prospRepoId;
            data[i++] = prospProcessId;
            data[i++] = getProspStepId(from);
            data[i++] = getProspStepId(to);
            data[i++] = ((TransHopMeta) hop).isEnabled();
            data[i++] = true;
            data[i++] = true;
        }
        else
        {
            throw new KettleException(
                    "The hop is not a Kettle Job Hop nor a Kettle Transformation Hop.");
        }

        db.insertRow(tableName, fields, data);
        return prospHopId;
    }

    protected void insertProspNote(Database db, long previousProcessId,
            LoggingObjectInterface processMeta) throws KettleException
    {
        List<NotePadMeta> lstNotePad = new ArrayList<NotePadMeta>();
        if (processMeta instanceof JobMeta)
        {
            lstNotePad.addAll(((JobMeta) processMeta).getNotes());
        }
        else if (processMeta instanceof TransMeta)
        {
            int totalNotes = ((TransMeta) processMeta).nrNotes();
            for (int t = 0; t < totalNotes; t++)
            {
                lstNotePad.add(((TransMeta) processMeta).getNote(t));
            }
        }
        else
        {
            throw new KettleException(
                    "The process is not a kettle job or kettle transformation");
        }

        if (lstNotePad.size() > 0)
        {
            String tableName = "prosp_process_note";

            RowMetaInterface fields = new RowMeta();
            fields.addValueMeta(new ValueMeta("id_repository",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_process",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_note",
                    ValueMetaInterface.TYPE_INTEGER));

            String tableName2 = "prosp_note";

            RowMetaInterface fields2 = new RowMeta();
            fields2.addValueMeta(new ValueMeta("id_note",
                    ValueMetaInterface.TYPE_INTEGER));
            fields2.addValueMeta(new ValueMeta("text",
                    ValueMetaInterface.TYPE_STRING));

            Object[] data = null;
            int i = 0;
            for (NotePadMeta notePad : lstNotePad)
            {
                long prospNoteId = -1;
                try
                {
                    prospNoteId = (previousProcessId > 0) ? getProspIdNote(db,
                            previousProcessId, notePad) : 0;
                }
                catch (SQLException e)
                {
                    throw new KettleException(e.toString());
                }
                if (prospNoteId <= 0)
                {
                    prospNoteId = generateId(db, tableName2);
                    data = new Object[fields2.size()];
                    i = 0;
                    data[i++] = prospNoteId;
                    data[i++] = notePad.getNote();

                    db.insertRow(tableName2, fields2, data);
                }
                data = new Object[fields.size()];
                i = 0;
                data[i++] = this.prospRepoId;
                data[i++] = this.getProspProcessId(processMeta);
                data[i++] = prospNoteId;

                db.insertRow(tableName, fields, data);
            }
        }
    }

    protected void insertProspHopStep(Database db, Object step,
            long prospProcessId) throws KettleException
    {
        long prospStepId = -1;
        try
        {
            if (step instanceof JobEntryCopy)
            {
                JobEntryCopy jobEntry = (JobEntryCopy) step;
                if (!prospJobEntryMetaMap.get(jobEntry.getParentJobMeta())
                        .keySet().contains(step))
                {
                    // INSERE STEP NO BANCO DE DADOS
                    prospStepId = insertProspStep(db, step, prospProcessId);

                    // INSERE STEP NO MAPEAMENTO
                    prospJobEntryMetaMap.get(jobEntry.getParentJobMeta()).put(
                            jobEntry, prospStepId);
                }
            }
            else if (step instanceof StepMeta)
            {
                StepMeta stepMeta = (StepMeta) step;
                if (!prospStepMetaMap.get(stepMeta.getParentTransMeta())
                        .keySet().contains(step))
                {
                    // INSERE STEP NO BANCO DE DADOS
                    prospStepId = insertProspStep(db, step, prospProcessId);

                    // INSERE STEP NO MAPEAMENTO
                    prospStepMetaMap.get(stepMeta.getParentTransMeta()).put(
                            stepMeta, prospStepId);

                    // Rogers (30/09/2013)
                    ParentProspStepParamCmd.get((StepMeta) step).execute(this,
                            db, step, prospProcessId);
                }
            }
            else
            {
                throw new KettleException(
                        "The step object is not a kettle job entry copy or a kettle step.");
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new KettleException(e.toString());
        }
    }

    protected long insertProspStep(Database db, Object step, long processId)
            throws KettleException
    {
        String tableName = "prosp_step";
        HashMap<String, Long> restriction = new HashMap<String, Long>();
        restriction.put("id_repository", this.prospRepoId);
        restriction.put("id_process", processId);
        long stepId = generateId(db, tableName, restriction);

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_step",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("type",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("name",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("description",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("nr", ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = this.prospRepoId;
        data[i++] = processId;
        data[i++] = stepId;
        if (step instanceof JobEntryCopy)
        {
            data[i++] = ((JobEntryCopy) step).getEntry().getPluginId();
            data[i++] = ((JobEntryCopy) step).getEntry().getName();
            data[i++] = ((JobEntryCopy) step).getEntry().getDescription();
            data[i++] = new Long((long) ((JobEntryCopy) step).getNr());
        }
        else if (step instanceof StepMeta)
        {
            data[i++] = ((StepMeta) step).getTypeId();
            data[i++] = ((StepMeta) step).getName();
            data[i++] = ((StepMeta) step).getDescription();
            data[i++] = new Long(0);
        }
        else
        {
            throw new KettleException(
                    "The step object is not a kettle job entry copy or a kettle step.");
        }

        db.insertRow(tableName, fields, data);
        return stepId;
    }

    protected void insertProspStepParam(Database db, Object step,
            long processId, String fieldName, String fieldValue)
            throws KettleException
    {
        String tableName = "prosp_step_parameter";
        Long stepId = getProspStepId(step);
        HashMap<String, Long> restriction = new HashMap<String, Long>();
        restriction.put("id_repository", this.prospRepoId);
        restriction.put("id_process", processId);
        restriction.put("id_step", stepId);
        Long paramId = generateId(db, tableName, restriction);

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_step",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_step_param",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("name",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("value",
                ValueMetaInterface.TYPE_STRING));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = this.prospRepoId;
        data[i++] = processId;
        data[i++] = stepId;
        data[i++] = paramId;
        data[i++] = fieldName;
        data[i++] = fieldValue;

        db.insertRow(tableName, fields, data);
    }

    protected void populateProspProcessMap(LoggingObjectInterface processMeta,
            Long prospProcessId) throws KettleException
    {
        if (processMeta instanceof JobMeta)
        {
            JobMeta jobMeta = (JobMeta) processMeta;
            this.prospJobMetaMap.put(jobMeta, prospProcessId);
            this.prospJobEntryMetaMap.put(jobMeta,
                    new HashMap<JobEntryCopy, Long>());
        }
        else if (processMeta instanceof TransMeta)
        {
            TransMeta transMeta = (TransMeta) processMeta;
            this.prospTransMetaMap.put(transMeta, prospProcessId);
            this.prospStepMetaMap.put(transMeta, new HashMap<StepMeta, Long>());
        }
        else
        {
            throw new KettleException(
                    "The process is not a kettle job or kettle transformation");
        }
    }

    protected void populateProspStepMap(Database db, long processId,
            Object processMeta) throws KettleException
    {
        // Popula o mapeamento de Steps (JOB ENTRY COPY ou STEP)
        StringBuilder SQL = new StringBuilder();
        SQL.append("SELECT t1.id_step as id, t1.name, t1.nr ");
        SQL.append("FROM  prosp_step t1 ");
        SQL.append("WHERE t1.id_repository = ? ");
        SQL.append("AND   id_process = ? ");

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = this.prospRepoId;
        data[i++] = processId;

        ResultSet res = db.openQuery(SQL.toString(), fields, data);
        try
        {
            if (processMeta instanceof JobMeta)
            {
                JobMeta jobMeta = (JobMeta) processMeta;
                Map<JobEntryCopy, Long> jobEntryMap = new HashMap<JobEntryCopy, Long>();
                while (res.next())
                {
                    long prospStepId = res.getLong("id");
                    String name = res.getString("name");
                    int nr = res.getInt("nr");
                    JobEntryCopy jec = jobMeta.findJobEntry(name, nr, true);
                    if (jec != null)
                    {
                        jobEntryMap.put(jec, prospStepId);
                    }
                }
                prospJobEntryMetaMap.put(jobMeta, jobEntryMap);
            }
            else if (processMeta instanceof TransMeta)
            {
                TransMeta transMeta = (TransMeta) processMeta;
                Map<StepMeta, Long> stepMap = new HashMap<StepMeta, Long>();
                while (res.next())
                {
                    long prospStepId = res.getLong("id");
                    String name = res.getString("name");
                    StepMeta sm = transMeta.findStep(name);
                    if (sm != null)
                    {
                        stepMap.put(sm, prospStepId);
                    }
                }
                prospStepMetaMap.put(transMeta, stepMap);
            }
            else
            {
                throw new KettleException(
                        "The process is not a kettle job or kettle transformation");
            }
        }
        catch (SQLException e)
        {
            throw new KettleException(e.toString());
        }
        db.closeQuery(res);
    }

    // Inner Class que agrupa o nome e os valores de um field
    // (prosp_jobentry_attr)
    protected static class FieldNameAndValues
    {
        public String name;
        public String type;
        public String value;
        public Long index;

        public FieldNameAndValues(String name, String type, String value,
                Long index)
        {
            // this.type = ((index != null) && (type != null)) ? type + "[]" :
            // type;
            this.name = name;
            this.type = type;
            this.value = value;
            this.index = index;
        }

        public FieldNameAndValues(String name, String type, String value)
        {
            this(name, type, value, null);
        }

        public FieldNameAndValues(String name)
        {
            this(name, null, null);
        }
    }

    protected static List<FieldNameAndValues> getFieldNamesAndValues(
            final Object bean) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException
    {
        Class<?> c1 = bean.getClass();
        List<FieldNameAndValues> fields = new ArrayList<FieldNameAndValues>();

        Field[] valueBeanFields = c1.getDeclaredFields();

        for (int i = 0; i < valueBeanFields.length; i++)
        {
            valueBeanFields[i].setAccessible(true);
            String fieldName = valueBeanFields[i].getName();
            Object fieldValue = valueBeanFields[i].get(bean);

            if (fieldValue != null)
            {
                if (fieldValue.getClass().isArray())
                {
                    String[] arrayProp = (String[]) fieldValue;
                    for (int j = 0; j < arrayProp.length; j++)
                    {
                        if (arrayProp[j] != null)
                        {
                            fields.add(new FieldNameAndValues(fieldName,
                                    arrayProp[j].getClass().getName(),
                                    arrayProp[j], new Long(j)));
                        }
                    }
                }
                else
                {
                    fields.add(new FieldNameAndValues(fieldName, fieldValue
                            .getClass().getName(), String.valueOf(fieldValue)));
                }

            }
            else
            {
                fields.add(new FieldNameAndValues(fieldName));
            }
        }

        return fields;
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
        // Seta os flags de inicio da execucao
        setFinished(false);
        active2.set(true);
        initialized2.set(true);

        // Perhaps there is already a list of input rows available?
        if (getSourceRows() != null)
        {
            result.setRows(getSourceRows());
        }

        // Where do we start?
        JobEntryCopy startpoint = getJobMeta().findJobEntry(
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
    protected Result execute(final int nr, Result prev_result,
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
                    (JobEntryTrans) jobEntryCopy.getEntry(), this));
        }

        // What entry is next?
        JobEntryInterface jobEntryInterface = jobEntryCopy.getEntry();
        jobEntryInterface.getLogChannel().setLogLevel(getLogLevel());

        // Track the fact that we are going to launch the next job entry...
        JobEntryResult jerBefore = new JobEntryResult(null, null,
                BaseMessages.getString(PKG, "Job.Comment.JobStarted"), reason,
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
                getActiveJobEntryTransformations().put(jobEntryCopy,
                        ((JobEntryTransDecorator) cloneJei).getJobEntryTrans());
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
                "Job.Comment.JobFinished"), null, jobEntryCopy.getName(),
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
                if (getLogChannel().isBasic())
                    getLogChannel().logBasic(
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
                                getLogChannel().logError(
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
                    if (getLogChannel().isBasic())
                        getLogChannel().logBasic(
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
                        getLogChannel().logError(Const.getStackTracker(e));
                        throw new KettleException(
                                BaseMessages.getString(PKG,
                                        "Job.Log.UnexpectedError",
                                        nextEntry.toString()), e);
                    }
                    if (getLogChannel().isBasic())
                        getLogChannel().logBasic(
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
                    getLogChannel()
                            .logError(
                                    getJobMeta().toString(),
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

    /**
     * Metodos das interfaces implementadas pela classe Job
     */

    @Override
    public String getContainerObjectId()
    {
        if (this.job != null)
        {
            return this.job.getContainerObjectId();
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getFilename()
    {
        if (this.job == null)
        {
            return null;
        }
        else
        {
            return this.job.getFilename();
        }
    }

    @Override
    public String getLogChannelId()
    {
        return this.job.getLogChannelId();
    }

    @Override
    public LogLevel getLogLevel()
    {
        if (this.job != null)
        {
            return this.job.getLogLevel();
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
        if (this.job != null)
        {
            return this.job.getObjectId();
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getObjectName()
    {
        if (this.job != null)
        {
            return this.job.getJobname();
        }
        else
        {
            return null;
        }
    }

    @Override
    public ObjectRevision getObjectRevision()
    {
        if (this.job != null)
        {
            return this.job.getObjectRevision();
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
        if (this.job != null)
        {
            return this.job.getParent();
        }
        else
        {
            return null;
        }
    }

    @Override
    public Date getRegistrationDate()
    {
        return this.job.getRegistrationDate();
    }

    @Override
    public RepositoryDirectoryInterface getRepositoryDirectory()
    {
        if (this.job == null)
            return null;
        return this.job.getRepositoryDirectory();
    }

    @Override
    public LogChannelInterface getLogChannel()
    {
        return this.job.getLogChannel();
    }

    @Override
    public void activateParameters()
    {
        this.job.activateParameters();
    }

    @Override
    public void addParameterDefinition(String key, String defValue,
            String description) throws DuplicateParamException
    {
        this.job.addParameterDefinition(key, defValue, description);
    }

    @Override
    public void clearParameters()
    {
        this.job.clearParameters();
    }

    @Override
    public void copyParametersFrom(NamedParams params)
    {
        this.job.copyParametersFrom(params);
    }

    @Override
    public void eraseParameters()
    {
        this.job.eraseParameters();
    }

    @Override
    public String getParameterDefault(String key) throws UnknownParamException
    {
        return this.job.getParameterDefault(key);
    }

    @Override
    public String getParameterDescription(String key)
            throws UnknownParamException
    {
        return this.job.getParameterDescription(key);
    }

    @Override
    public String getParameterValue(String key) throws UnknownParamException
    {
        return this.job.getParameterValue(key);
    }

    @Override
    public String[] listParameters()
    {
        return this.job.listParameters();
    }

    @Override
    public void setParameterValue(String key, String value)
            throws UnknownParamException
    {
        this.job.setParameterValue(key, value);
    }

    @Override
    public void copyVariablesFrom(VariableSpace space)
    {
        this.job.copyVariablesFrom(space);
    }

    @Override
    public String environmentSubstitute(String aString)
    {
        return this.job.environmentSubstitute(aString);
    }

    @Override
    public String[] environmentSubstitute(String[] aString)
    {
        return this.job.environmentSubstitute(aString);
    }

    @Override
    public boolean getBooleanValueOfVariable(String variableName,
            boolean defaultValue)
    {
        return this.job.getBooleanValueOfVariable(variableName, defaultValue);
    }

    @Override
    public VariableSpace getParentVariableSpace()
    {
        return this.job.getParentVariableSpace();
    }

    @Override
    public String getVariable(String variableName)
    {
        return this.job.getVariable(variableName);
    }

    @Override
    public String getVariable(String variableName, String defaultValue)
    {
        return this.job.getVariable(variableName, defaultValue);
    }

    @Override
    public void initializeVariablesFrom(VariableSpace parent)
    {
        this.job.initializeVariablesFrom(parent);
    }

    @Override
    public void injectVariables(Map<String, String> prop)
    {
        this.job.injectVariables(prop);
    }

    @Override
    public String[] listVariables()
    {
        return this.job.listVariables();
    }

    @Override
    public void setParentVariableSpace(VariableSpace parent)
    {
        this.job.setParentVariableSpace(parent);
    }

    @Override
    public void setVariable(String variableName, String variableValue)
    {
        this.job.setVariable(variableName, variableValue);
    }

    @Override
    public void shareVariablesWith(VariableSpace space)
    {
        this.job.shareVariablesWith(space);
    }

    /**
     * Metodos especificos da classe Job
     */

    public void init()
    {
        if (this.job != null)
        {
            this.job.init();
        }
    }

    @Override
    public String toString()
    {
        return this.job.toString();
    }

    public String getJobname()
    {
        if (this.job != null)
        {
            return this.job.getJobname();
        }
        else
        {
            return null;
        }
    }

    public void setRepository(Repository rep)
    {
        this.job.setRepository(rep);
    }

    // Threads main loop: called by Thread.start();
    public void run()
    {
        this.job.run();
    }

    /**
     * Sets the finished flag.<b> Then launch all the this.job listeners and
     * call the jobFinished method for each.<br>
     * 
     * @see JobListener#jobFinished(Job)
     */
    public void fireJobListeners() throws KettleException
    {
        this.job.fireJobListeners();
    }

    /**
     * Wait until this this.job has finished.
     */
    public void waitUntilFinished()
    {
        this.job.waitUntilFinished();
    }

    /**
     * Wait until this this.job has finished.
     * 
     * @param maxMiliseconds
     *            the maximum number of ms to wait
     */
    public void waitUntilFinished(long maxMiliseconds)
    {
        this.job.waitUntilFinished(maxMiliseconds);
    }

    /**
     * Get the number of errors that happened in the this.job.
     * 
     * @return nr of error that have occurred during execution. During execution
     *         of a this.job the number can change.
     */
    public int getErrors()
    {
        return this.job.getErrors();
    }

    /**
     * Set the number of occured errors to 0.
     */
    public void resetErrors()
    {
        this.job.resetErrors();
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
        this.job.addErrors(nrToAdd);
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
        return this.job.beginProcessing();
    }

    public boolean isActive()
    {
        return this.active2.get() ? this.active2.get() : this.job.isActive();
    }

    // Stop all activity!
    public void stopAll()
    {
        this.job.stopAll();
    }

    public void setStopped(boolean stopped)
    {
        this.job.setStopped(stopped);
    }

    /**
     * @return Returns the stopped status of this Job...
     */
    public boolean isStopped()
    {
        return this.job.isStopped();
    }

    /**
     * @return Returns the startDate.
     */
    public Date getStartDate()
    {
        return this.job.getStartDate();
    }

    /**
     * @return Returns the endDate.
     */
    public Date getEndDate()
    {
        return this.job.getEndDate();
    }

    /**
     * @return Returns the currentDate.
     */
    public Date getCurrentDate()
    {
        return this.job.getCurrentDate();
    }

    /**
     * @return Returns the depDate.
     */
    public Date getDepDate()
    {
        return this.job.getDepDate();
    }

    /**
     * @return Returns the logDate.
     */
    public Date getLogDate()
    {
        return this.job.getLogDate();
    }

    /**
     * @return Returns the jobinfo.
     */
    public JobMeta getJobMeta()
    {
        return this.job.getJobMeta();
    }

    /**
     * @return Returns the rep.
     */
    public Repository getRep()
    {
        return this.job.getRep();
    }

    public Thread getThread()
    {
        return this.job.getThread();
    }

    /**
     * @return Returns the jobTracker.
     */
    public JobTracker getJobTracker()
    {
        return this.job.getJobTracker();
    }

    /**
     * @param jobTracker
     *            The jobTracker to set.
     */
    public void setJobTracker(JobTracker jobTracker)
    {
        this.job.setJobTracker(jobTracker);
    }

    public void setSourceRows(List<RowMetaAndData> sourceRows)
    {
        this.job.setSourceRows(sourceRows);
    }

    public List<RowMetaAndData> getSourceRows()
    {
        return this.job.getSourceRows();
    }

    /**
     * @return Returns the parentJob.
     */
    public Job getParentJob()
    {
        return this.job.getParentJob();
    }

    /**
     * @param parentJob
     *            The parentJob to set.
     */
    public void setParentJob(Job parentJob)
    {
        this.job.setParentJob(parentJob);
    }

    public Result getResult()
    {
        return this.job.getResult();
    }

    public void setResult(Result result)
    {
        this.job.setResult(result);
    }

    /**
     * @return Returns the initialized.
     */
    public boolean isInitialized()
    {
        return this.initialized2.get() ? this.initialized2.get() : this.job
                .isInitialized();
    }

    /**
     * @return Returns the batchId.
     */
    public long getBatchId()
    {
        return this.job.getBatchId();
    }

    /**
     * @param batchId
     *            The batchId to set.
     */
    public void setBatchId(long batchId)
    {
        this.job.setBatchId(batchId);
    }

    /**
     * @return the jobBatchId
     */
    public long getPassedBatchId()
    {
        return this.job.getPassedBatchId();
    }

    /**
     * @param jobBatchId
     *            the jobBatchId to set
     */
    public void setPassedBatchId(long jobBatchId)
    {
        this.job.setPassedBatchId(jobBatchId);
    }

    public void setInternalKettleVariables(VariableSpace var)
    {
        this.job.setInternalKettleVariables(var);
    }

    public String getStatus()
    {
        return this.job.getStatus();
    }

    /**
     * Add a this.job listener to the this.job
     * 
     * @param jobListener
     *            the this.job listener to add
     */
    public void addJobListener(JobListener jobListener)
    {
        this.job.addJobListener(jobListener);
        if (jobListener instanceof ParentProvenanceListener)
        {
            addDatabaseInConnectionPool(((ParentProvenanceListener) jobListener)
                    .getDb());
        }
    }

    public void addJobEntryListener(JobEntryListener jobEntryListener)
    {
        this.job.addJobEntryListener(jobEntryListener);
        if (jobEntryListener instanceof ParentProvenanceListener)
        {
            addDatabaseInConnectionPool(((ParentProvenanceListener) jobEntryListener)
                    .getDb());
        }
    }

    /**
     * Remove a this.job listener from the this.job
     * 
     * @param jobListener
     *            the this.job listener to remove
     */
    public void removeJobListener(JobListener jobListener)
    {
        this.job.removeJobListener(jobListener);
        if (jobListener instanceof ParentProvenanceListener)
        {
            removeDatabaseFromConnectionPool(((ParentProvenanceListener) jobListener)
                    .getDb());
        }
    }

    /**
     * Remove a this.job entry listener from the this.job
     * 
     * @param jobListener
     *            the this.job entry listener to remove
     */
    public void removeJobEntryListener(JobEntryListener jobEntryListener)
    {
        this.job.removeJobEntryListener(jobEntryListener);
        if (jobEntryListener instanceof ParentProvenanceListener)
        {
            removeDatabaseFromConnectionPool(((ParentProvenanceListener) jobEntryListener)
                    .getDb());
        }
    }

    public List<JobEntryListener> getJobEntryListeners()
    {
        return this.job.getJobEntryListeners();
    }

    public List<JobListener> getJobListeners()
    {
        return this.job.getJobListeners();
    }

    /**
     * @return the finished
     */
    public boolean isFinished()
    {
        return this.job.isFinished();
    }

    /**
     * @param finished
     *            the finished to set
     */
    public void setFinished(boolean finished)
    {
        // Fecha as conexoes utilizadas para persistir a
        // proveniencia
        if (finished)
        {
            disconnectConnectionPool();
        }

        this.job.setFinished(finished);
    }

    public void setSocketRepository(SocketRepository socketRepository)
    {
        this.job.setSocketRepository(socketRepository);
    }

    public SocketRepository getSocketRepository()
    {
        return this.job.getSocketRepository();
    }

    public void setLogLevel(LogLevel logLevel)
    {
        this.job.setLogLevel(logLevel);
    }

    public List<LoggingHierarchy> getLoggingHierarchy()
    {
        return this.job.getLoggingHierarchy();
    }

    /**
     * @return the interactive
     */
    public boolean isInteractive()
    {
        return this.job.isInteractive();
    }

    /**
     * @param interactive
     *            the interactive to set
     */
    public void setInteractive(boolean interactive)
    {
        this.job.setInteractive(interactive);
    }

    /**
     * @return the activeJobEntryTransformations
     */
    public Map<JobEntryCopy, JobEntryTrans> getActiveJobEntryTransformations()
    {
        return this.job.getActiveJobEntryTransformations();
    }

    /**
     * @return the activeJobEntryJobs
     */
    public Map<JobEntryCopy, JobEntryJob> getActiveJobEntryJobs()
    {
        return this.job.getActiveJobEntryJobs();
    }

    /**
     * @return A flat list of results in THIS this.job, in the order of
     *         execution of this.job entries
     */
    public List<JobEntryResult> getJobEntryResults()
    {
        return this.job.getJobEntryResults();
    }

    /**
     * @param containerObjectId
     *            the execution container object id to set
     */
    public void setContainerObjectId(String containerObjectId)
    {
        this.job.setContainerObjectId(containerObjectId);
    }

    public LoggingObjectInterface getParentLoggingObject()
    {
        return this.job.getParentLoggingObject();
    }

    /**
     * @return the startJobEntryCopy
     */
    public JobEntryCopy getStartJobEntryCopy()
    {
        return this.job.getStartJobEntryCopy();
    }

    /**
     * @param startJobEntryCopy
     *            the startJobEntryCopy to set
     */
    public void setStartJobEntryCopy(JobEntryCopy startJobEntryCopy)
    {
        this.job.setStartJobEntryCopy(startJobEntryCopy);
    }

    // GETTERS dos atributos especificos
    /**
     * Obtem o codigo da Proveniencia Prospectiva do Job.
     * 
     * @return
     */
    public long getProspJobId()
    {
        return prospJobId;
    }

    /**
     * Obtem o o codigo da Proveniencia Prospectiva do Processo (JobMeta ou
     * TransMeta).
     * 
     * @return
     */
    public Long getProspProcessId(LoggingObjectInterface processMeta)
    {
        Long processId = null;
        if (processMeta instanceof JobMeta)
        {
            processId = prospJobMetaMap.get(processMeta);
            // Rogers (bug): Se retornou nulo, tenta varrer tudo e pegar o id.
            if (processId == null)
            {
                Set<Map.Entry<JobMeta, Long>> entries = prospJobMetaMap
                        .entrySet();
                for (Map.Entry<JobMeta, Long> entry : entries)
                {
                    if (entry.getKey().equals(processMeta))
                    {
                        processId = entry.getValue();
                        break;
                    }
                }
            }
        }
        else if (processMeta instanceof TransMeta)
        {
            processId = prospTransMetaMap.get(processMeta);
            // Rogers (bug): Se retornou nulo, tenta varrer tudo e pegar o id.
            if (processId == null)
            {
                Set<Map.Entry<TransMeta, Long>> entries = prospTransMetaMap
                        .entrySet();
                for (Map.Entry<TransMeta, Long> entry : entries)
                {
                    if (entry.getKey().equals(processMeta))
                    {
                        processId = entry.getValue();
                        break;
                    }
                }
            }
        }
        return processId;
    }

    /**
     * Obtem o o codigo da Proveniencia Prospectiva do Processo (JobMeta ou
     * TransMeta).
     * 
     * @return
     */
    public Long getProspStepId(Object step)
    {
        Long stepId = null;
        if (step instanceof JobEntryCopy)
        {
            JobEntryCopy jec = (JobEntryCopy) step;
            Map<JobEntryCopy, Long> map = prospJobEntryMetaMap.get(jec
                    .getParentJobMeta());
            if (map == null)
            {
                Set<Map.Entry<JobMeta, Map<JobEntryCopy, Long>>> entries = prospJobEntryMetaMap
                        .entrySet();
                for (Map.Entry<JobMeta, Map<JobEntryCopy, Long>> entry : entries)
                {
                    if (entry.getKey().equals(jec.getParentJobMeta()))
                    {
                        map = entry.getValue();
                        break;
                    }
                }
            }
            stepId = map.get(jec);
            if (stepId == null)
            {
                Set<Map.Entry<JobEntryCopy, Long>> entries = map.entrySet();
                for (Map.Entry<JobEntryCopy, Long> entry : entries)
                {
                    if (entry.getKey().equals(jec))
                    {
                        stepId = entry.getValue();
                        break;
                    }
                }
            }
        }
        else if (step instanceof StepMeta)
        {
            StepMeta sm = (StepMeta) step;
            Map<StepMeta, Long> map = prospStepMetaMap.get(sm
                    .getParentTransMeta());
            if (map == null)
            {
                Set<Map.Entry<TransMeta, Map<StepMeta, Long>>> entries = prospStepMetaMap
                        .entrySet();
                for (Map.Entry<TransMeta, Map<StepMeta, Long>> entry : entries)
                {
                    if (entry.getKey().equals(sm.getParentTransMeta()))
                    {
                        map = entry.getValue();
                        break;
                    }
                }
            }
            stepId = map.get(sm);
            if (stepId == null)
            {
                Set<Map.Entry<StepMeta, Long>> entries = map.entrySet();
                for (Map.Entry<StepMeta, Long> entry : entries)
                {
                    if (entry.getKey().equals(sm))
                    {
                        stepId = entry.getValue();
                        break;
                    }
                }
            }
        }
        return stepId;
    }

    /**
     * Obtem a lista de listeners de captura de metadados de proveniencia
     * retrospectiva.
     * 
     * @return
     */
    public List<IRetrospJobListener> getRetrospJobListeners()
    {
        return retrospJobListeners;
    }

    /**
     * Adiciona um this.job listener de proveniencia retrospectiva.
     * 
     * @param jobListener
     */
    public void addRetrospJobListeners(IRetrospJobListener jobListener)
    {
        retrospJobListeners.add(jobListener);
        if (jobListener instanceof ParentProvenanceListener)
        {
            addDatabaseInConnectionPool(((ParentProvenanceListener) jobListener)
                    .getDb());
        }

    }

    /**
     * Remove um this.job listener de proveniencia retrospectiva.
     * 
     * @param jobListener
     */
    public void removeJobListener(IRetrospJobListener jobListener)
    {
        retrospJobListeners.remove(jobListener);
        if (jobListener instanceof ParentProvenanceListener)
        {
            removeDatabaseFromConnectionPool(((ParentProvenanceListener) jobListener)
                    .getDb());
        }
    }

    public long getProspRepoId()
    {
        return prospRepoId;
    }

    public RepositoryMeta getRepoMeta()
    {
        return this.repoMeta;
    }

    public String getRepoLoc()
    {
        return this.repoLoc;
    }

    public DatabaseMeta getProvConnection()
    {
        return this.db.getDatabaseMeta();
    }

    public boolean isFineGrainedEnabled(String smiClassName)
    {
        FineGrainedStep fgStep = FineGrainedStepMap.get().findBySmiClassName(
                smiClassName);
        return (fgStep != null) ? mapFineGrainedEnabled.get(fgStep) : false;
    }

    public Database getDb()
    {
        return db;
    }
}
