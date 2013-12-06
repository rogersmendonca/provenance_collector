package br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.finegrained.strategy.StepParameter;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class CsvInputParam extends StepParameter
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        CsvInputMeta meta = (CsvInputMeta) sm.getStepMetaInterface();

        stepParamMap.put("FILENAME", meta.getFilename());
        stepParamMap.put("FILENAME_FIELD", meta.getFilenameField());
        stepParamMap.put("ROW_NUM_FIELD", meta.getRowNumField());
        stepParamMap.put("INCLUDE_FILENAME",
                boolToStr(meta.isIncludingFilename()));
        stepParamMap.put("DELIMITER", meta.getDelimiter());
        stepParamMap.put("ENCLOSURE", meta.getEnclosure());
        stepParamMap.put("HEADER_PRESENT", boolToStr(meta.isHeaderPresent()));
        stepParamMap.put("BUFFERSIZE", meta.getBufferSize());
        stepParamMap.put("LAZY_CONVERSION",
                boolToStr(meta.isLazyConversionActive()));
        stepParamMap.put("ADD_FILENAME_RESULT",
                boolToStr(meta.isAddResultFile()));
        stepParamMap.put("PARALLEL", boolToStr(meta.isRunningInParallel()));
        stepParamMap.put("NEWLINE_POSSIBLE",
                boolToStr(meta.isNewlinePossibleInFields()));
        stepParamMap.put("ENCODING", meta.getEncoding());

        String[] inputFields = new String[getArraySize(meta.getInputFields())];
        for (int i = 0; i < inputFields.length; i++)
        {
            inputFields[i] = meta.getInputFields()[i].getName();
        }
        putListParamInStepParamMap(stepParamMap, "FIELD", inputFields);
    }
}
