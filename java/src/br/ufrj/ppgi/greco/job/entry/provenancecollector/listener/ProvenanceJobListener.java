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
import org.pentaho.di.repository.IUser;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation.DB_OPERATION_TYPE;

public class ProvenanceJobListener extends ParentProvenanceListener implements
        IRetrospJobListener
{
    private String tableName;
    private JobDecorator rootJob;

    public ProvenanceJobListener(Database db, JobDecorator rootJob)
    {
        super(db);
        this.tableName = "retrosp_process";
        this.rootJob = rootJob;
    }

    @Override
    public void jobStarted(Job job) throws KettleException
    {
        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("start_date", ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("user", ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("success", ValueMetaInterface.TYPE_BOOLEAN));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = job.getBatchId();
        data[i++] = rootJob.getProspProcessId(job.getJobMeta());
        data[i++] = new Date(System.currentTimeMillis());
        IUser user = job.getRep().getUserInfo();
        data[i++] = (user != null) ? user.getLogin() : "-";
        data[i++] = false;

        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        operations.add(new DMLOperation(DB_OPERATION_TYPE.INSERT, tableName,
                fields, data));

        executeDML(db, operations);
    }

    @Override
    public void jobFinished(Job job, boolean success)
            throws KettleException
    {
        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("finish_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("success",
                ValueMetaInterface.TYPE_BOOLEAN));
        fields.addValueMeta(new ValueMeta("id",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process",
                ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = success;
        data[i++] = job.getBatchId();
        data[i++] = rootJob.getProspProcessId(job.getJobMeta());

        String[] sets = { "finish_date", "success" };

        String[] codes = { "id", "id_prosp_process" };

        String[] condition = { "=", "=" };

        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        operations.add(new DMLOperation(DB_OPERATION_TYPE.UPDATE, tableName,
                fields, data, codes, condition, sets));

        // Executa os comandos DML
        executeDML(db, operations);
    }
}
