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
import org.pentaho.di.repository.IUser;
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

        fields.addValueMeta(new ValueMeta("id_prosp_repository", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("start_date", ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("user", ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("success", ValueMetaInterface.TYPE_BOOLEAN));

        data = new Object[fields.size()];
        i = 0;
        data[i++] = rootJob.getProspRepoId();
        data[i++] = rootJob.getProspProcessId(trans.getTransMeta());
        data[i++] = trans.getBatchId();
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
        fields.addValueMeta(new ValueMeta("finish_date", ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("success", ValueMetaInterface.TYPE_BOOLEAN));
        fields.addValueMeta(new ValueMeta("id_prosp_repository", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process", ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process", ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = (trans.getErrors() == 0);
        data[i++] = trans.getBatchId();
        data[i++] = rootJob.getProspProcessId(trans.getTransMeta());

        String[] sets = { "finish_date", "success" };

        String[] codes = { "id_prosp_repository", "id_prosp_process", "id_process" };

        String[] condition = { "=", "=", "=" };

        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        operations.add(new DMLOperation(DB_OPERATION_TYPE.UPDATE, tableName, fields, data, codes, condition, sets));

        // Executa os comandos DML
        executeDML(db, operations);
    }
}
