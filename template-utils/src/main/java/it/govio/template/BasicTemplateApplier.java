package it.govio.template;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import it.govio.template.items.Item;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class BasicTemplateApplier extends TemplateApplier {
	
	public String getMarkdown(BaseMessage message, Map<String, String> placeholders) {
		return getMessage(getStringSubstitutor(message, placeholders));
	}
	
	public String getSubject(BaseMessage message, Map<String, String> placeholders) {
		return getSubject(getStringSubstitutor(message, placeholders));
	}
	
	private StringSubstitutor getStringSubstitutor(BaseMessage message, Map<String, String> placeholders) {
		if(placeholders == null)
			placeholders = new HashMap<String,String>();
		if(message.getAmount() != null)
			placeholders.put(ItemKeys.AMOUNT.toString(), message.getAmount().toString());
		if(message.getDueDate() != null)
			placeholders.put(ItemKeys.DUEDATE.toString(), message.getDueDate().toString());
		if(message.getScheduledExpeditionDate() != null)
			placeholders.put(ItemKeys.EXPEDITIONDATE.toString(), message.getScheduledExpeditionDate().toString());
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
			placeholderValues.putAll(item.getPlaceholderValues(placeholders.get(item.getName())));
		}
		return new StringSubstitutor(placeholderValues);
	}
	
}
