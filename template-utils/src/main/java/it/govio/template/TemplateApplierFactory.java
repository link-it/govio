package it.govio.template;

import java.util.HashMap;
import java.util.Map;

import it.govio.template.items.*;

public class TemplateApplierFactory {
	
	private TemplateApplierFactory() { }
	
	public static CsvTemplateApplier buildCSVTemplateApplier(Template template) {
		String message = template.getMessageBody();
		String subject = template.getSubject();
		
		return CsvTemplateApplier.builder()
				.items(getItems(template))
				.message(message)
				.subject(subject)
				.build();
	}
	
	public static BasicTemplateApplier buildBasicTemplateApplier(Template template) {
		String message = template.getMessageBody();
		String subject = template.getSubject();
		
		return BasicTemplateApplier.builder()
				.items(getItems(template))
				.message(message)
				.subject(subject)
				.build();
	}
	
	
	public static Map<String, Item<?>> getItems(Template template) {

		boolean hasDueDate = template.isHasDueDate();
		boolean hasPayment = template.isHasPayment();

		Map<String, Item<?>> items = new HashMap<>();

		int index = 0;

		// CAMPI FISSI
		Item<?> taxcode = new StringItem(index, ItemKeys.TAXCODE.toString(), true, "[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]");
		items.put(ItemKeys.TAXCODE.toString(), taxcode);

		Item<?> expeditionDateTime = new DateTimeItem(++index, ItemKeys.EXPEDITIONDATE.toString(), true);
		items.put(ItemKeys.EXPEDITIONDATE.toString(), expeditionDateTime);
		
		if(hasDueDate) {
			Item<?> dueDateItem = new DateTimeItem(++index, ItemKeys.DUEDATE.toString(), true);
			items.put(ItemKeys.DUEDATE.toString(), dueDateItem);
		}
		if(hasPayment) {
			Item<?> noticeNumberItem = new StringItem(++index, ItemKeys.NOTICENUMBER.toString(), true, "^[0123][0-9]{17}$");
			items.put(ItemKeys.NOTICENUMBER.toString(), noticeNumberItem);

			Item<?> amountItem = new LongItem(++index, ItemKeys.AMOUNT.toString(), true);
			items.put(ItemKeys.AMOUNT.toString(), amountItem);

			Item<?> invalidAfterDueDateItem = new BooleanItem(++index, ItemKeys.INVALIDAFTERDUEDATE.toString(), false);
			items.put(ItemKeys.INVALIDAFTERDUEDATE.toString(), invalidAfterDueDateItem);

			Item<?> payeeItem = new StringItem(++index, ItemKeys.PAYEE.toString(), false, "^[0-9]{11}$");
			items.put(ItemKeys.PAYEE.toString(), payeeItem);
		}

		// CAMPI Aggiuntivi
		for(Placeholder placeholder : template.getPlaceholders()) {
			switch(placeholder.getType()) {
			case DATE:
			{
				Item<?> item = new DateItem(placeholder.getPosition() + index, placeholder.getName(), placeholder.isMandatory());
				items.put(item.getName(), item);
			}
			break;
			case DATETIME:
			{
				Item<?> item = new DateTimeItem(placeholder.getPosition() + index, placeholder.getName(), placeholder.isMandatory());
				items.put(item.getName(), item);
			}
			break;
			case STRING:
			{
				Item<?> item = new StringItem(placeholder.getPosition() + index, placeholder.getName(), placeholder.isMandatory(), placeholder.getPattern());
				items.put(item.getName(), item);
			}
			break;
			}
		}
		return items;

	}
}
