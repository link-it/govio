package it.govio.template;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;

import it.govio.template.exception.TemplateValidationException;
import it.govio.template.items.BooleanItem;
import it.govio.template.items.DateTimeItem;
import it.govio.template.items.Item;
import it.govio.template.items.LongItem;
import it.govio.template.items.StringItem;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class CsvTemplateApplier extends TemplateApplier {
	
	public Message buildMessage(String csvline) {
		CSVParser build = new CSVParserBuilder().build();
		String[] splitted;
		try {
			splitted = build.parseLine(csvline);
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
		for(Item<?> item : items.values()) {
			placeholderValues.putAll(item.getPlaceholderValues(item.getStringValueFromCsv(splitted)));
		}
		StringSubstitutor substitutor = new StringSubstitutor(placeholderValues);

		return Message.builder()
				.amount(amount)
				.dueDate(dueDate)
				.email(null)
				.invalidAfterDueDate(invalidAfterDueDate)
				.markdown(getMessage(substitutor))
				.noticeNumber(noticeNumber)
				.payee(payee)
				.scheduledExpeditionDate(scheduledExpeditionDate)
				.subject(getSubject(substitutor))
				.taxcode(taxcode)
				.build();
	}

	private LocalDateTime getScheduledExpeditionDate(String[] splitted) {
		DateTimeItem item = (DateTimeItem) items.get( ItemKeys.EXPEDITIONDATE.toString());
		if(item != null)
			return item.getValueFromCsv(splitted);
		else 
			throw new TemplateValidationException("La data di spedizione è obbligatoria");
	}

	private Boolean getInvalidAfterDueDate(String[] splitted) {
		BooleanItem item = (BooleanItem) items.get( ItemKeys.INVALIDAFTERDUEDATE.toString());
		return item != null ? item.getValueFromCsv(splitted) : null;
	}

	private Long getAmount(String[] splitted) {
		LongItem item = (LongItem) items.get( ItemKeys.AMOUNT.toString());
		return item != null ? item.getValueFromCsv(splitted) : null;
	}

	private String getPayee(String[] splitted) {
		StringItem item = (StringItem) items.get( ItemKeys.PAYEE.toString());
		return item != null ? item.getValueFromCsv(splitted) : null;
	}

	private String getNoticeNumber(String[] splitted) {
		StringItem item = (StringItem) items.get( ItemKeys.NOTICENUMBER.toString());
		return item != null ? item.getValueFromCsv(splitted) : null;
	}

	private LocalDateTime getDuedate(String[] splitted) {
		DateTimeItem item = (DateTimeItem) items.get( ItemKeys.DUEDATE.toString());
		return item != null ? item.getValueFromCsv(splitted) : null;
	}

	private String getTaxcode(String[] splitted) {
		StringItem item = (StringItem) items.get( ItemKeys.TAXCODE.toString());
		if(item != null)
			return item.getValueFromCsv(splitted);
		else 
			throw new TemplateValidationException("Il codice fiscale del destinatario è obbligatorio");
	}


}
