package it.govio.batch.step.template;

import java.util.HashMap;
import java.util.Map;

import it.govio.batch.entity.GovioTemplateEntity;
import it.govio.batch.entity.GovioTemplatePlaceholderEntity;

public class GovioTemplateApplierFactory {
	
	public static TemplateApplier buildTemplateApplier(GovioTemplateEntity govioTemplate) {

		boolean hasDueDate = govioTemplate.getHasDueDate();
		boolean hasPayment = govioTemplate.getHasPayment();
		String message = govioTemplate.getMessageBody();
		String subject = govioTemplate.getSubject();
		Map<String, CsvItem> items = new HashMap<>();

		int index = 0;

		// CAMPI FISSI
		CsvItem taxcode = new StringCsvItem(index, CsvItem.Keys.TAXCODE.toString(), true, "[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]");
		items.put(CsvItem.Keys.TAXCODE.toString(), taxcode);

		CsvItem expeditionDateTime = new DateTimeCsvItem(++index, CsvItem.Keys.EXPEDITIONDATE.toString(), true);
		items.put(CsvItem.Keys.EXPEDITIONDATE.toString(), expeditionDateTime);

		CsvItem dueDateItem = new DateCsvItem(++index, CsvItem.Keys.DUEDATE.toString(), hasDueDate);
		items.put(CsvItem.Keys.DUEDATE.toString(), dueDateItem);

		if(hasPayment) {
			CsvItem noticeNumberItem = new StringCsvItem(++index, CsvItem.Keys.NOTICENUMBER.toString(), true, "^[0123][0-9]{17}$");
			items.put(CsvItem.Keys.NOTICENUMBER.toString(), noticeNumberItem);

			CsvItem amountItem = new LongCsvItem(++index, CsvItem.Keys.AMOUNT.toString(), true);
			items.put(CsvItem.Keys.AMOUNT.toString(), amountItem);

			CsvItem invalidAfterDueDateItem = new BooleanCsvItem(++index, CsvItem.Keys.INVALIDAFTERDUEDATE.toString(), true);
			items.put(CsvItem.Keys.INVALIDAFTERDUEDATE.toString(), invalidAfterDueDateItem);

			CsvItem payeeItem = new StringCsvItem(++index, CsvItem.Keys.PAYEE.toString(), false, "^[0-9]{11}$");
			items.put(CsvItem.Keys.PAYEE.toString(), payeeItem);
		}

		// CAMPI Aggiuntivi
		for(GovioTemplatePlaceholderEntity placeholder : govioTemplate.getGovioTemplatePlaceholders()) {
			switch(placeholder.getGovioPlaceholder().getType()) {
			case DATE:
			{
				CsvItem item = new DateCsvItem(placeholder.getIndex() + index, placeholder.getGovioPlaceholder().getName(), placeholder.isMandatory());
				items.put(item.getName(), item);
			}
			break;
			case DATETIME:
			{
				CsvItem item = new DateTimeCsvItem(placeholder.getIndex() + index, placeholder.getGovioPlaceholder().getName(), placeholder.isMandatory());
				items.put(item.getName(), item);
			}
			break;
			case STRING:
			{
				CsvItem item = new StringCsvItem(placeholder.getIndex() + index, placeholder.getGovioPlaceholder().getName(), placeholder.isMandatory(), placeholder.getGovioPlaceholder().getPattern());
				items.put(item.getName(), item);
			}
			break;
			}
		}
		
		return TemplateApplier.builder()
				.items(items)
				.message(message)
				.subject(subject)
				.build();
	}
}
