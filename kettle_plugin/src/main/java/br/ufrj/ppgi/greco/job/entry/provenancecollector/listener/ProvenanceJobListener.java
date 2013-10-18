package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.DMLOperation;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.DMLOperation.EnumDMLOperation;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 *
 */
public class ProvenanceJobListener extends ParentProvenanceListener implements
        IRetrospJobListener
{
    private String tableName;
    private JobDecorator rootJob;

    public ProvenanceJobListener(Database db, JobDecorator rootJob)
    {
        super(db);
        this.tableName = "retrosp_workflow";
        this.rootJob = rootJob;
    }

    @Override
    public void jobStarted(Job job) throws KettleException
    {
        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_prosp_repository", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_workflow", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_workflow", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("start_date", ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("success", ValueMetaInterface.TYPE_BOOLEAN));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = rootJob.getProspRepoId();
        data[i++] = rootJob.getProspWorkflowId(job.getJobMeta());
        data[i++] = job.getBatchId();        
        data[i++] = new Date(System.currentTimeMillis());        
        data[i++] = false;

        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        operations.add(new DMLOperation(EnumDMLOperation.INSERT, tableName, fields, data));

        executeDML(db, operations);
    }

    @Override
    public void jobFinished(Job job, boolean success)
            throws KettleException
    {
        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("finish_date", ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("success", ValueMetaInterface.TYPE_BOOLEAN));
        fields.addValueMeta(new ValueMeta("id_prosp_repository", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_workflow", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_workflow", ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = success;
        data[i++] = rootJob.getProspRepoId();        
        data[i++] = rootJob.getProspWorkflowId(job.getJobMeta());
        data[i++] = job.getBatchId();

        String[] sets = { "finish_date", "success" };

        String[] codes = { "id_prosp_repository", "id_prosp_workflow", "id_workflow" };

        String[] condition = { "=", "=", "=" };

        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        operations.add(new DMLOperation(EnumDMLOperation.UPDATE, tableName, fields, data, codes, condition, sets));

        // Executa os comandos DML
        executeDML(db, operations);
    }
}
