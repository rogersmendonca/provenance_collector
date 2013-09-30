package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryListener;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation.DB_OPERATION_TYPE;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 *
 */
public class ProvenanceJobEntryListener extends ParentProvenanceListener
        implements JobEntryListener
{
    private static long seq;
    static
    {
        seq = 1;
    }
    private String tableName;
    private JobDecorator rootJob;
    private Map<JobEntryCopy, Long> mapJobEntryCopySeq;

    public ProvenanceJobEntryListener(Database db, JobDecorator rootJob)
    {
        super(db);
        this.tableName = "retrosp_step";
        this.rootJob = rootJob;
        this.mapJobEntryCopySeq = new HashMap<JobEntryCopy, Long>();
    }

    @Override
    public void beforeExecution(Job job, JobEntryCopy jec, JobEntryInterface jei)
    {
        // Insere uma linha na tabela retrosp_jobentry,
        // registrando o inicio da execucao do JobEntry
        if (job instanceof JobDecorator)
        {
            RowMetaInterface fields = new RowMeta();
            fields.addValueMeta(new ValueMeta("id_prosp_repository", ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_prosp_process", ValueMetaInterface.TYPE_INTEGER));            
            fields.addValueMeta(new ValueMeta("id_process", ValueMetaInterface.TYPE_INTEGER));            
            fields.addValueMeta(new ValueMeta("id_prosp_step", ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("seq", ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("start_date", ValueMetaInterface.TYPE_DATE));
            fields.addValueMeta(new ValueMeta("finish_date", ValueMetaInterface.TYPE_DATE));
            fields.addValueMeta(new ValueMeta("success", ValueMetaInterface.TYPE_BOOLEAN));

            Object[] data = new Object[fields.size()];
            int i = 0;
            this.mapJobEntryCopySeq.put(jec, seq++);
            data[i++] = rootJob.getProspRepoId();
            data[i++] = rootJob.getProspProcessId(job.getJobMeta());
            data[i++] = job.getBatchId();            
            data[i++] = rootJob.getProspStepId(jec);
            data[i++] = this.mapJobEntryCopySeq.get(jec);
            data[i++] = new Date(System.currentTimeMillis());
            data[i++] = null;
            data[i++] = false;

            List<DMLOperation> operations = new ArrayList<DMLOperation>();
            operations.add(new DMLOperation(DB_OPERATION_TYPE.INSERT,
                    tableName, fields, data));

            // Executa os comandos DML
            try
            {
                executeDML(db, operations);
            }
            catch (KettleDatabaseException e)
            {
                throw new RuntimeException(e.toString());
            }
        }
    }

    @Override
    public void afterExecution(Job job, JobEntryCopy jec,
            JobEntryInterface jei, Result result)
    {
        if (job instanceof JobDecorator)
        {
            List<DMLOperation> operations = new ArrayList<DMLOperation>();

            RowMetaInterface fields = new RowMeta();
            fields.addValueMeta(new ValueMeta("finish_date", ValueMetaInterface.TYPE_DATE));
            fields.addValueMeta(new ValueMeta("success", ValueMetaInterface.TYPE_BOOLEAN));
            fields.addValueMeta(new ValueMeta("id_prosp_repository", ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_prosp_process", ValueMetaInterface.TYPE_INTEGER));            
            fields.addValueMeta(new ValueMeta("id_process", ValueMetaInterface.TYPE_INTEGER));            
            fields.addValueMeta(new ValueMeta("id_prosp_step", ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("seq", ValueMetaInterface.TYPE_INTEGER));

            Object[] data = new Object[fields.size()];
            int i = 0;
            data[i++] = new Date(System.currentTimeMillis());
            data[i++] = (result.getNrErrors() == 0);
            data[i++] = rootJob.getProspRepoId();
            data[i++] = rootJob.getProspProcessId(job.getJobMeta());
            data[i++] = job.getBatchId();            
            data[i++] = rootJob.getProspStepId(jec);
            data[i++] = this.mapJobEntryCopySeq.get(jec);

            String[] sets = { "finish_date", "success" };

            String[] codes = { "id_prosp_repository", "id_prosp_process", "id_process",  "id_prosp_step", "seq" };

            String[] condition = { "=", "=", "=", "=", "=" };

            operations.add(new DMLOperation(DB_OPERATION_TYPE.UPDATE, tableName, fields, data, codes, condition, sets));

            // Executa os comandos DML
            try
            {
                executeDML(db, operations);
            }
            catch (KettleDatabaseException e)
            {
                throw new RuntimeException(e.toString());
            }
        }
    }
}
