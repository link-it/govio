package it.govio.batch.step.template;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.govio.batch.exception.TemplateValidationException;

public class DateCsvItem extends CsvItem{
	
	private Logger logger = LoggerFactory.getLogger(DateCsvItem.class);
	protected static DateTimeFormatter baseFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALY);
	protected static DateTimeFormatter longFormat = DateTimeFormatter.ofPattern("E dd M yyyy", Locale.ITALY);
	
	public DateCsvItem(int index, String name, boolean mandatory) {
		super(index, name, mandatory);
	}
	
	@Override
	protected void validateValue(String value) throws TemplateValidationException {
		super.validateValue(value);
		
		// Se c'e' un valore, controllo che sia compatibile.
		if(value != null && !value.isBlank()) {
			try {
				LocalDate.parse(value);
			} catch (DateTimeParseException e) {
				logger.debug("Validazione della data fallita: {}", e.getMessage());
				throw new TemplateValidationException(String.format("Il valore {} del campo {} non presenta una data valida.", value, name));
			}
		}
	}
	
	public LocalDate getDateValue(String[] values) throws TemplateValidationException {
		String value = getValue(values);
		validateValue(value);
		return LocalDate.parse(value);
	}

	@Override
	public Map<String, String> getPlaceholderValues(String[] values) throws TemplateValidationException {
		LocalDate value = getDateValue(values);
		Map<String, String> valuesMap = new HashMap<String, String>();
		
		valuesMap.put(name, value.format(baseFormat));
		valuesMap.put(name + ".verbose", value.format(longFormat));
		return valuesMap;
	}
}
