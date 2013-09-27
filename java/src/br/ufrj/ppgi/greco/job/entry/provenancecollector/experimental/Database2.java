package br.ufrj.ppgi.greco.job.entry.provenancecollector.experimental;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class Database2 extends Database
{
    static private Log logger;
    static
    {
        PropertyConfigurator
                .configure("D:/projetos/Teste/src/log4j.properties");
        logger = LogFactory.getLog(Database2.class);
    }

    @SuppressWarnings("deprecation")
    public Database2(DatabaseMeta databaseMeta)
    {
        super(databaseMeta);
        // TODO Auto-generated constructor stub
    }

    public Database2(LoggingObjectInterface parentObject,
            DatabaseMeta databaseMeta)
    {
        super(parentObject, databaseMeta);
    }

    @Override
    public String getInsertStatement(String schemaName, String tableName,
            RowMetaInterface fields)
    {
        String sql = super.getInsertStatement(schemaName, tableName, fields);
        logger.debug("INSERT SQL = " + sql);
        return sql;
    }

    /**
     * Sets the values of the preparedStatement pstmt.
     * 
     * @param rowMeta
     * @param data
     */
    @Override
    public void setValuesInsert(RowMetaInterface rowMeta, Object[] data)
            throws KettleDatabaseException
    {
        // now set the values in the row!
        for (int i = 0; i < rowMeta.size(); i++)
        {
            ValueMetaInterface v = rowMeta.getValueMeta(i);
            Object object = data[i];
            
            if (v.getName().equalsIgnoreCase("trans_id") && (object == null)) {
                logger.debug(String.format("*** OOPS %d - %d = %s", (i + 1),
                        v.getType(), String.valueOf(object)));
                object = "0";
            }
            logger.debug(String.format("*** %d - %d = %s", (i + 1),
                    v.getType(), String.valueOf(object)));
        }

        super.setValues(rowMeta, data, getPrepStatementInsert());
    }
}
