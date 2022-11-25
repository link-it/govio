package it.govio.batch.step.beans;

import java.time.LocalDateTime;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Value;

import it.govio.batch.exception.CsvFormatException;
import it.govio.batch.step.beans.CsvItem.CsvItemBuilder;

public class CsvItemLineMapper extends DefaultLineMapper<CsvItem> {

	public LineTokenizer lineTokenizer;

	@Value( "${csv.fieldnames.amount:importo}" )
	private String fieldname_amount;

	@Value( "${csv.fieldnames.invalidAfterDueDate:invalidaAllaScadenza}" )
	private String fieldname_invalidAfterDueDate;

	@Value( "${csv.fieldnames.noticeNumber:numeroAvviso}" )
	private String fieldname_noticeNumber;

	@Value( "${csv.fieldnames.scheduledDate:dataNotifica}" )
	private String fieldname_scheduledDate;

	@Value( "${csv.fieldnames.taxCode:codiceFiscale}" )
	private String fieldname_taxCode;


	@Override
	public CsvItem mapLine(String line, int lineNumber) throws Exception {

		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
		FieldSet tokenizedLine = delimitedLineTokenizer.tokenize(line);
		Properties properties = tokenizedLine.getProperties();

		return CsvItem.builder()
		.rawData(line)
		.rowNumber(lineNumber)
		.amount(properties.getProperty(fieldname_amount))
		.invalidAfterDueDate(properties.getProperty((fieldname_invalidAfterDueDate)))
		.noticeNumber(properties.getProperty(fieldname_noticeNumber))
		.scheduledDate(properties.getProperty(fieldname_scheduledDate))
		.taxcode(properties.getProperty(fieldname_taxCode))
		.placeholderValues(properties)
		.build();
	}
}