package br.ufrj.ppgi.greco.job.entry.provenancecollector.command;

import java.util.HashMap;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.step.StepMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl.NullParamCmd;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.FineGrainedStep;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.FineGrainedStepMap;

/**
 * Command pai de registro dos dados de proveniencia prospectiva dos Steps de
 * Granulosidade Fina.
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
public abstract class ParentProspStepParamCmd
{
    protected void insertProspStepParam(JobDecorator rootJob, Database db,
            StepMeta stepMeta, long processId, String fieldName,
            String fieldValue) throws KettleException
    {
        String tableName = "prosp_step_parameter";
        Long stepId = rootJob.getProspStepId(stepMeta);
        HashMap<String, Long> restriction = new HashMap<String, Long>();
        restriction.put("id_repository", rootJob.getProspRepoId());
        restriction.put("id_process", processId);
        restriction.put("id_step", stepId);
        Long paramId = rootJob.generateId(db, tableName, restriction);

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
        data[i++] = rootJob.getProspRepoId();
        data[i++] = processId;
        data[i++] = stepId;
        data[i++] = paramId;
        data[i++] = fieldName;
        data[i++] = fieldValue;

        db.insertRow(tableName, fields, data);
    }

    public static ParentProspStepParamCmd get(StepMeta stepMeta)
    {
        String smiClassName = stepMeta.getStepMetaInterface().getClass()
                .getName();
        FineGrainedStep fgStep = FineGrainedStepMap.get().findBySmiClassName(
                smiClassName);
        if (fgStep != null)
        {
            return fgStep.getCmd();
        }
        else
        {
            return new NullParamCmd();
        }
    }

    public void execute(JobDecorator rootJob, Database db, Object step,
            Long processId) throws KettleException
    {
        if (step instanceof JobEntryCopy)
        {
            // Nesta primeira versao, os parametros do JobEntry nao serao
            // coletados
        }
        else if (step instanceof StepMeta)
        {
            StepMeta sm = (StepMeta) step;
            Class<?> smiClass = sm.getStepMetaInterface().getClass();

            if (rootJob.isFineGrainedEnabled(smiClass.getName()))
            {
                insertProvenance(rootJob, db, sm, processId);
            }
        }
    }

    public abstract void insertProvenance(JobDecorator rootJob, Database db,
            StepMeta sm, Long processId) throws KettleException;
}
