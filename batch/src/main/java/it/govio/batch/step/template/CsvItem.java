package it.govio.batch.step.template;

import java.util.Map;

import it.govio.batch.exception.TemplateValidationException;
import lombok.Getter;

@Getter
public abstract class CsvItem {
	
	public enum Keys {
		TAXCODE("taxcode"), 
		EXPEDITIONDATE("expedition_date"), 
		DUEDATE("due_date"), 
		NOTICENUMBER("notice_number"), 
		AMOUNT("amount"), 
		INVALIDAFTERDUEDATE("invalid_after_due_date"), 
		PAYEE("payee");
		
		private String string;

		Keys(String string) {
			this.string = string;
		}  
		
		@Override
		public String toString() {
			return string;
		}
	}
	
	protected int index;
	protected String name;
	protected boolean mandatory;
	
	protected CsvItem(int index, String name, boolean mandatory) {
		this.name = name;
		this.mandatory = mandatory;
		this.index = index;
	}
	
	protected String getValue(String[] values) {
		if(values.length <= index)
			throw new TemplateValidationException(String.format("Numero di valori inferiore a quanto richiesto dal template. Assente il valore %s in posizione %d", name, index));
		return values[index];
	}
	
	protected void validateValue(String value) throws TemplateValidationException {
		if(mandatory && (value == null || value.isBlank()))
			throw new TemplateValidationException(String.format("Il valore %s in posizione %d è obbligatorio", name, index));
	}
	
	public abstract Map<String, String> getPlaceholderValues(String[] values) throws TemplateValidationException;
	  
}
