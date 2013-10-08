package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
@XmlRootElement(name = "fineGrainedSteps")
@XmlAccessorType(XmlAccessType.FIELD)
public class FineGrainedSteps
{
    @XmlElement(name="fineGrainedStep")
    private List<FineGrainedStep> fineGrainedSteps;

    public List<FineGrainedStep> getFineGrainedSteps()
    {
        return fineGrainedSteps;
    }

    public void setFineGrainedSteps(List<FineGrainedStep> fineGrainedSteps)
    {
        this.fineGrainedSteps = fineGrainedSteps;
    }
}
