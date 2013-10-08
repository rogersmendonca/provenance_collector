package br.ufrj.ppgi.greco.job.entry.provenancecollector.listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.variables.VariableSpace;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.DMLOperation;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 * 
 */
public abstract class ParentProvenanceListener
{
    protected Database db;

    public ParentProvenanceListener(Database db)
    {
        this.db = db;
    }

    public static Database getDatabase(
            LoggingObjectInterface loggingObjectInterface,
            VariableSpace variableSpace, DatabaseMeta logcon)
            throws KettleDatabaseException
    {
        return getDatabase(loggingObjectInterface, variableSpace, logcon, false);
    }

    public static Database getDatabase(
            LoggingObjectInterface loggingObjectInterface,
            VariableSpace variableSpace, DatabaseMeta logcon, boolean autoCommit)
            throws KettleDatabaseException
    {
        Database db = new Database(loggingObjectInterface, logcon);
        // Database db = new Database2(loggingObjectInterface, logcon);
        db.shareVariablesWith(variableSpace);
        db.connect();
        db.setAutoCommit(autoCommit);
        return db;
    }

    public static long getId(Database db, String tableName)
            throws KettleException
    {
        try
        {
            ResultSet res = db.openQuery("SELECT COUNT(*) + 1 AS id FROM "
                    + tableName);
            long id = res.next() ? res.getLong("id") : 0;
            db.closeQuery(res);
            return id;
        }
        catch (SQLException e)
        {
            throw new KettleException(e.toString());
        }
    }

    protected void executeDML(Database db, List<DMLOperation> operations)
            throws KettleDatabaseException
    {
        for (int i = 0; i < operations.size(); i++)
        {
            DMLOperation oper = operations.get(i);
            switch (oper.getType())
            {
                case INSERT:
                    db.insertRow(oper.getTableName(), oper.getFields(),
                            oper.getData());
                    break;
                case UPDATE:
                    db.prepareUpdate(oper.getTableName(), oper.getCodes(),
                            oper.getCondition(), oper.getSets());
                    db.setValuesUpdate(oper.getFields(), oper.getData());
                    db.updateRow();
                    break;
                case DELETE:
                    db.prepareDelete(oper.getTableName(), oper.getCodes(),
                            oper.getCondition());
                    db.setValuesUpdate(oper.getFields(), oper.getData());
                    db.updateRow();
                    break;
            }
        }

        try
        {
            if (!db.getConnection().getAutoCommit())
                db.commit(true);
        }
        catch (SQLException e)
        {
            db.rollback();
            throw new KettleDatabaseException(e.getMessage());
        }
    }

    public Database getDb()
    {
        return db;
    }
}
