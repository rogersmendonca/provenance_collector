package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util.DMLOperation.DB_OPERATION_TYPE;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.specialization.TransProv;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.EnumStepType;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 * 
 */
public class ProvenanceRowListener extends ParentProvenanceListener implements
        RowListener
{
    protected StepInterface step;
    private String tableName;
    private JobDecorator rootJob;
    private TransProv transProv;
    private final boolean VAR_FALSE = false;

    Map<String, Long> fieldMap = null;

    public ProvenanceRowListener(Database db, StepInterface step,
            JobDecorator rootJob, TransProv transProv)
    {
        super(db);
        this.step = step;
        this.tableName = "retrosp_step_field_row";
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
            // Rogers: Definir somente os passos em que for necessario
            if (VAR_FALSE)
            {
                registerDB(rowMeta, row, 'R', step.getLinesRead());
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
        try
        {
            StepMetaInterface smi = this.step.getStepMeta()
                    .getStepMetaInterface();
            // Merge Join
            if (rootJob.isFineGrainedEnabled(EnumStepType.MERGE_JOIN)
                    && (smi instanceof MergeJoinMeta))
            {
                // incrementa 1 no numero de linhas escritas, pois o metodo eh
                // executado antes da incrementacao do contador no metodo putRow
                registerDB(rowMeta, row, 'W', step.getLinesWritten() + 1);
            }
        }
        catch (KettleException e)
        {
            throw new KettleStepException(e.toString());
        }
    }

    @Override
    public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
            throws KettleStepException
    {
        try
        {
            // Rogers: Definir somente os passos em que for necessario
            if (VAR_FALSE)
            {
                // incrementa 1 no numero de linhas escritas, pois o metodo eh
                // executado antes da incrementacao do contador no metodo
                // putError
                registerDB(rowMeta, row, 'E', step.getLinesRejected() + 1);
            }
        }
        catch (KettleException e)
        {
            throw new KettleStepException(e.toString());
        }
    }

    protected Map<String, Long> getFieldMap(RowMetaInterface rowMeta)
            throws KettleException
    {
        if (fieldMap == null)
        {
            StringBuilder SQL = new StringBuilder();
            SQL.append("SELECT t1.id_field, t1.name ");
            SQL.append("FROM retrosp_step_field t1 ");
            SQL.append("WHERE t1.id_prosp_repository = ? ");
            SQL.append("AND   t1.id_prosp_process = ? ");
            SQL.append("AND   t1.id_process = ? ");
            SQL.append("AND   t1.id_prosp_step = ? ");
            SQL.append("AND   t1.seq = ? ");

            RowMetaInterface fields = new RowMeta();
            fields.addValueMeta(new ValueMeta("id_prosp_repository",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_prosp_process",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_process",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("id_prosp_step",
                    ValueMetaInterface.TYPE_INTEGER));
            fields.addValueMeta(new ValueMeta("seq",
                    ValueMetaInterface.TYPE_INTEGER));

            Object[] data = new Object[fields.size()];
            int i = 0;
            data[i++] = rootJob.getProspRepoId();
            data[i++] = rootJob.getProspProcessId(transProv.getTransMeta());
            data[i++] = transProv.getBatchId();
            data[i++] = rootJob.getProspStepId(step.getStepMeta());
            data[i++] = transProv.generateStepMetaSeq(step);

            ResultSet res = db.openQuery(SQL.toString(), fields, data);
            fieldMap = new HashMap<String, Long>();
            try
            {
                while (res.next())
                {
                    fieldMap.put(res.getString("name"), res.getLong("id_field"));
                }
            }
            catch (SQLException e)
            {
            }
            db.closeQuery(res);

            if (fieldMap.size() != rowMeta.getFieldNames().length)
            {
                for (String fieldName : rowMeta.getFieldNames())
                {

                    // OBTEM O FIELD ID
                    String tableName = "retrosp_step_field";
                    HashMap<String, Long> restriction = new HashMap<String, Long>();
                    restriction.put("id_prosp_repository",
                            rootJob.getProspRepoId());
                    restriction
                            .put("id_prosp_process",
                                    rootJob.getProspProcessId(transProv
                                            .getTransMeta()));
                    restriction.put("id_process", transProv.getBatchId());
                    restriction.put("id_prosp_step",
                            rootJob.getProspStepId(step.getStepMeta()));
                    restriction.put("seq", transProv.generateStepMetaSeq(step));
                    Long fieldId = rootJob.generateId(db, tableName,
                            restriction);

                    // INSERE O FIELD NA TABELA RETROSP_STEP_FIELD
                    fields = new RowMeta();
                    fields.addValueMeta(new ValueMeta("id_prosp_repository",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("id_prosp_process",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("id_process",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("id_prosp_step",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("seq",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("id_field",
                            ValueMetaInterface.TYPE_INTEGER));
                    fields.addValueMeta(new ValueMeta("name",
                            ValueMetaInterface.TYPE_STRING));

                    data = new Object[fields.size()];
                    i = 0;
                    data[i++] = rootJob.getProspRepoId();
                    data[i++] = rootJob.getProspProcessId(transProv
                            .getTransMeta());
                    data[i++] = transProv.getBatchId();
                    data[i++] = rootJob.getProspStepId(step.getStepMeta());
                    data[i++] = transProv.generateStepMetaSeq(step);
                    data[i++] = fieldId;
                    data[i++] = fieldName;

                    db.insertRow(tableName, fields, data);

                    // INSERE O FIELD NO MAPEAMENTO
                    fieldMap.put(fieldName, fieldId);
                }
            }
        }
        return fieldMap;
    }

    protected void registerDB(RowMetaInterface rowMeta, Object[] row,
            char event, long rowNr) throws KettleException
    {
        // Inclui os campos e valores gerados
        RowMetaInterface fields = new RowMeta();

        fields.addValueMeta(new ValueMeta("id_prosp_repository",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_process",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_prosp_step",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("seq",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("id_field",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("row_count",
                ValueMetaInterface.TYPE_INTEGER));
        fields.addValueMeta(new ValueMeta("event",
                ValueMetaInterface.TYPE_STRING));
        fields.addValueMeta(new ValueMeta("field_value",
                ValueMetaInterface.TYPE_STRING));

        int TOTAL_COLS = rowMeta.getFieldNames().length;
        List<DMLOperation> operations = new ArrayList<DMLOperation>();
        for (int k = 0; k < TOTAL_COLS; k++)
        {
            Object[] data = new Object[fields.size()];
            int i = 0;
            data[i++] = rootJob.getProspRepoId();
            data[i++] = rootJob.getProspProcessId(transProv.getTransMeta());
            data[i++] = transProv.getBatchId();
            data[i++] = rootJob.getProspStepId(step.getStepMeta());
            data[i++] = transProv.generateStepMetaSeq(step);
            data[i++] = getFieldMap(rowMeta).get(rowMeta.getFieldNames()[k]);
            data[i++] = rowNr;
            data[i++] = event;
            data[i++] = rowMeta.getString(row, k);
            operations.add(new DMLOperation(DB_OPERATION_TYPE.INSERT,
                    tableName, fields, data));
        }

        // Executa os comandos DML
        synchronized (db)
        {
            executeDML(db, operations);
        }

    }
}
