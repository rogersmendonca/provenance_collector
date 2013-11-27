package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.DMLOperation;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.DMLOperation.EnumDMLOperation;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 * 
 */
public abstract class ParentProvenanceTransListener extends
        ParentProvenanceListener
{
    private String tableName;
    private JobDecorator rootJob;

    public ParentProvenanceTransListener(Database db, JobDecorator rootJob)
    {
        super(db);
        this.tableName = "retrosp_workflow";
        this.rootJob = rootJob;
    }

    protected void transStarted(Trans trans) throws KettleException
    {
        // Inicializacao
        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        RowMetaInterface fields = null;
        Object[] data = null;
        int i = 0;

        fields = new RowMeta();

        fields.addValueMeta(new ValueMeta("id_prosp_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_workflow",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_workflow",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("start_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("success",
                ValueMetaInterface.TYPE_BOOLEAN));
        fields.addValueMeta(new ValueMeta("id_root_prosp_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_root_prosp_workflow",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_root",
                ValueMetaInterface.TYPE_INTEGER));

        LoggingObjectInterface parent = trans.getParent();
        Job parentJob = trans.getParentJob();
        Trans parentTrans = trans.getParentTrans();
        if ((parent != null) && ((parentJob != null) || (parentTrans != null)))
        {
            fields.addValueMeta(new ValueMeta("id_parent_prosp_repository",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_parent_prosp_workflow",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_parent",
                    ValueMetaInterface.TYPE_INTEGER));
        }

        data = new Object[fields.size()];
        i = 0;
        data[i++] = rootJob.getProspRepoId();
        data[i++] = rootJob.getProspWorkflowId(trans.getTransMeta());
        data[i++] = trans.getBatchId();
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = false;
        data[i++] = rootJob.getProspRepoId();
        data[i++] = rootJob.getProspJobId();
        data[i++] = rootJob.getBatchId();
        if ((parent != null) && ((parentJob != null) || (parentTrans != null)))
        {
            data[i++] = rootJob.getProspRepoId();
            data[i++] = rootJob.getProspWorkflowId((parentJob != null) ? parentJob.getJobMeta() : parentTrans.getTransMeta());
            data[i++] = (parentJob != null) ? parentJob.getBatchId()
                    : parentTrans.getBatchId();
        }

        operations.add(new DMLOperation(EnumDMLOperation.INSERT, tableName,
                fields, data));

        // Executa os comandos DML
        synchronized (db)
        {
            executeDML(db, operations);
        }
    }

    public void transFinished(Trans trans) throws KettleException
    {
        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("finish_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("success",
                ValueMetaInterface.TYPE_BOOLEAN));
        fields.addValueMeta(new ValueMeta("id_prosp_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_workflow",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_workflow",
                ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = (trans.getErrors() == 0);
        data[i++] = rootJob.getProspRepoId();
        data[i++] = rootJob.getProspWorkflowId(trans.getTransMeta());
        data[i++] = trans.getBatchId();

        String[] sets = { "finish_date", "success" };

        String[] codes = { "id_prosp_repository", "id_prosp_workflow",
                "id_workflow" };

        String[] condition = { "=", "=", "=" };

        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        operations.add(new DMLOperation(EnumDMLOperation.UPDATE, tableName,
                fields, data, codes, condition, sets));

        // Executa os comandos DML
        synchronized (db)
        {
            executeDML(db, operations);
        }
    }
}
