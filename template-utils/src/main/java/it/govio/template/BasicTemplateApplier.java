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
package it.govio.template;

import java.util.HashMap;
import java.util.Map;

import it.govio.template.exception.TemplateFreemarkerException;
import it.govio.template.exception.TemplateValidationException;
import it.govio.template.items.Item;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class BasicTemplateApplier extends TemplateApplier {
	
	public String getMarkdown(BaseMessage message, Map<String, String> placeholders) throws TemplateValidationException, TemplateFreemarkerException {
		return getMessage(getPlaceholderValues(message, placeholders));
	}
	
	public String getSubject(BaseMessage message, Map<String, String> placeholders) throws TemplateValidationException, TemplateFreemarkerException {
		return getSubject(getPlaceholderValues(message, placeholders));
	}
	
	private Map<String, String> getPlaceholderValues(BaseMessage message, Map<String, String> placeholders) throws TemplateValidationException {
		if(placeholders == null)
			placeholders = new HashMap<>();
		if(message.getAmount() != null)
			placeholders.put(ItemKeys.AMOUNT.toString(), message.getAmount().toString());
		if(message.getDueDate() != null)
			placeholders.put(ItemKeys.DUEDATE.toString(), message.getDueDate().toLocalDateTime().toString());
		if(message.getScheduledExpeditionDate() != null)
			placeholders.put(ItemKeys.EXPEDITIONDATE.toString(), message.getScheduledExpeditionDate().toLocalDateTime().toString());
		if(message.getInvalidAfterDueDate() != null)
			placeholders.put(ItemKeys.INVALIDAFTERDUEDATE.toString(), message.getInvalidAfterDueDate().toString());
		if(message.getNoticeNumber() != null)
			placeholders.put(ItemKeys.NOTICENUMBER.toString(), message.getNoticeNumber());
		if(message.getPayee() != null)
			placeholders.put(ItemKeys.PAYEE.toString(), message.getPayee());
		if(message.getTaxcode() != null)
			placeholders.put(ItemKeys.TAXCODE.toString(), message.getTaxcode());

		Map<String, String> placeholderValues = new HashMap<>();
		for(Item<?> item : items.values()) {
			item.validateValue(placeholders.get(item.getName()));
			placeholderValues.putAll(item.getPlaceholderValues(placeholders.get(item.getName())));
		}
		return placeholderValues;
	}
	
}
