package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class TextFileOutputParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        TextFileOutputMeta meta = (TextFileOutputMeta) sm.getStepMetaInterface();

        stepParamMap.put("fileName", meta.getFileName());
        stepParamMap.put("fileNameInField", boolToStr(meta.isFileNameInField()));
        stepParamMap.put("fileNameField", meta.getFileNameField());
        stepParamMap.put("file_extension", meta.getExtension());
        
        if (meta.getOutputFields() != null)
        {
            stepParamMap.put("field_count", String.valueOf(meta.getOutputFields().length));
            for(int i = 0; i < meta.getOutputFields().length; i++)
            {
                stepParamMap.put("field#" + i, meta.getOutputFields()[i].getName());    
            }
        }
    }
}
