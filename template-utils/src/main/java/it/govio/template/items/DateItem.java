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


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.govio.template.exception.TemplateValidationException;


public class DateItem extends Item<LocalDate>{
	
	private Logger logger = LoggerFactory.getLogger(DateItem.class);
	protected static DateTimeFormatter baseFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALY);
	protected static DateTimeFormatter longFormat = DateTimeFormatter.ofPattern("EEEE dd LLLL yyyy", Locale.ITALY);
	
	public DateItem(int position, String name, boolean mandatory) {
		super(name, mandatory, position);
	}
	
	@Override
	public LocalDate getValue(String stringValue) throws TemplateValidationException {
		validateValue(stringValue);
		// Se c'e' un valore, controllo che sia compatibile.
		if(stringValue != null && !stringValue.isBlank()) {
			try {
				return LocalDate.parse(stringValue);
			} catch (DateTimeParseException e) {
				logger.debug("Validazione della data fallita: {}", e.getMessage());
				throw new TemplateValidationException(String.format("Il valore %s del placeholder %s non presenta una data valida.", stringValue, name));
			}
		}
		return null;
	}

	@Override
	public Map<String, String> getPlaceholderValues(String stringValue) throws TemplateValidationException {
		LocalDate value = getValue(stringValue);
		
		Map<String, String> valuesMap = new HashMap<>();
		if(value == null) return valuesMap;
		
		valuesMap.put(name, value.format(baseFormat));
		valuesMap.put(name + "_verbose", value.format(longFormat));
		return valuesMap;
	}
}
