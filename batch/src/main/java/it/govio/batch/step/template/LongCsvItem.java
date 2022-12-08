package it.govio.batch.step.template;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.govio.batch.exception.TemplateValidationException;

public class LongCsvItem extends CsvItem{
	
	private Logger logger = LoggerFactory.getLogger(LongCsvItem.class);
	
	public LongCsvItem(int index, String name, boolean mandatory) {
		super(index, name, mandatory);
	}
	
	@Override
	protected void validateValue(String value) throws TemplateValidationException {
		super.validateValue(value);
		
		// Se c'e' un valore, controllo che sia compatibile.
		if(value != null && !value.isBlank()) {
			try {
				Long.parseLong(value);
			} catch (NumberFormatException e) {
				logger.debug("Validazione del numero fallita: {}", e.getMessage());
				throw new TemplateValidationException(String.format("Il valore %s del campo %s non presenta un numero valido.", value, name));
			}
		}
	}
	
	public Long getLongValue(String[] values) throws TemplateValidationException {
		String value = getValue(values);
		validateValue(value);
		return Long.valueOf(value);
	}

	@Override
	public Map<String, String> getPlaceholderValues(String[] values) throws TemplateValidationException {
		Long value = getLongValue(values);
		
		Double truncatedDouble = BigDecimal.valueOf(value)
			    .setScale(2)
			    .doubleValue();
		
		Map<String, String> valuesMap = new HashMap<String, String>();
		NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.ITALY);
		valuesMap.put(name, value.toString());
		valuesMap.put(name + ".currency", formatter.format(truncatedDouble));
		return valuesMap;
	}
}
