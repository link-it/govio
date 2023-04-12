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
import java.util.regex.Pattern;

import it.govio.template.exception.TemplateValidationException;

public class StringItem extends Item<String>{
	
	private String pattern;
	
	public StringItem(int position, String name, boolean mandatory, String pattern) {
		super(name, mandatory, position);
		this.pattern = pattern;
	}
	
	@Override
	public String getValue(String value) {
		super.validateValue(value);
		// Se c'e' un pattern ed un valore, controllo che sia compatibile.
		if(pattern != null && !Pattern.matches(pattern, value)) {
			throw new TemplateValidationException(String.format("Il valore %s del placeholder %s non rispetta il pattern %s", value, name, pattern));
		}
		return value;
	}
	
	@Override
	public Map<String, String> getPlaceholderValues(String value) {
		Map<String, String> valuesMap = new HashMap<>();
		
		if(value == null) return valuesMap;
				
		valuesMap.put(name, value);
		valuesMap.put(name + "_lower", value.toLowerCase());
		valuesMap.put(name + "_upper", value.toUpperCase());
		return valuesMap;
	}
}
