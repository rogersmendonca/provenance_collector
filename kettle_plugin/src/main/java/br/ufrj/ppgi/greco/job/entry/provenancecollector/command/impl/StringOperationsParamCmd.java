package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class StringOperationsParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        StringOperationsMeta meta = (StringOperationsMeta) sm.getStepMetaInterface();
        
        putListParamInStepParamMap(stepParamMap, "in_stream_name", meta.getFieldInStream());
        putListParamInStepParamMap(stepParamMap, "out_stream_name", meta.getFieldOutStream());
        
        if (meta.getTrimType() != null) 
        {
            String[] arr = new String[meta.getTrimType().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = StringOperationsMeta.getTrimTypeDesc(i);
                putListParamInStepParamMap(stepParamMap, "trim_type", arr);        
            }
        }
        
        if (meta.getLowerUpper() != null) 
        {
            String[] arr = new String[meta.getLowerUpper().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = StringOperationsMeta.getLowerUpperDesc(i);
                putListParamInStepParamMap(stepParamMap, "lower_upper", arr);        
            }
        }
        
        if (meta.getPaddingType() != null) 
        {
            String[] arr = new String[meta.getPaddingType().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = StringOperationsMeta.getPaddingDesc(i);
                putListParamInStepParamMap(stepParamMap, "padding_type", arr);        
            }
        }

        putListParamInStepParamMap(stepParamMap, "pad_char", meta.getPadChar());
        putListParamInStepParamMap(stepParamMap, "pad_len", meta.getPadLen());
        
        if (meta.getInitCap() != null) 
        {
            String[] arr = new String[meta.getInitCap().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = StringOperationsMeta.getInitCapDesc(i);
                putListParamInStepParamMap(stepParamMap, "init_cap", arr);        
            }
        }
        
        if (meta.getMaskXML() != null) 
        {
            String[] arr = new String[meta.getMaskXML().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = StringOperationsMeta.getMaskXMLDesc(i);
                putListParamInStepParamMap(stepParamMap, "mask_xml", arr);        
            }
        }
        
        if (meta.getDigits() != null) 
        {
            String[] arr = new String[meta.getDigits().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = StringOperationsMeta.getDigitsDesc(i);
                putListParamInStepParamMap(stepParamMap, "digits", arr);        
            }
        }
        
        if (meta.getRemoveSpecialCharacters() != null) 
        {
            String[] arr = new String[meta.getRemoveSpecialCharacters().length];
            for(int i = 0; i < arr.length; i++) 
            {
                arr[i] = StringOperationsMeta.getRemoveSpecialCharactersDesc(i);
                putListParamInStepParamMap(stepParamMap, "remove_special_characters", arr);        
            }
        }
    }
}
