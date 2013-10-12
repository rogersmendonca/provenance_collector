package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.StepDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization.TransProv;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.DMLOperation;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.DMLOperation.EnumDMLOperation;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 * 
 */
public class ProvenanceRowListener extends ParentProvenanceListener implements
        RowListener
{
    protected StepDecorator step;
    private String tableName;
    private String tableNameHopField;
    private JobDecorator rootJob;
    private TransProv transProv;

    public ProvenanceRowListener(Database db, StepDecorator stepDecorator,
            JobDecorator rootJob, TransProv transProv)
    {
        super(db);
        this.step = stepDecorator;
        this.tableName = "retrosp_row_field";
        this.tableNameHopField = "prosp_hop_field";
        this.rootJob = rootJob;
        this.transProv = transProv;
    }

    @Override
    /**
     * Executado nos metodos BaseStep.getRow()
     */
    public void rowReadEvent(RowMetaInterface rowMeta, Object[] row)
            throws KettleStepException
    {
        try
        {
            // Get current Row Set
            RowSet currentRowSet = null;
            List<RowSet> rowSetList = this.step.getOriginalStep()
                    .getInputRowSets();
            for (RowSet rowSet : rowSetList)
            {
                if ((rowSet.getRowMeta() != null)
                        && (rowMeta != null)
                        && rowSet.getRowMeta().toString()
                                .equals(rowMeta.toString()))
                {
                    currentRowSet = rowSet;
                    break;
                }
            }

            // Se o step from do hop for fine grained e estiver habilitado,
            // registra a proveniencia
            if (currentRowSet != null)
            {
                StepInterface originStep = this.transProv
                        .findRunThread(currentRowSet.getOriginStepName());

                Class<?> originSmiClass = originStep.getStepMeta()
                        .getStepMetaInterface().getClass();

                if (rootJob.isFineGrainedEnabled(originSmiClass.getName()))
                {
                    registerDB(rowMeta, row, originStep, step.getLinesRead());
                }
            }
        }
        catch (KettleException e)
        {
            throw new KettleStepException(e.toString());
        }
    }

    @Override
    /**
     * Executado nos metodos BaseStep.putRow, BaseStep.putRowTo
     */
    public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
            throws KettleStepException
    {
    }

    @Override
    public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
            throws KettleStepException
    {
    }

    protected Map<String, Long> getHopFieldMap(RowMetaInterface rowMeta,
            StepInterface originStep) throws KettleException
    {
        StringBuilder SQL = new StringBuilder();
        SQL.append("SELECT t1.id_field, t1.field_name ");
        SQL.append("FROM " + this.tableNameHopField + " t1 ");
        SQL.append("WHERE t1.id_repository = ? ");
        SQL.append("AND   t1.id_process = ? ");
        SQL.append("AND   t1.id_step_from = ? ");
        SQL.append("AND   t1.id_step_to = ? ");

        RowMetaInterface fields = new RowMeta();
        fields.addValueMeta(new ValueMeta("id_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_step_from",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_step_to",
                ValueMetaInterface.TYPE_INTEGER));

        Object[] data = new Object[fields.size()];
        int i = 0;
        data[i++] = rootJob.getProspRepoId();
        data[i++] = rootJob.getProspProcessId(transProv.getTransMeta());
        data[i++] = rootJob.getProspStepId(originStep.getStepMeta());
        data[i++] = rootJob.getProspStepId(step.getStepMeta());

        HashMap<String, Long> hopFieldMap = new HashMap<String, Long>();
        synchronized (db)
        {
            ResultSet res = db.openQuery(SQL.toString(), fields, data);
            try
            {
                while (res.next())
                {
                    hopFieldMap.put(res.getString("field_name"),
                            res.getLong("id_field"));
                }
            }
            catch (SQLException e)
            {
                System.out.println(e.getMessage());
            }
            finally
            {
                db.closeQuery(res);
            }
        }

        if (hopFieldMap.size() != rowMeta.getFieldNames().length)
        {
            hopFieldMap.clear();
            for (String fieldName : rowMeta.getFieldNames())
            {
                // OBTEM O FIELD ID
                HashMap<String, Long> restriction = new HashMap<String, Long>();
                restriction.put("id_repository", rootJob.getProspRepoId());
                restriction.put("id_process",
                        rootJob.getProspProcessId(transProv.getTransMeta()));
                restriction.put("id_step_from",
                        rootJob.getProspStepId(originStep.getStepMeta()));
                restriction.put("id_step_to",
                        rootJob.getProspStepId(step.getStepMeta()));

                Long fieldId = rootJob.generateId(db, tableNameHopField,
                        restriction);

                // INSERE O FIELD NA TABELA RETROSP_STEP_FIELD
                fields = new RowMeta();
                fields.addValueMeta(new ValueMeta("id_repository",
                        ValueMetaInterface.TYPE_INTEGER));
                fields.addValueMeta(new ValueMeta("id_process",
                        ValueMetaInterface.TYPE_INTEGER));
                fields.addValueMeta(new ValueMeta("id_step_from",
                        ValueMetaInterface.TYPE_INTEGER));
                fields.addValueMeta(new ValueMeta("id_step_to",
                        ValueMetaInterface.TYPE_INTEGER));
                fields.addValueMeta(new ValueMeta("id_field",
                        ValueMetaInterface.TYPE_INTEGER));
                fields.addValueMeta(new ValueMeta("field_name",
                        ValueMetaInterface.TYPE_STRING));

                data = new Object[fields.size()];
                i = 0;
                data[i++] = rootJob.getProspRepoId();
                data[i++] = rootJob.getProspProcessId(transProv.getTransMeta());
                data[i++] = rootJob.getProspStepId(originStep.getStepMeta());
                data[i++] = rootJob.getProspStepId(step.getStepMeta());
                data[i++] = fieldId;
                data[i++] = fieldName;

                List<DMLOperation> operations = new ArrayList<DMLOperation>();
                operations.add(new DMLOperation(EnumDMLOperation.INSERT,
                        tableNameHopField, fields, data));

                synchronized (db)
                {
                    executeDML(db, operations);
                }

                // INSERE O FIELD NO MAPEAMENTO
                hopFieldMap.put(fieldName, fieldId);
            }
        }
        return hopFieldMap;
    }

    protected void registerDB(RowMetaInterface rowMeta, Object[] row,
            StepInterface originStep, long rowNr) throws KettleException
    {
        // Inclui os campos e valores gerados
        RowMetaInterface fields = new RowMeta();

        fields.addValueMeta(new ValueMeta("id_prosp_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_step_from",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_step_to",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_field",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("seq_from",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("seq_to",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("row_count",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("field_value",
                ValueMetaInterface.TYPE_STRING));

        int TOTAL_COLS = rowMeta.getFieldNames().length;
        List<DMLOperation> operations = new ArrayList<DMLOperation>();

        if (originStep.getStepname().equals("Input - Research Group"))
        {
            System.out.println("teste");
        }

        Map<String, Long> hopFieldMap = getHopFieldMap(rowMeta, originStep);
        for (int k = 0; k < TOTAL_COLS; k++)
        {
            Object[] data = new Object[fields.size()];
            int i = 0;
            data[i++] = rootJob.getProspRepoId();
            data[i++] = rootJob.getProspProcessId(transProv.getTransMeta());
            data[i++] = rootJob.getProspStepId(originStep.getStepMeta());
            data[i++] = rootJob.getProspStepId(step.getStepMeta());
            Long id_prosp_field = hopFieldMap.get(rowMeta.getFieldNames()[k]);
            data[i++] = id_prosp_field;
            data[i++] = transProv.getBatchId();
            data[i++] = transProv.getStepMetaSeq(originStep);
            data[i++] = transProv.getStepMetaSeq(step);
            data[i++] = rowNr;
            data[i++] = rowMeta.getString(row, k);
            operations.add(new DMLOperation(EnumDMLOperation.INSERT, tableName,
                    fields, data));
        }

        // Executa os comandos DML
        synchronized (db)
        {
            executeDML(db, operations);
        }
    }
}
