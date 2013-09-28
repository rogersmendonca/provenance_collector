package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener.util;

import org.pentaho.di.core.row.RowMetaInterface;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 *
 */
public class DMLOperation
{
    public enum DB_OPERATION_TYPE
    {
        INSERT, UPDATE, DELETE
    };

    private DB_OPERATION_TYPE type;
    private String tableName;
    private RowMetaInterface fields;
    private Object[] data;
    private String[] codes;
    private String[] condition;
    private String[] sets;

    public DMLOperation(DB_OPERATION_TYPE type, String tableName,
            RowMetaInterface fields, Object[] data)
    {
        this.type = type;
        this.tableName = tableName;
        this.fields = fields;
        this.data = data;
    }

    public DMLOperation(DB_OPERATION_TYPE type, String tableName,
            RowMetaInterface fields, Object[] data, String[] codes,
            String[] condition, String[] sets)
    {
        this(type, tableName, fields, data);
        this.codes = codes;
        this.condition = condition;
        this.sets = sets;
    }

    public DB_OPERATION_TYPE getType()
    {
        return type;
    }

    public String getTableName()
    {
        return tableName;
    }

    public RowMetaInterface getFields()
    {
        return fields;
    }

    public Object[] getData()
    {
        return data;
    }

    public String[] getSets()
    {
        return sets;
    }

    public String[] getCodes()
    {
        return codes;
    }

    public String[] getCondition()
    {
        return condition;
    }
}
