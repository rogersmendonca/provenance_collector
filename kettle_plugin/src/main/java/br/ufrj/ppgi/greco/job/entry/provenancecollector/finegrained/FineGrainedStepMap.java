package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Singleton com o mapeamento dos Steps de Granulosidade Fina.
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
@XmlRootElement(name = "fineGrainedSteps")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryClass = FineGrainedStepMap.class, factoryMethod = "get")
public class FineGrainedStepMap
{
    private static final String FINE_GRAINED_STEPS_XML = "/fine_grained_steps.xml";
    private static FineGrainedStepMap singleton;
    private Map<String, FineGrainedStep> fineGrainedStepMap;

    static
    {
        JAXBContext jaxbContext;
        try
        {
            // Carrega o singleton a partir do xml FINE_GRAINED_STEPS_XML
            jaxbContext = JAXBContext.newInstance(FineGrainedStepMap.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            InputStream inputXML = FineGrainedStepMap.class
                    .getResourceAsStream(FINE_GRAINED_STEPS_XML);
            singleton = (FineGrainedStepMap) jaxbUnmarshaller
                    .unmarshal(inputXML);
        }
        catch (JAXBException e)
        {
            // Se ocorreu algum problema, instancia um singleton vazio
            singleton = new FineGrainedStepMap();
            singleton.fineGrainedStepMap = new HashMap<String, FineGrainedStep>();
        }

    }

    private FineGrainedStepMap()
    {

    }

    public static FineGrainedStepMap get()
    {
        if (singleton == null)
        {
            singleton = new FineGrainedStepMap();
        }

        return singleton;
    }

    public Map<String, FineGrainedStep> getFineGrainedStepMap()
    {
        return fineGrainedStepMap;
    }

    public void setFineGrainedSteps(
            Map<String, FineGrainedStep> fineGrainedStepMap)
    {
        this.fineGrainedStepMap = fineGrainedStepMap;
    }

    public int size()
    {
        return getFineGrainedStepMap().size();
    }

    public FineGrainedStep findBySmiClassName(String smiClassName)
    {
        FineGrainedStep ret = null;
        for (FineGrainedStep fgStep : getFineGrainedStepMap().values())
        {
            if (fgStep.getSmiClassName().equals(smiClassName))
            {
                ret = fgStep;
                break;
            }
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof FineGrainedStepMap;
    }

    @Override
    public int hashCode()
    {
        return 0;
    }
}
