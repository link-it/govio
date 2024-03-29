/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
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
	
	public void validateValue(String value) throws TemplateValidationException {
		if(mandatory && (value == null))
			throw new TemplateValidationException(String.format("Il valore per il placeholder %s è obbligatorio", name));		
	}
	
	public T getValueFromCsv(String[] values) throws TemplateValidationException {
		return getValue(getStringValueFromCsv(values));
	}
	
	public String getStringValueFromCsv(String[] values) throws TemplateValidationException {
		if(values.length <= position || values[position].isBlank())
			return null;
		return values[position];
	}
	
}
