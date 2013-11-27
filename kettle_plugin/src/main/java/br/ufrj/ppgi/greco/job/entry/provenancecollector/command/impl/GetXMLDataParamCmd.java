package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class GetXMLDataParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        GetXMLDataMeta meta = (GetXMLDataMeta) sm.getStepMetaInterface();
        
        stepParamMap.put("include", boolToStr(meta.includeFilename()));
        stepParamMap.put("include_field", meta.getFilenameField());
        stepParamMap.put("rownum", boolToStr(meta.includeRowNumber()));
        stepParamMap.put("addresultfile",boolToStr( meta.addResultFile()));
        stepParamMap.put("namespaceaware", boolToStr(meta.isNamespaceAware()));
        stepParamMap.put("ignorecomments", boolToStr(meta.isIgnoreComments()));
        stepParamMap.put("readurl", boolToStr(meta.isReadUrl()));
        stepParamMap.put("validating", boolToStr(meta.isValidating()));
        stepParamMap.put("usetoken", boolToStr(meta.isuseToken()));
        stepParamMap.put("IsIgnoreEmptyFile", boolToStr(meta.isIgnoreEmptyFile()));
        stepParamMap.put("doNotFailIfNoFile", boolToStr(meta.isdoNotFailIfNoFile()));
         
        stepParamMap.put("rownum_field", meta.getRowNumberField());
        stepParamMap.put("encoding", meta.getEncoding());
        
        putListParamInStepParamMap(stepParamMap, "file", meta.getFileName());
        
        String[] inputFields = new String[getArraySize(meta.getInputFields())];
        for (int i = 0; i < inputFields.length; i++)
        {
            inputFields[i] = meta.getInputFields()[i].getName();
        }
        putListParamInStepParamMap(stepParamMap, "field", inputFields);
        
        stepParamMap.put("limit", String.valueOf(meta.getRowLimit()));
        stepParamMap.put("loopxpath", meta.getLoopXPath());
        stepParamMap.put("IsInFields", boolToStr(meta.isInFields()));
        stepParamMap.put("IsAFile", boolToStr(meta.getIsAFile()));        
        stepParamMap.put("XmlField", meta.getXMLField());
        stepParamMap.put("prunePath", meta.getPrunePath());
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
