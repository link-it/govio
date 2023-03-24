package it.govio.template.items;


import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.govio.template.exception.TemplateValidationException;


public class LongItem extends Item<Long>{
	
	private Logger logger = LoggerFactory.getLogger(LongItem.class);
	
	public LongItem(int position, String name, boolean mandatory) {
		super(name, mandatory, position);
	}
	
	@Override
	public Long getValue(String stringValue) throws TemplateValidationException {
		super.validateValue(stringValue);
		
		// Se c'e' un valore, controllo che sia compatibile.
		if(stringValue != null && !stringValue.isBlank()) {
			try {
				return Long.parseLong(stringValue);
			} catch (NumberFormatException e) {
				logger.debug("Validazione del numero fallita: {}", e.getMessage());
				throw new TemplateValidationException(String.format("Il valore %s del placeholder %s non presenta un numero valido.", stringValue, name));
			}
		}
		return null;
	}

	@Override
	public Map<String, String> getPlaceholderValues(String stringValue) throws TemplateValidationException {
		Long value = getValue(stringValue);
		
		Map<String, String> valuesMap = new HashMap<>();
		if(value == null) return valuesMap;
		
		Double truncatedDouble = BigDecimal.valueOf(value)
			    .setScale(2)
			    .doubleValue();
		NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.ITALY);
		valuesMap.put(name, value.toString());
		valuesMap.put(name + ".currency", formatter.format(truncatedDouble));
		return valuesMap;
	}
}
