package it.govio.template.test;

import it.govio.template.BaseMessage;
import it.govio.template.BasicTemplateApplier;
import it.govio.template.Placeholder;
import it.govio.template.Placeholder.Type;
import it.govio.template.Template;
import it.govio.template.TemplateApplierFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


class UC2_BasicTemplateTest {
	
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
	void UC_1_1_STATIC_OK(){
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
	void UC_1_2_CUSTOM_STRING_PLACEHOLDER(){
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

}
