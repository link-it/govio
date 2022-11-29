package it.govio.batch.step.template;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.govio.batch.exception.TemplateValidationException;
import it.govio.batch.step.FileReadTasklet;

public class DateTimeCsvItem extends CsvItem{
	
	private Logger logger = LoggerFactory.getLogger(FileReadTasklet.class);
	protected final static DateTimeFormatter baseFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALY);
	protected final static DateTimeFormatter longFormat = DateTimeFormatter.ofPattern("E dd M yyyy 'alle ore' HH:mm", Locale.ITALY);
	protected final static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALY);

	public DateTimeCsvItem(int index, String name, boolean mandatory) {
		super(index, name, mandatory);
	}
	
	@Override
	protected void validateValue(String value) throws TemplateValidationException {
		super.validateValue(value);
		
		// Se c'e' un valore, controllo che sia compatibile.
		if(value != null && !value.isBlank()) {
			try {
				LocalDateTime.parse(value);
			} catch (DateTimeParseException e) {
				logger.debug("Validazione della data con ora fallita: {}", e.getMessage());
				throw new TemplateValidationException(String.format("Il valore {} del campo {} non presenta una data con ora valida.", value, name));
			}
		}
	}
	
	public LocalDateTime getDateValue(String[] values) throws TemplateValidationException {
		String value = getValue(values);
		validateValue(value);
		return LocalDateTime.parse(value);
	}

	@Override
	public Map<String, String> getPlaceholderValues(String[] values) throws TemplateValidationException {
		LocalDateTime value = getDateValue(values);
		Map<String, String> valuesMap = new HashMap<String, String>();
		
		valuesMap.put(name, value.format(baseFormat));
		valuesMap.put(name + ".verbose", value.format(longFormat));
		valuesMap.put(name + ".date", value.format(DateCsvItem.baseFormat));
		valuesMap.put(name + ".date.verbose", value.format(DateCsvItem.longFormat));
		valuesMap.put(name + ".time", value.format(timeFormat));
		return valuesMap;
	}
}
