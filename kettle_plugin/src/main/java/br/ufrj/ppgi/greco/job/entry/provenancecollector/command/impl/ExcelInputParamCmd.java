package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class ExcelInputParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        ExcelInputMeta meta = (ExcelInputMeta) sm.getStepMetaInterface();

        stepParamMap.put("header", boolToStr(meta.startsWithHeader()));
        stepParamMap.put("noempty", boolToStr(meta.ignoreEmptyRows()));
        stepParamMap.put("stoponempty", boolToStr(meta.stopOnEmpty()));
        stepParamMap.put("filefield", meta.getFileField());
        stepParamMap.put("sheetfield", meta.getSheetField());
        stepParamMap.put("sheetrownumfield", meta.getSheetRowNumberField());
        stepParamMap.put("rownumfield", meta.getRowNumberField());
        stepParamMap.put("sheetfield", meta.getSheetField());
        stepParamMap.put("limit", String.valueOf(meta.getRowLimit()));
        stepParamMap.put("encoding", meta.getEncoding());
        stepParamMap.put("add_to_result_filenames",
                boolToStr(meta.isAddResultFile()));
        stepParamMap.put("accept_filenames",
                boolToStr(meta.isAcceptingFilenames()));
        stepParamMap.put("accept_field", meta.getAcceptingField());
        stepParamMap.put("accept_stepname", meta.getAcceptingStepName());

        putListParamInStepParamMap(stepParamMap, "file", meta.getFileName());

        String[] fields = new String[getArraySize(meta.getField())];
        for (int i = 0; i < fields.length; i++)
        {
            fields[i] = meta.getField()[i].getName();
        }
        putListParamInStepParamMap(stepParamMap, "field", fields);

        putListParamInStepParamMap(stepParamMap, "sheet", meta.getSheetName());

        // ERROR HANDLING
        stepParamMap.put("strict_types", boolToStr(meta.isStrictTypes()));
        stepParamMap.put("error_ignored", boolToStr(meta.isErrorIgnored()));
        stepParamMap.put("error_line_skipped",
                boolToStr(meta.isErrorLineSkipped()));

        stepParamMap.put("bad_line_files_destination_directory",
                meta.getWarningFilesDestinationDirectory());
        stepParamMap.put("bad_line_files_extension",
                meta.getBadLineFilesExtension());
        stepParamMap.put("error_line_files_destination_directory",
                meta.getErrorFilesDestinationDirectory());
        stepParamMap.put("error_line_files_extension",
                meta.getErrorFilesExtension());
        stepParamMap.put("line_number_files_destination_directory",
                meta.getLineNumberFilesDestinationDirectory());
        stepParamMap.put("line_number_files_extension",
                meta.getLineNumberFilesExtension());

        stepParamMap.put("shortFileFieldName", meta.getShortFileNameField());
        stepParamMap.put("pathFieldName", meta.getPathField());
        stepParamMap.put("hiddenFieldName", meta.isHiddenField());
        stepParamMap.put("lastModificationTimeFieldName",
                meta.getLastModificationDateField());
        stepParamMap.put("uriNameFieldName", meta.getUriField());
        stepParamMap.put("rootUriNameFieldName", meta.getRootUriField());
        stepParamMap.put("extensionFieldName", meta.getExtensionField());
        stepParamMap.put("sizeFieldName", meta.getSizeField());

        stepParamMap.put("spreadsheet_type",
                (meta.getSpreadSheetType() != null) ? meta.getSpreadSheetType()
                        .getDescription() : "");
    }
}
