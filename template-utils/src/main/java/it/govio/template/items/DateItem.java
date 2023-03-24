package it.govio.template.items;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.govio.template.exception.TemplateValidationException;


public class DateItem extends Item<LocalDate>{
	
	private Logger logger = LoggerFactory.getLogger(DateItem.class);
	protected static DateTimeFormatter baseFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALY);
	protected static DateTimeFormatter longFormat = DateTimeFormatter.ofPattern("E dd M yyyy", Locale.ITALY);
	
	public DateItem(int position, String name, boolean mandatory) {
		super(name, mandatory, position);
	}
	
	@Override
	public LocalDate getValue(String stringValue) throws TemplateValidationException {
		validateValue(stringValue);
		// Se c'e' un valore, controllo che sia compatibile.
		if(stringValue != null && !stringValue.isBlank()) {
			try {
				return LocalDate.parse(stringValue);
			} catch (DateTimeParseException e) {
				logger.debug("Validazione della data fallita: {}", e.getMessage());
				throw new TemplateValidationException(String.format("Il valore %s del placeholder %s non presenta una data valida.", stringValue, name));
			}
		}
		return null;
	}

	@Override
	public Map<String, String> getPlaceholderValues(String stringValue) throws TemplateValidationException {
		LocalDate value = getValue(stringValue);
		
		Map<String, String> valuesMap = new HashMap<>();
		if(value == null) return valuesMap;
		
		valuesMap.put(name, value.format(baseFormat));
		valuesMap.put(name + ".verbose", value.format(longFormat));
		return valuesMap;
	}
}
