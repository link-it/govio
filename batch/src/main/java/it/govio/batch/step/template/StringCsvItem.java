package it.govio.batch.step.template;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import it.govio.batch.exception.TemplateValidationException;

public class StringCsvItem extends CsvItem{
	
	private String pattern;
	
	public StringCsvItem(int index, String name, boolean mandatory, String pattern) {
		super(index, name, mandatory);
		this.pattern = pattern;
	}
	
	@Override
	protected void validateValue(String value) throws TemplateValidationException {
		super.validateValue(value);
		
		// Se c'e' un pattern ed un valore, controllo che sia compatibile.
		if(pattern != null && value != null && !value.isBlank() && !Pattern.matches(pattern, value)) {
			throw new TemplateValidationException(String.format("Il valore %s del campo %s non rispetta il pattern %s", value, name, pattern));
		}
	}
	
	@Override
	public String getValue(String[] values) throws TemplateValidationException {
		String value = super.getValue(values);
		validateValue(value);
		return value;
	}

	@Override
	public Map<String, String> getPlaceholderValues(String[] values) throws TemplateValidationException {
		String value = getValue(values);
		validateValue(value);
		Map<String, String> valuesMap = new HashMap<>();
		valuesMap.put(name, value);
		valuesMap.put(name + ".lower", value.toLowerCase());
		valuesMap.put(name + ".upper", value.toUpperCase());
		return valuesMap;
	}
}
