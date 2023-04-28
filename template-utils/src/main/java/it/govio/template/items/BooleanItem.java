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
		if (stringValue != null && !stringValue.equalsIgnoreCase("true") && !stringValue.equalsIgnoreCase("false") && !stringValue.isBlank())
			throw new TemplateValidationException(String.format("Il valore %s del placeholder %s non è un booleano", stringValue, name));
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