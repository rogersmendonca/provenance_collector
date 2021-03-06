package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization.TransProv;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.DMLOperation;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.DMLOperation.EnumDMLOperation;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 * 
 */
public class ProvenanceStepListener extends ParentProvenanceListener implements
        StepListener
{
    private String tableName;
    private JobDecorator rootJob;
    private TransProv transProv;

    public ProvenanceStepListener(Database db, JobDecorator rootJob,
            TransProv transProv)
    {
        super(db);
        this.tableName = "retrosp_step";
        this.rootJob = rootJob;
        this.transProv = transProv;
    }

    @Override
    public void stepActive(Trans trans, StepMeta stepMeta, StepInterface step)
    {
        // Inicializacoes
        Long workflowId = trans.getBatchId();
        Long stepId = transProv.generateStepMetaSeq(step);

        // Insere uma linha na tabela retrosp_step, registrando o inicio da
        // execucao do Step
        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_prosp_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_workflow",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_workflow",
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
        fields.addValueMeta(new ValueMeta("id_user",
                ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;

        data[i++] = rootJob.getProspRepoId();
        data[i++] = rootJob.getProspWorkflowId(transProv.getTransMeta());
        data[i++] = workflowId;
        data[i++] = rootJob.getProspStepId(stepMeta);
        data[i++] = stepId;
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = null;
        data[i++] = false;
        try
        {
            IUser user = trans.getRepository().getUserInfo();
            data[i++] = rootJob.getUserId(db, user);
        }
        catch (KettleException e)
        {
            throw new RuntimeException(e.toString());
        }

        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        operations.add(new DMLOperation(EnumDMLOperation.INSERT, tableName,
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

        // Insere os parametros do Step (caso seja fine-grained)
        try
        {
            StepParameter.execute(rootJob, db, stepMeta, workflowId, stepId);
            if (!db.getConnection().getAutoCommit())
                db.commit(true);
        }
        catch (Exception e)
        {
            try
            {
                db.rollback();
            }
            catch (KettleDatabaseException e1)
            {
                throw new RuntimeException(e.toString());
            }
            throw new RuntimeException(e.toString());
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
        fields.addValueMeta(new ValueMeta("id_prosp_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_workflow",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_workflow",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_step",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("seq",
                ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = new Date(System.currentTimeMillis());
        data[i++] = (step.getErrors() == 0);
        data[i++] = rootJob.getProspRepoId();
        data[i++] = rootJob.getProspWorkflowId(transProv.getTransMeta());
        data[i++] = transProv.getBatchId();
        data[i++] = rootJob.getProspStepId(stepMeta);
        data[i++] = transProv.getStepMetaSeq(step);

        String[] sets = { "finish_date", "success" };

        String[] codes = { "id_prosp_repository", "id_prosp_workflow",
                "id_workflow", "id_prosp_step", "seq" };

        String[] condition = { "=", "=", "=", "=", "=" };

        operations.add(new DMLOperation(EnumDMLOperation.UPDATE, tableName,
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
