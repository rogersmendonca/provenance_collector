package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.xmloutput.XMLOutputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class XMLOutputParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        XMLOutputMeta meta = (XMLOutputMeta) sm.getStepMetaInterface();

        stepParamMap.put("fileName", meta.getFileName());
        stepParamMap.put("file_extension", meta.getExtension());
        stepParamMap.put("encoding", meta.getEncoding());
        stepParamMap.put("parent_xml_element", meta.getMainElement());
        stepParamMap.put("repeat_xml_element", meta.getRepeatElement());

        if (meta.getOutputFields() != null)
        {
            stepParamMap.put("field_count", String.valueOf(meta.getOutputFields().length));
            for(int i = 0; i < meta.getOutputFields().length; i++)
            {
                stepParamMap.put("field#" + i, meta.getOutputFields()[i].getFieldName());
                stepParamMap.put("element#" + i, meta.getOutputFields()[i].getElementName());
            }
        }
    }
}
