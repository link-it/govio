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
package it.govio.template.test;

import it.govio.template.BaseMessage;
import it.govio.template.BasicTemplateApplier;
import it.govio.template.Placeholder;
import it.govio.template.Placeholder.Type;
import it.govio.template.Template;
import it.govio.template.TemplateApplierFactory;
import it.govio.template.exception.TemplateFreemarkerException;
import it.govio.template.exception.TemplateValidationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemarker.template.TemplateException;

import static org.junit.jupiter.api.Assertions.assertEquals;


class UC3_FreemarkerTemplateTest {
	
	private BaseMessage getDefaultMessage() {
		return BaseMessage.builder()
				.amount(100l)
				.dueDate(LocalDateTime.of(2050, 12, 31, 12, 0, 0))
				.email("s.nakamoto@xxxx.xx")
				.invalidAfterDueDate(true)
				.noticeNumber("301000001234500000")
				.payee("01234567890")
				.scheduledExpeditionDate(LocalDateTime.of(2050, 12, 31, 12, 0, 0))
				.taxcode("AAAAAA00A00A000A")
				.build();
	}
	
	@Test
	@DisplayName("template con messaggio statico")
	void UC_1_1_STATIC_OK() throws IOException, TemplateException, TemplateValidationException, TemplateFreemarkerException {
		List<Placeholder> placeholders = new ArrayList<> ();
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("Il Comune di Empoli le dice hello, il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();
		
		BasicTemplateApplier templateApplier = TemplateApplierFactory.buildBasicTemplateApplier(template);
		BaseMessage message = getDefaultMessage();
		assertEquals(template.getMessageBody(), templateApplier.getMarkdown(message, null));
		assertEquals(template.getSubject(), templateApplier.getSubject(message, null));
	}
	
	@Test
	@DisplayName("Placeholder string")
	void UC_1_2_CUSTOM_STRING_PLACEHOLDER() throws IOException, TemplateException, TemplateValidationException, TemplateFreemarkerException {
		List<Placeholder> placeholders = new ArrayList<> ();
		Placeholder fullname = Placeholder.builder()
				.mandatory(true)
				.name("full_name")
				.type(Type.STRING)
				.build();
		placeholders.add(fullname);
		
		
		
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("Il Comune di Empoli le dice hello, ${full_name}, il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri ${full_name}")
				.placeholders(placeholders)
				.build();
		
		BasicTemplateApplier templateApplier = TemplateApplierFactory.buildBasicTemplateApplier(template);
		BaseMessage message = getDefaultMessage();
		Map<String, String> values = new HashMap<>();
		values.put(fullname.getName(), "Satoshi Nakamoto");
		assertEquals("Il Comune di Empoli le dice hello, Satoshi Nakamoto, il markdown deve essere di almeno 80 caratteri", templateApplier.getMarkdown(message, values));
		assertEquals("hello, il subject deve essere almeno dieci caratteri Satoshi Nakamoto", templateApplier.getSubject(message, values));
	}

	@Test
	@DisplayName("Placeholder string")
	void UC_1_3_IFPATTERN() throws IOException, TemplateException, TemplateValidationException, TemplateFreemarkerException {
		List<Placeholder> placeholders = new ArrayList<> ();
		Placeholder fullname = Placeholder.builder()
				.mandatory(true)
				.name("full_name")
				.type(Type.STRING)
				.build();
		placeholders.add(fullname);
		
		
		{
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("Il Comune di Empoli le dice hello, <#if (full_name?length > 5) >aaaa<#else>bbbb</#if>, il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri ${full_name}")
				.placeholders(placeholders)
				.build();
		
		BasicTemplateApplier templateApplier = TemplateApplierFactory.buildBasicTemplateApplier(template);
		BaseMessage message = getDefaultMessage();
		Map<String, String> values = new HashMap<>();
		values.put(fullname.getName(), "Satoshi Nakamoto");
		assertEquals("Il Comune di Empoli le dice hello, aaaa, il markdown deve essere di almeno 80 caratteri", templateApplier.getMarkdown(message, values));
		assertEquals("hello, il subject deve essere almeno dieci caratteri Satoshi Nakamoto", templateApplier.getSubject(message, values));
		}
		{
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("Il Comune di Empoli le dice hello, <#if (full_name?length<5) >aaaa<#else>bbbb</#if>, il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri ${full_name}")
				.placeholders(placeholders)
				.build();
		
		BasicTemplateApplier templateApplier = TemplateApplierFactory.buildBasicTemplateApplier(template);
		BaseMessage message = getDefaultMessage();
		Map<String, String> values = new HashMap<>();
		values.put(fullname.getName(), "Satoshi Nakamoto");
		assertEquals("Il Comune di Empoli le dice hello, bbbb, il markdown deve essere di almeno 80 caratteri", templateApplier.getMarkdown(message, values));
		assertEquals("hello, il subject deve essere almeno dieci caratteri Satoshi Nakamoto", templateApplier.getSubject(message, values));
		}
	}
}
