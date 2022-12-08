package it.govio.batch.step.template;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.exception.TemplateValidationException;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TemplateApplier {

	private String message;
	private String subject;
	private Map<String, CsvItem> items;

	public GovioMessageEntity buildGovioMessageEntity(String record) {
		CSVParser build = new CSVParserBuilder().build();
		String[] splitted;
		try {
			splitted = build.parseLine(record);
		} catch(IOException e) {
			throw new TemplateValidationException("Impossibile tokenizzare il record: " + e);
		}
		String taxcode = getTaxcode(splitted);
		LocalDateTime dueDate = getDuedate(splitted);
		String noticeNumber = getNoticeNumber(splitted);
		String payee = getPayee(splitted);
		Long amount = getAmount(splitted);
		Boolean invalidAfterDueDate = getInvalidAfterDueDate(splitted);
		LocalDateTime scheduledExpeditionDate = getScheduledExpeditionDate(splitted);
		
		Map<String, String> placeholderValues = new HashMap<>();
		for(CsvItem item : items.values()) {
			placeholderValues.putAll(item.getPlaceholderValues(splitted));
		}
		StringSubstitutor substitutor = new StringSubstitutor(placeholderValues);

		return GovioMessageEntity.builder()
				.amount(amount)
				.creationDate(LocalDateTime.now())
				.dueDate(dueDate)
				.email(null)
				.invalidAfterDueDate(invalidAfterDueDate)
				.markdown(getMessage(substitutor))
				.noticeNumber(noticeNumber)
				.payee(payee)
				.scheduledExpeditionDate(scheduledExpeditionDate)
				.status(Status.SCHEDULED)
				.subject(getSubject(substitutor))
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
		BooleanCsvItem csvItem = (BooleanCsvItem) items.get( CsvItem.Keys.INVALIDAFTERDUEDATE.toString());
		return csvItem != null ? csvItem.getBooleanValue(splitted) : null;
	}

	private Long getAmount(String[] splitted) {
		LongCsvItem csvItem = (LongCsvItem) items.get( CsvItem.Keys.AMOUNT.toString());
		return csvItem != null ? csvItem.getLongValue(splitted) : null;
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
