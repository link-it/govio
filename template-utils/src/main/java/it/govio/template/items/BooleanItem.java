package it.govio.template.items;

import java.util.HashMap;
import java.util.Map;

import it.govio.template.exception.TemplateValidationException;


public class BooleanItem extends Item<Boolean> {
	
	public BooleanItem(int position, String name, boolean mandatory) {
		super(name, mandatory, position);
	}
	
	@Override
	public Boolean getValue(String stringValue) throws TemplateValidationException {
		super.validateValue(stringValue);
		if (!stringValue.equalsIgnoreCase("true") && !stringValue.equalsIgnoreCase("false") && !stringValue.isBlank())
			throw new TemplateValidationException(String.format("Il valore %s del campo %s non è un booleano", stringValue, name));
		return Boolean.parseBoolean(stringValue);
	}

	@Override
	public Map<String, String> getPlaceholderValues(String stringValue) throws TemplateValidationException {
		Boolean value = getValue(stringValue);
		
		Map<String, String> valuesMap = new HashMap<>();
		if(value == null) return valuesMap;
		
		valuesMap.put(name, value.toString());
		return valuesMap;
	}
}