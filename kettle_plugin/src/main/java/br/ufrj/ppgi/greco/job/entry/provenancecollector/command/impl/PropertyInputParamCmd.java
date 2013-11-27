package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.propertyinput.PropertyInputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class PropertyInputParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        PropertyInputMeta meta = (PropertyInputMeta) sm.getStepMetaInterface();
        
        stepParamMap.put("file_type", meta.getFileType());
        stepParamMap.put("encoding", meta.getEncoding());
        stepParamMap.put("include", boolToStr(meta.includeFilename()));
        stepParamMap.put("include_field", meta.getFilenameField());
        stepParamMap.put("filename_Field", meta.getDynamicFilenameField());
        stepParamMap.put("rownum", boolToStr(meta.includeRowNumber()));
        stepParamMap.put("isaddresult", boolToStr(meta.isAddResultFile()));
        stepParamMap.put("filefield", boolToStr(meta.isFileField()));
        stepParamMap.put("rownum_field", meta.getRowNumberField());
        stepParamMap.put("resetrownumber", boolToStr(meta.resetRowNumber()));
        stepParamMap.put("resolvevaluevariable", boolToStr(meta.isResolveValueVariable()));
        stepParamMap.put("ini_section", boolToStr(meta.includeIniSection()));
        stepParamMap.put("ini_section_field", meta.getINISectionField());
        stepParamMap.put("section", meta.getSection());
        
        putListParamInStepParamMap(stepParamMap, "file", meta.getFileName());
        
        String[] inputFields = new String[getArraySize(meta.getInputFields())];
        for (int i = 0; i < inputFields.length; i++)
        {
            inputFields[i] = meta.getInputFields()[i].getName();
        }
        putListParamInStepParamMap(stepParamMap, "field", inputFields);

        stepParamMap.put("limit", String.valueOf(meta.getRowLimit()));
        stepParamMap.put("shortFileFieldName", meta.getShortFileNameField());
        stepParamMap.put("pathFieldName", meta.getPathField());
        stepParamMap.put("hiddenFieldName", meta.isHiddenField());
        stepParamMap.put("lastModificationTimeFieldName", meta.getLastModificationDateField());
        stepParamMap.put("uriNameFieldName", meta.getUriField());
        stepParamMap.put("rootUriNameFieldName", meta.getRootUriField());
        stepParamMap.put("extensionFieldName", meta.getExtensionField());
        stepParamMap.put("sizeFieldName", meta.getSizeField());
    }
}
