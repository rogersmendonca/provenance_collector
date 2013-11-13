package br.ufrj.ppgi.greco.job.entry.provenancecollector.command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
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
 * Registra os parametros dos Steps de Granulosidade Fina.
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
public abstract class StepParameterCmd
{
    private String tableProspStepParam = "prosp_step_parameter";
    private String tableRetrospStepParam = "retrosp_step_parameter";
    private Map<String, Long> prospStepParamMap = null;

    public static void execute(JobDecorator rootJob, Database db,
            Object prospStep, Long workflowId, Long stepId)
            throws KettleException
    {
        if (prospStep instanceof JobEntryCopy)
        {
            // Nesta primeira versao, os parametros do JobEntry nao serao
            // coletados
        }
        else if (prospStep instanceof StepMeta)
        {
            StepMeta sm = (StepMeta) prospStep;
            String smiClassName = sm.getStepMetaInterface().getClass()
                    .getName();

            FineGrainedStep fgStep = FineGrainedStepMap.get()
                    .findBySmiClassName(smiClassName);

            StepParameterCmd cmd = (fgStep != null) ? fgStep.getCmd()
                    : new NullParamCmd();

            if (rootJob.isFineGrainedEnabled(smiClassName))
            {
                HashMap<String, String> stepParamMap = new HashMap<String, String>();
                cmd.populaStepParamMap(stepParamMap, sm);
                cmd.insertParamProvenance(rootJob, db, sm, workflowId, stepId,
                        stepParamMap);
            }
        }
    }

    private void insertParamProvenance(JobDecorator rootJob, Database db,
            StepMeta sm, Long workflowId, Long stepId,
            HashMap<String, String> stepParamMap) throws KettleException
    {
        synchronized (db)
        {
            for (Map.Entry<String, String> entryParam : stepParamMap.entrySet())
            {
                String paramName = entryParam.getKey();
                String paramValue = entryParam.getValue();
                Long prospWorkflowId = rootJob.getProspWorkflowId(sm
                        .getParentTransMeta());
                Long prospStepId = rootJob.getProspStepId(sm);
                Long paramId = getProspStepParamMap(rootJob, db, sm).get(
                        paramName);
                RowMetaInterface fields = null;
                Object[] data = null;
                int i = 0;

                // Insere prosp_step_parameter
                if (paramId == null)
                {
                    HashMap<String, Long> restriction = new HashMap<String, Long>();
                    restriction.put("id_repository", rootJob.getProspRepoId());
                    restriction.put("id_workflow", prospWorkflowId);
                    restriction.put("id_step", prospStepId);
                    paramId = rootJob.generateId(db, tableProspStepParam,
                            restriction);

                    fields = new RowMeta();
                    fields.addValueMeta(new ValueMeta("id_repository",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("id_workflow",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("id_step",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("id_step_param",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("param_name",
                            ValueMetaInterface.TYPE_STRING));

                    data = new Object[fields.size()];
                    i = 0;
                    data[i++] = rootJob.getProspRepoId();
                    data[i++] = prospWorkflowId;
                    data[i++] = prospStepId;
                    data[i++] = paramId;
                    data[i++] = paramName;

                    db.insertRow(tableProspStepParam, fields, data);
                }

                // Insere retrosp_step_parameter
                if ((paramValue != null) && (paramValue.trim().length() > 0))
                {
                    fields = new RowMeta();
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
                    fields.addValueMeta(new ValueMeta("id_prosp_step_param",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("param_value",
                            ValueMetaInterface.TYPE_STRING));

                    data = new Object[fields.size()];
                    i = 0;
                    data[i++] = rootJob.getProspRepoId();
                    data[i++] = prospWorkflowId;
                    data[i++] = workflowId;
                    data[i++] = prospStepId;
                    data[i++] = stepId;
                    data[i++] = paramId;
                    data[i++] = paramValue;

                    db.insertRow(tableRetrospStepParam, fields, data);
                }
            }
        }
    }

    private Map<String, Long> getProspStepParamMap(JobDecorator rootJob,
            Database db, StepMeta stepMeta) throws KettleDatabaseException
    {
        if (this.prospStepParamMap == null)
        {
            this.prospStepParamMap = new HashMap<String, Long>();

            Long workflowId = rootJob.getProspWorkflowId(stepMeta
                    .getParentTransMeta());
            Long stepId = rootJob.getProspStepId(stepMeta);
            StringBuilder SQL = new StringBuilder();
            SQL.append("SELECT t1.id_step_param, t1.param_name ");
            SQL.append("FROM " + tableProspStepParam + " t1 ");
            SQL.append("WHERE t1.id_repository = ? ");
            SQL.append("AND   t1.id_workflow = ? ");
            SQL.append("AND   t1.id_step = ? ");

            RowMetaInterface fields = new RowMeta();
            fields.addValueMeta(new ValueMeta("id_repository",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_workflow",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_step",
                    ValueMetaInterface.TYPE_INTEGER));

            Object[] data = new Object[fields.size()];
            int i = 0;
            data[i++] = rootJob.getProspRepoId();
            data[i++] = workflowId;
            data[i++] = stepId;

            synchronized (db)
            {
                ResultSet res = db.openQuery(SQL.toString(), fields, data);
                try
                {
                    while (res.next())
                    {
                        this.prospStepParamMap.put(res.getString("param_name"),
                                res.getLong("id_step_param"));
                    }
                }
                catch (SQLException e)
                {
                }
                finally
                {
                    db.closeQuery(res);
                }
            }
        }
        return this.prospStepParamMap;
    }

    protected abstract void populaStepParamMap(
            Map<String, String> stepParamMap, StepMeta sm);
}
