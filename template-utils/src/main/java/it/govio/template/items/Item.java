/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
		if(values[position].isBlank())
			return null;
		return values[position];
	}
	
}
