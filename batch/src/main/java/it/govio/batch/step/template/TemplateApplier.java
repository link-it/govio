package it.govio.batch.step.template;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.exception.TemplateValidationException;
import lombok.Builder;

@Builder
public class TemplateApplier {

	private String message;
	private String subject;
	private Map<String, CsvItem> items;

	public GovioMessageEntity buildGovioMessageEntity(String record) {
		String[] splitted = record.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

		String taxcode = getTaxcode(splitted);
		LocalDateTime dueDate = getDuedate(splitted);
		String noticeNumber = getNoticeNumber(splitted);;
		String payee = getPayee(splitted);
		Integer amount = getAmount(splitted);
		boolean invalidAfterDueDate = getInvalidAfterDueDate(splitted);
		LocalDateTime scheduledExpeditionDate = getScheduledExpeditionDate(splitted);
		
		Map<String, String> placeholderValues = new HashMap<>();
		for(CsvItem item : items.values()) {
			placeholderValues.putAll(item.getPlaceholderValues(splitted));
		}
		StringSubstitutor substitutor = new StringSubstitutor(placeholderValues);
		
		String message = getMessage(substitutor);
		String subject = getSubject(substitutor);

		return GovioMessageEntity.builder()
				.amount(amount)
				.creationDate(LocalDateTime.now())
				.dueDate(dueDate)
				.email(null)
				.invalidAfterDueDate(invalidAfterDueDate)
				.markdown(message)
				.noticeNumber(noticeNumber)
				.payee(payee)
				.scheduledExpeditionDate(scheduledExpeditionDate)
				.status(Status.SCHEDULED)
				.subject(subject)
				.taxcode(taxcode)
				.build();
	}

	private LocalDateTime getScheduledExpeditionDate(String[] splitted) {
		DateTimeCsvItem csvItem = (DateTimeCsvItem) items.get( CsvItem.Keys.EXPEDITIONDATE.toString());
		if(csvItem != null)
			return csvItem.getDateValue(splitted);
		else 
			throw new TemplateValidationException("La data di spedizione è obbligatoria");
	}

	private Boolean getInvalidAfterDueDate(String[] splitted) {
		BooleanCsvItem csvItem = (BooleanCsvItem) items.get( CsvItem.Keys.EXPEDITIONDATE.toString());
		return csvItem != null ? csvItem.getBooleanValue(splitted) : null;
	}

	private Integer getAmount(String[] splitted) {
		IntegerCsvItem csvItem = (IntegerCsvItem) items.get( CsvItem.Keys.AMOUNT.toString());
		return csvItem != null ? csvItem.getIntegerValue(splitted) : null;
	}

	private String getPayee(String[] splitted) {
		StringCsvItem csvItem = (StringCsvItem) items.get( CsvItem.Keys.PAYEE.toString());
		return csvItem != null ? csvItem.getValue(splitted) : null;
	}

	private String getNoticeNumber(String[] splitted) {
		StringCsvItem csvItem = (StringCsvItem) items.get( CsvItem.Keys.NOTICENUMBER.toString());
		return csvItem != null ? csvItem.getValue(splitted) : null;
	}

	private String getSubject(StringSubstitutor substitutor) {
		return substitutor.replace(subject);
	}

	private String getMessage(StringSubstitutor substitutor) {
		return substitutor.replace(message);
	}

	private LocalDateTime getDuedate(String[] splitted) {
		DateTimeCsvItem csvItem = (DateTimeCsvItem) items.get( CsvItem.Keys.DUEDATE.toString());
		return csvItem != null ? csvItem.getDateValue(splitted) : null;
	}

	private String getTaxcode(String[] splitted) {
		StringCsvItem csvItem = (StringCsvItem) items.get( CsvItem.Keys.TAXCODE.toString());
		if(csvItem != null)
			return csvItem.getValue(splitted);
		else 
			throw new TemplateValidationException("Il codice fiscale del destinatario è obbligatorio");
	}


}
