package it.govio.template.items;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import it.govio.template.exception.TemplateValidationException;

public class StringItem extends Item<String>{
	
	private String pattern;
	
	public StringItem(int position, String name, boolean mandatory, String pattern) {
		super(name, mandatory, position);
		this.pattern = pattern;
	}
	
	@Override
	public String getValue(String value) throws TemplateValidationException {
		super.validateValue(value);
		// Se c'e' un pattern ed un valore, controllo che sia compatibile.
		if(pattern != null && !Pattern.matches(pattern, value)) {
			throw new TemplateValidationException(String.format("Il valore %s del campo %s non rispetta il pattern %s", value, name, pattern));
		}
		return value;
	}
	
	@Override
	public Map<String, String> getPlaceholderValues(String value) throws TemplateValidationException {
		Map<String, String> valuesMap = new HashMap<>();
		
		if(value == null) return valuesMap;
				
		valuesMap.put(name, value);
		valuesMap.put(name + ".lower", value.toLowerCase());
		valuesMap.put(name + ".upper", value.toUpperCase());
		return valuesMap;
	}
}
