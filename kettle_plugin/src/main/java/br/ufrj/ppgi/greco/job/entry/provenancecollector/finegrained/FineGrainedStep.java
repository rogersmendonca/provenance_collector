package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained;

import java.beans.Transient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.pentaho.di.i18n.BaseMessages;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.JobEntryProvenanceCollector;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.ParentProspStepParamCmd;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl.NullParamCmd;
import br.ufrj.ppgi.greco.job.entry.provenancecollector.util.EnumETLOperation;

/**
 * Step de Granulosidade Fina.
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
@XmlRootElement(name = "fineGrainedStep")
@XmlAccessorType(XmlAccessType.FIELD)
public class FineGrainedStep
{
    // for i18n purposes, needed by Translator2!! $NON-NLS-1$
    private static Class<?> PKG = JobEntryProvenanceCollector.class;
    private String id;
    private String descKey;
    private EnumETLOperation operation;
    private String smiClassName;
    private String cmdClassName;
    private ParentProspStepParamCmd cmd;

    public FineGrainedStep()
    {

    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDescKey()
    {
        return descKey;
    }

    @XmlTransient
    public String getDescription()
    {
        return BaseMessages.getString(PKG, getDescKey());
    }

    public void setDescKey(String descKey)
    {
        this.descKey = descKey;
    }

    public EnumETLOperation getOperation()
    {
        return operation;
    }

    public void setOperation(EnumETLOperation operation)
    {
        this.operation = operation;
    }

    public void setOperation(String operation)
    {
        this.operation = EnumETLOperation.valueOf(operation);
    }

    public String getSmiClassName()
    {
        return smiClassName;
    }

    public void setSmiClassName(String smiClassName)
    {
        this.smiClassName = smiClassName;
    }

    public String getCmdClassName()
    {
        return cmdClassName;
    }

    @Transient
    public ParentProspStepParamCmd getCmd()
    {
        if (this.cmd == null)
        {
            try
            {
                this.cmd = (ParentProspStepParamCmd) Class.forName(
                        cmdClassName).newInstance();
            }
            catch (InstantiationException e)
            {
                cmd = new NullParamCmd();
            }
            catch (IllegalAccessException e)
            {
                cmd = new NullParamCmd();
            }
            catch (ClassNotFoundException e)
            {
                cmd = new NullParamCmd();
            }
        }
        return cmd;
    }

    public void setCmdClassName(String cmdClassName)
    {
        this.cmdClassName = cmdClassName;
    }
}
