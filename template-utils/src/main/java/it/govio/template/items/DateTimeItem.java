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


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.govio.template.exception.TemplateValidationException;



public class DateTimeItem extends Item<LocalDateTime> {
	
	private Logger logger = LoggerFactory.getLogger(DateTimeItem.class);
	protected static final DateTimeFormatter baseFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALY);
	protected static final DateTimeFormatter longFormat = DateTimeFormatter.ofPattern("E dd M yyyy 'alle ore' HH:mm", Locale.ITALY);
	protected static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALY);

	public DateTimeItem(int position, String name, boolean mandatory) {
		super(name, mandatory, position);
	}
	
	
	@Override
	public LocalDateTime getValue(String stringValue) throws TemplateValidationException {
		super.validateValue(stringValue);
		
		// Se c'e' un valore, controllo che sia compatibile.
		if(stringValue != null && !stringValue.isBlank()) {
			try {
				return LocalDateTime.parse(stringValue);
			} catch (DateTimeParseException e) {
				logger.debug("Validazione della data con ora fallita: {}", e.getMessage());
				throw new TemplateValidationException(String.format("Il valore %s del placeholder %s non presenta una data con ora valida.", stringValue, name));
			}
		}
		return null;
	}

	@Override
	public Map<String, String> getPlaceholderValues(String stringValue) throws TemplateValidationException {
		LocalDateTime value = getValue(stringValue);
		
		Map<String, String> valuesMap = new HashMap<>();
		if(value == null) return valuesMap;
		
		valuesMap.put(name, value.format(baseFormat));
		valuesMap.put(name + ".verbose", value.format(longFormat));
		valuesMap.put(name + ".date", value.format(DateItem.baseFormat));
		valuesMap.put(name + ".date.verbose", value.format(DateItem.longFormat));
		valuesMap.put(name + ".time", value.format(timeFormat));
		return valuesMap;
	}
}
