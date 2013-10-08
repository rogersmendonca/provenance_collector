package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XmlAdapter de List<FineGrainedStep> para LinkedHashMap<String,
 * FineGrainedStep>
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
public class FineGrainedStepAdapter extends
        XmlAdapter<FineGrainedSteps, Map<String, FineGrainedStep>>
{
    @Override
    public Map<String, FineGrainedStep> unmarshal(
            FineGrainedSteps fineGrainedSteps) throws Exception
    {
        Collections.sort(fineGrainedSteps.getFineGrainedSteps(),
                new FineGrainedStepComparator());

        Map<String, FineGrainedStep> map = new LinkedHashMap<String, FineGrainedStep>();
        for (FineGrainedStep fineGrainedStep : fineGrainedSteps
                .getFineGrainedSteps())
        {
            map.put(fineGrainedStep.getId(), fineGrainedStep);
        }
        return map;
    }
    
    @Override
    public FineGrainedSteps marshal(Map<String, FineGrainedStep> map)
            throws Exception
    {
        FineGrainedSteps fineGrainedSteps = new FineGrainedSteps();
        fineGrainedSteps.setFineGrainedSteps(new LinkedList<FineGrainedStep>(
                map.values()));
        return fineGrainedSteps;
    }

    /**
     * Ordena primeiro pelo tipo da Operacao (E, T ou L) e depois pelo id do
     * Step.
     * 
     * @author Rogers Reiche de Mendonca
     * @since out-2013
     * 
     */
    private class FineGrainedStepComparator implements
            Comparator<FineGrainedStep>
    {
        @Override
        public int compare(FineGrainedStep fgs1, FineGrainedStep fgs2)
        {
            int comp = fgs1.getOperation().getOrder()
                    - fgs2.getOperation().getOrder();

            return (comp == 0) ? fgs1.getId().compareToIgnoreCase(fgs2.getId())
                    : comp;
        }
    }
}