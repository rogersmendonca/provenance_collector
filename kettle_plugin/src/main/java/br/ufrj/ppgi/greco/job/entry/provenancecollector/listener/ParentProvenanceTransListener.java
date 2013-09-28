package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.trans.Trans;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation.DB_OPERATION_TYPE;

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
        this.tableName = "retrosp_process";
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

        fields.addValueMeta(new ValueMeta("id", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("start_date", ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("user", ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("success", ValueMetaInterface.TYPE_BOOLEAN));

        data = new Object[fields.size()];
        i = 0;
        data[i++] = trans.getBatchId();
        data[i++] = rootJob.getProspProcessId(trans.getTransMeta());
        data[i++] = new Date(System.currentTimeMillis());
        IUser user = trans.getRepository().getUserInfo();
        data[i++] = (user != null) ? user.getLogin() : "-";
        data[i++] = false;

        operations.add(new DMLOperation(DB_OPERATION_TYPE.INSERT, tableName,
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
        fields.addValueMeta(new ValueMeta("id",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process",
                ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = (trans.getErrors() == 0);
        data[i++] = trans.getBatchId();
        data[i++] = rootJob.getProspProcessId(trans.getTransMeta());

        String[] sets = { "finish_date", "success" };

        String[] codes = { "id", "id_prosp_process" };

        String[] condition = { "=", "=" };

        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        operations.add(new DMLOperation(DB_OPERATION_TYPE.UPDATE, tableName,
                fields, data, codes, condition, sets));

        // Executa os comandos DML
        executeDML(db, operations);
    }
    
    protected void registerDB(Trans trans) throws KettleException
    {
        // Inicializacao
        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        String tableName = null;
        RowMetaInterface fields = null;
        Object[] data = null;
        int i = 0;

        // Insere uma linha na tabela PROV_TRANSFORMATION
        tableName = "retrosp_trans";

        fields = new RowMeta();

        fields.addValueMeta(new ValueMeta("id_batch",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("dep_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("end_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("ended",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("errors",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("file_name",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("log_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("mapping_step",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("name",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("object_id",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("object_name",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("object_type",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("parent_job_id",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("parent_trans_id",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("rep_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("repository",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("rep_directory",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("result_rows",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("row_count",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("start_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("status",
                ValueMetaInterface.TYPE_STRING));

        data = new Object[fields.size()];
        i = 0;
        data[i++] = trans.getBatchId(); // id_batch
        data[i++] = trans.getDepDate(); // dep_date
        data[i++] = trans.getEndDate(); // end_date
        data[i++] = Long.valueOf(trans.getEnded()); // ended
        data[i++] = Long.valueOf(trans.getErrors()); // errors
        data[i++] = trans.getFilename(); // file_name
        data[i++] = trans.getLogDate(); // log_date
        data[i++] = trans.getMappingStepName(); // mapping_step
        data[i++] = trans.getName(); // name
        data[i++] = trans.getObjectId().toString(); // object_id
        data[i++] = trans.getObjectName(); // object_name
        data[i++] = trans.getObjectType().toString(); // object_type
        data[i++] = (trans.getParentJob() != null) ? trans.getParentJob()
                .getBatchId() : new Long(-1); // parent_job_id
        data[i++] = (trans.getParentTrans() != null) ? trans.getParentTrans()
                .getBatchId() : new Long(-1); // parent_trans_id
        data[i++] = trans.getReplayDate(); // rep_date
        data[i++] = trans.getRepository().getName(); // repository
        RepositoryMeta repoMeta = trans.getRepository().getRepositoryMeta();
        String baseDir = null;
        if (repoMeta instanceof KettleFileRepositoryMeta)
        {
            baseDir = ((KettleFileRepositoryMeta) repoMeta).getBaseDirectory();
        }
        else if (repoMeta instanceof KettleDatabaseRepositoryMeta)
        {
            baseDir = ((KettleDatabaseRepositoryMeta) repoMeta).getConnection()
                    .getURL();
        }
        else
        {
            baseDir = "";
        }
        data[i++] = String.format("%s%s", baseDir, trans
                .getRepositoryDirectory().getPath()); // rep_directory

        data[i++] = Long.valueOf(trans.getResult().getRows().size()); // result_rows
        data[i++] = Long.valueOf(trans.getRowsets().size()); // row_count
        data[i++] = trans.getStartDate(); // start_date
        data[i++] = trans.getStatus(); // status

        operations.add(new DMLOperation(DB_OPERATION_TYPE.INSERT, tableName,
                fields, data));

        if ((trans.getRowsets() != null) && (trans.getRowsets().size() > 0))
        {
            // Inclui os campos e valores gerados
            tableName = "retrosp_trans_field";

            fields = new RowMeta();

            fields.addValueMeta(new ValueMeta("id_batch",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("trans_name",
                    ValueMetaInterface.TYPE_STRING));
            fields.addValueMeta(new ValueMeta("field_name",
                    ValueMetaInterface.TYPE_STRING));
            fields.addValueMeta(new ValueMeta("field_value",
                    ValueMetaInterface.TYPE_STRING));
            fields.addValueMeta(new ValueMeta("step_origin",
                    ValueMetaInterface.TYPE_STRING));
            fields.addValueMeta(new ValueMeta("step_dest",
                    ValueMetaInterface.TYPE_STRING));

            RowSet row = trans.getRowsets().get(0);
            int TOTAL_COLS = row.getRowMeta().size();
            String[] fieldNames = row.getRowMeta().getFieldNames();
            String[] fieldNamesAndTypes = row.getRowMeta()
                    .getFieldNamesAndTypes(TOTAL_COLS);
            for (int k = 0; k < TOTAL_COLS; k++)
            {
                data = new Object[fields.size()];
                i = 0;
                data[i++] = trans.getBatchId();
                data[i++] = trans.getName();
                data[i++] = fieldNames[k];
                data[i++] = fieldNamesAndTypes[k];
                data[i++] = row.getOriginStepName();
                data[i++] = row.getDestinationStepName();
                operations.add(new DMLOperation(DB_OPERATION_TYPE.INSERT,
                        tableName, fields, data));
            }
        }

        // Executa os comandos DML
        synchronized (db)
        {
            executeDML(db, operations);
        }
    }
}
