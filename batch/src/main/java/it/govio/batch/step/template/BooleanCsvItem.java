package it.govio.batch.step.template;

import java.util.HashMap;
import java.util.Map;

import it.govio.batch.exception.TemplateValidationException;

public class BooleanCsvItem extends CsvItem{
	
	public BooleanCsvItem(int index, String name, boolean mandatory) {
		super(index, name, mandatory);
	}
	
	public Boolean getBooleanValue(String[] values) throws TemplateValidationException {
		String value = getValue(values);
		validateValue(value);
		return Boolean.parseBoolean(value);
	}

	@Override
	public Map<String, String> getPlaceholderValues(String[] values) throws TemplateValidationException {
		Boolean value = getBooleanValue(values);
		
		Map<String, String> valuesMap = new HashMap<String, String>();
		valuesMap.put(name, value.toString());
		return valuesMap;
	}
}
