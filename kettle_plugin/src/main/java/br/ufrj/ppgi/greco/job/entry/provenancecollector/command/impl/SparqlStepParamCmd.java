package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.lang.reflect.InvocationTargetException;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.ParentProspStepParamCmd;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.decorator.JobDecorator;

/**
 * Command registro dos dados de proveniencia prospectiva do step SPARQL_INPUT.
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
public class SparqlStepParamCmd extends ParentProspStepParamCmd
{
    public void insertProvenance(JobDecorator rootJob, Database db,
            StepMeta sm, Long processId) throws KettleException
    {
        StepMetaInterface smi = sm.getStepMetaInterface();

        try
        {
            // Obtem valores dos campos por reflection
            String endpoint = (String) smi.getClass()
                    .getMethod("getEndpointUri").invoke(smi);
            String defaultGraph = (String) smi.getClass()
                    .getMethod("getDefaultGraph").invoke(smi);
            String sparqlInput = (String) smi.getClass()
                    .getMethod("getQueryString").invoke(smi);

            // Endpoint URI
            if (endpoint != null)
            {
                insertProspStepParam(rootJob, db, sm, processId,
                        "ENDPOINT_URI", endpoint);
            }

            // Default Graph
            if (defaultGraph != null)
            {
                insertProspStepParam(rootJob, db, sm, processId,
                        "DEFAULT_GRAPH", defaultGraph);
            }

            // SPARQL
            if (sparqlInput != null)
            {
                insertProspStepParam(rootJob, db, sm, processId, "SPARQL",
                        sparqlInput);
            }
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
