package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation.DB_OPERATION_TYPE;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization.TransProv;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 *
 */
public class ProvenanceStepListener extends ParentProvenanceListener implements
        StepListener
{
    protected StepInterface step;
    private String tableName;
    private JobDecorator rootJob;
    private TransProv transProv;

    public ProvenanceStepListener(Database db, StepInterface step,
            JobDecorator rootJob, TransProv transProv)
    {
        super(db);
        this.step = step;
        this.tableName = "retrosp_step";
        this.rootJob = rootJob;
        this.transProv = transProv;
    }

    @Override
    public void stepActive(Trans trans, StepMeta stepMeta, StepInterface step)
    {
        // Insere uma linha na tabela retrosp_jobentry,
        // registrando o inicio da execucao do Step

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_step",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("seq",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("start_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("finish_date",
                ValueMetaInterface.TYPE_DATE));        
        fields.addValueMeta(new ValueMeta("success",
                ValueMetaInterface.TYPE_BOOLEAN));

        Object[] data = new Object[fields.size()];
        int i = 0;

        data[i++] = transProv.getBatchId();
        data[i++] = rootJob.getProspProcessId(transProv.getTransMeta());
        data[i++] = rootJob.getProspStepId(stepMeta);
        data[i++] = transProv.generateStepMetaSeq(step);
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = null;        
        data[i++] = false;

        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        operations.add(new DMLOperation(DB_OPERATION_TYPE.INSERT, tableName,
                fields, data));

        // Executa os comandos DML
        synchronized (db)
        {
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
    public void stepIdle(Trans trans, StepMeta stepMeta, StepInterface step)
    {

    }

    @Override
    public void stepFinished(Trans trans, StepMeta stepMeta, StepInterface step)
    {
        List<DMLOperation> operations = new ArrayList<DMLOperation>();

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("finish_date",
                ValueMetaInterface.TYPE_DATE));
        fields.addValueMeta(new ValueMeta("success",
                ValueMetaInterface.TYPE_BOOLEAN));
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_step",
                ValueMetaInterface.TYPE_INTEGER));        
        fields.addValueMeta(new ValueMeta("seq",
                ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = (step.getErrors() == 0);
        data[i++] = trans.getBatchId();
        data[i++] = rootJob.getProspProcessId(transProv.getTransMeta());
        data[i++] = rootJob.getProspStepId(stepMeta);
        data[i++] = transProv.getStepMetaSeq(step);

        String[] sets = { "finish_date", "success" };

        String[] codes = { "id_process", "id_prosp_process", "id_prosp_step", "seq" };

        String[] condition = { "=", "=", "=", "=" };

        operations.add(new DMLOperation(DB_OPERATION_TYPE.UPDATE, tableName,
                fields, data, codes, condition, sets));

        // Executa os comandos DML
        synchronized (db)
        {
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
