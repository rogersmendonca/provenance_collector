package br.ufrj.ppgi.greco.job.entry.provenancecollector.command.impl;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;

import br.ufrj.ppgi.greco.job.entry.provenancecollector.command.StepParameterCmd;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2013
 * 
 */
public class TextFileInputParamCmd extends StepParameterCmd
{
    @Override
    public void populateStepParamMap(Map<String, String> stepParamMap,
            StepMeta sm)
    {
        TextFileInputMeta meta = (TextFileInputMeta) sm.getStepMetaInterface();

        stepParamMap.put("accept_filenames", boolToStr(meta.isAcceptingFilenames()));
        stepParamMap.put("passing_through_fields", boolToStr(meta.isPassingThruFields()));
        stepParamMap.put("accept_field", meta.getAcceptingField());
        stepParamMap.put("accept_stepname", meta.getAcceptingStepName());
        
        stepParamMap.put("separator", meta.getSeparator());
        stepParamMap.put("enclosure", meta.getEnclosure());
        stepParamMap.put("enclosure_breaks", boolToStr(meta.isBreakInEnclosureAllowed()));
        stepParamMap.put("escapechar", meta.getEscapeCharacter());
        stepParamMap.put("header", boolToStr(meta.hasHeader()));
        stepParamMap.put("nr_headerlines", String.valueOf(meta.getNrHeaderLines()));
        stepParamMap.put("footer", boolToStr(meta.hasFooter()));
        stepParamMap.put("nr_footerlines", String.valueOf(meta.getNrFooterLines()));
        stepParamMap.put("line_wrapped", boolToStr(meta.isLineWrapped()));
        stepParamMap.put("nr_wraps", String.valueOf(meta.getNrWraps()));
        stepParamMap.put("layout_paged", boolToStr(meta.isLayoutPaged()));
        stepParamMap.put("nr_lines_per_page", String.valueOf(meta.getNrLinesPerPage()));
        stepParamMap.put("nr_lines_doc_header", String.valueOf(meta.getNrLinesDocHeader()));
        stepParamMap.put("noempty", boolToStr(meta.noEmptyLines()));
        stepParamMap.put("include", boolToStr(meta.includeFilename()));
        stepParamMap.put("include_field", meta.getFilenameField());
        stepParamMap.put("rownum", boolToStr(meta.includeRowNumber()));
        stepParamMap.put("rownumByFile", boolToStr(meta.isRowNumberByFile()));
        stepParamMap.put("rownum_field", meta.getRowNumberField());
        stepParamMap.put("format", meta.getFileFormat());
        stepParamMap.put("encoding", meta.getEncoding());
        stepParamMap.put("add_to_result_filenames", boolToStr(meta.isAcceptingFilenames()));
        
        putListParamInStepParamMap(stepParamMap, "file", meta.getFileName());
        stepParamMap.put("file_type", meta.getFileType());
        stepParamMap.put("file_compression", meta.getFileCompression());
        
        String[] filters = new String[getArraySize(meta.getFilter())];
        for (int i = 0; i < filters.length; i++)
        {
            filters[i] = meta.getFilter()[i].getFilterString();
        }
        putListParamInStepParamMap(stepParamMap, "filter", filters);
        
        String[] inputFields = new String[getArraySize(meta.getInputFields())];
        for (int i = 0; i < inputFields.length; i++)
        {
            inputFields[i] = meta.getInputFields()[i].getName();
        }
        putListParamInStepParamMap(stepParamMap, "field", inputFields);

        stepParamMap.put("limit", String.valueOf(meta.getRowLimit()));

        // ERROR HANDLING
        stepParamMap.put("error_ignored", boolToStr(meta.isErrorIgnored()));
        stepParamMap.put("skip_bad_files", boolToStr(meta.isSkipBadFiles()));
        stepParamMap.put("file_error_field", meta.getFileErrorField());
        stepParamMap.put("file_error_message_field", meta.getFileErrorMessageField());
        stepParamMap.put("error_line_skipped", boolToStr(meta.isErrorLineSkipped()));
        stepParamMap.put("error_count_field", meta.getErrorCountField());
        stepParamMap.put("error_fields_field", meta.getErrorFieldsField());
        stepParamMap.put("error_text_field", meta.getErrorTextField());

        stepParamMap.put("bad_line_files_destination_directory", meta.getWarningFilesDestinationDirectory());
        stepParamMap.put("bad_line_files_extension", meta.getWarningFilesExtension());
        stepParamMap.put("error_line_files_destination_directory", meta.getErrorFilesDestinationDirectory());
        stepParamMap.put("error_line_files_extension", meta.getErrorLineFilesExtension());
        stepParamMap.put("line_number_files_destination_directory", meta.getLineNumberFilesDestinationDirectory());
        stepParamMap.put("line_number_files_extension", meta.getLineNumberFilesExtension());

        stepParamMap.put("date_format_lenient", boolToStr(meta.isDateFormatLenient()));
        stepParamMap.put("date_format_locale", (meta.getDateFormatLocale() != null) ? meta.getDateFormatLocale().toString() : "");

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
