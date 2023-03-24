package it.govio.template.items;

import java.util.Map;

import it.govio.template.exception.TemplateValidationException;
import lombok.Getter;

@Getter
public abstract class Item<T> {
	
	protected String name;
	protected boolean mandatory;
	private int position;
	
	protected Item(String name, boolean mandatory, int position) {
		this.name = name;
		this.mandatory = mandatory;
		this.position = position;
	}
	
	public abstract T getValue(String stringValue) throws TemplateValidationException;
	public abstract Map<String, String> getPlaceholderValues(String stringValue) throws TemplateValidationException;
	
	protected void validateValue(String value) throws TemplateValidationException {
		if(mandatory && (value == null))
			throw new TemplateValidationException(String.format("Il valore per il placeholder %s è obbligatorio", name));		
	}
	
	public T getValueFromCsv(String[] values) throws TemplateValidationException {
		return getValue(getStringValueFromCsv(values));
	}
	
	public String getStringValueFromCsv(String[] values) {
		if(values.length <= position)
			throw new TemplateValidationException(String.format("Numero di valori inferiore a quanto richiesto dal template. Assente il valore %s in posizione %d", name, position));
		return values[position];
	}
	
}
