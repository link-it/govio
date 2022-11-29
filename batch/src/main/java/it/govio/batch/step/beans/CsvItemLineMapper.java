package it.govio.batch.step.beans;

import java.util.Properties;

import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Value;

public class CsvItemLineMapper extends DefaultLineMapper<CsvItem> {
	
	public CsvItemLineMapper(LineTokenizer lineTokenizer) {
		this.lineTokenizer = lineTokenizer;
	}

	public LineTokenizer lineTokenizer;

	@Value( "${csv.fieldnames.amount:importo}" )
	public static String fieldname_amount;

	@Value( "${csv.fieldnames.invalidAfterDueDate:invalidaAllaScadenza}" )
	public static String fieldname_invalidAfterDueDate;

	@Value( "${csv.fieldnames.noticeNumber:numeroAvviso}" )
	public static String fieldname_noticeNumber;

	@Value( "${csv.fieldnames.scheduledDate:dataNotifica}" )
	public static String fieldname_scheduledDate;

	@Value( "${csv.fieldnames.taxCode:codiceFiscale}" )
	public static String fieldname_taxCode;


	@Override
	public CsvItem mapLine(String line, int lineNumber) throws Exception {

		FieldSet tokenizedLine = lineTokenizer.tokenize(line);
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