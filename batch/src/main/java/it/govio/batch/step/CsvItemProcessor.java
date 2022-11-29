package it.govio.batch.step;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.exception.TemplateValidationException;
import it.govio.batch.step.beans.CsvItem;
import it.govio.batch.step.beans.GovioMessage;
import it.govio.batch.step.beans.CsvPaymentItem;

@Component
public class CsvItemProcessor implements ItemProcessor<CsvItem, GovioMessage> {
	/**
	 * Processor che prende una item del CSV e lo trasforma
	 * in un GovioMessageEntity in base al template associato al
	 * file
	 */
	public GovioMessage process(CsvItem item) {

		GovioMessage govioMessage = new GovioMessage();
		GovioFileMessageEntity	govioFileMessageEntity = GovioFileMessageEntity.builder()
		.line_number(item.getRowNumber())
		.line_record(item.getRawData())
		.build();
		
		try {
			validation(item);
		} catch (TemplateValidationException e) {
			govioFileMessageEntity.setError(e.getMessage());
			
			return GovioMessage.builder().govioFileMessageEntity(govioFileMessageEntity)
					.govioFileMessageEntity(govioFileMessageEntity)
					.govioMessageEntity(null)
					.build(); //TODO
		}
		
		// Applicare il template ai valori dell'item
		StringSubstitutor sub = buildMap(item);
		String markdown = sub.replace(item.getServiceInstance().getGovioTemplate().getMessageBody());
		
		sub = buildMap(item);
		String subject = sub.replace(item.getServiceInstance().getGovioTemplate().getSubject());
		
		// Costruire il message entity
		GovioMessageEntity messageEntity = GovioMessageEntity.builder()
				.govioServiceInstance(item.getServiceInstance())
                .markdown(markdown)
                .subject(subject)
                .taxcode(item.getTaxcode())
                .status(Status.SCHEDULED)
                .creationDate(LocalDateTime.now())
                .scheduledExpeditionDate(item.getScheduledExpeditionDate())
                .build();		
		
		govioFileMessageEntity.setGovioMessage(messageEntity);

		govioMessage.setGovioMessageEntity(messageEntity);
		govioMessage.setGovioFileMessageEntity(govioFileMessageEntity);
		return govioMessage;
	}
	
	public GovioMessage process(CsvPaymentItem item) {
		try {
			validation(item);
		} catch (TemplateValidationException e) {
			GovioFileMessageEntity govioFileMessageEntity = new GovioFileMessageEntity();
			govioFileMessageEntity.setError(e.getMessage());
			// perchè costruire un messaggio, non va restituita una eccezione?
			return GovioMessage.builder().govioFileMessageEntity(govioFileMessageEntity)
					.govioFileMessageEntity(govioFileMessageEntity)
					.govioMessageEntity(null)
					.build(); //TODO
		}

		GovioMessage message = process((CsvItem)item);
		message.getGovioMessageEntity().setExpeditionDate(item.getScheduledExpeditionDate());
    	message.getGovioMessageEntity().setAmount(item.getAmount());
    	message.getGovioMessageEntity().setNoticeNumber(item.getNoticeNumber());
    	message.getGovioMessageEntity().setPayee(item.getPayeeTaxcode());
    	message.getGovioMessageEntity().setInvalidAfterDueDate(item.isInvalidAfterDueDate());
    return message;
	}	
	
	StringSubstitutor buildMap(CsvItem item) {
		Map<String, String> valuesMap = new HashMap<>();
		valuesMap.put("taxcode", item.getTaxcode());
		LocalDateTime date = item.getScheduledExpeditionDate().truncatedTo(ChronoUnit.MINUTES);
		String[] parts = date.toString().split("T");
		valuesMap.put("expedition_date.date", parts[0]);
		valuesMap.put("expedition_date.time", parts[1]);
		return new StringSubstitutor(valuesMap);
	}

	
	StringSubstitutor buildMap(CsvPaymentItem item) {
		Map<String, String> valuesMap = new HashMap<>();
		
		valuesMap.put("taxcode", item.getTaxcode());
		LocalDateTime date = item.getScheduledExpeditionDate().truncatedTo(ChronoUnit.MINUTES);
		String[] parts = date.toString().split("T");
		valuesMap.put("expedition_date.date", parts[0]);
		valuesMap.put("expedition_date.time", parts[1]);

		valuesMap.put("notice_number", item.getNoticeNumber());
		valuesMap.put("amount", item.getAmount()+"");
		valuesMap.put("payee_taxcode", item.getPayeeTaxcode());
		
		// si assume che il reader abbia effettuato i controlli sulla presenza dei dati opzionali e popolato correttamente i campi relativi
		// solo se il template has_due_date
		if (item.getServiceInstance().getGovioTemplate().getHasDueDate()) {
			date = item.getDueDate().truncatedTo(ChronoUnit.MINUTES);
			parts = date.toString().split("T");
			valuesMap.put("due_date.date", parts[0]);
			valuesMap.put("due_date.time", parts[1]);
		}
		// solo se il template has_payment e due_date
		if (item.getServiceInstance().getGovioTemplate().getHasDueDate() && item.getServiceInstance().getGovioTemplate().getHasPayment()) {
		if (item.isInvalidAfterDueDate()) valuesMap.put("invalid_after_due_date","true");
		else valuesMap.put("invalid_after_due_date","false");
		}
		return new StringSubstitutor(valuesMap);
	}
	

	void validation(CsvItem item) throws TemplateValidationException {
		// Qui valido che:
		// - I campi obbligatori ci siano
		if (item.getTaxcode()==null) throw new TemplateValidationException("taxcode null");
		if (item.getScheduledExpeditionDate()==null) throw new TemplateValidationException("expedition date null");
		// - Se un campo ha un pattern impostato sia rispettato
		String taxcode = item.getTaxcode();
		Pattern pattern = Pattern.compile("[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]");
		Matcher matcher = pattern.matcher(taxcode);
		if (!matcher.find())
			throw new TemplateValidationException("taxcode non valido");
		// se le date sono valide
	}
	
	void validation(CsvPaymentItem item) throws TemplateValidationException {
		validation((CsvItem)item);
		// - I campi obbligatori ci siano
		if (item.getNoticeNumber()==null) throw new TemplateValidationException("notice number null");
		// - Se un campo ha un pattern impostato sia rispettato
		Pattern pattern = Pattern.compile("^[0123]\\d{17}$");
		Matcher matcher = pattern.matcher(item.getNoticeNumber());
		if (!matcher.matches()) {
			throw new TemplateValidationException("notice number non valido");
		}
		if (item.getAmount()>9999999999L){
			throw new TemplateValidationException("amount non valido");
		}
		pattern = Pattern.compile("\\d{11}");
		matcher = pattern.matcher(item.getPayeeTaxcode());
		if (!matcher.find()) {
			throw new TemplateValidationException("payee taxcode non valido");
		}
	}
}