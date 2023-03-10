package it.govio.template;

import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import it.govio.template.exception.TemplateValidationException;
import it.govio.template.items.Item;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class TemplateApplier {
	
	protected String message;
	protected String subject;
	protected Map<String, Item<?>> items;
	
	protected String getSubject(StringSubstitutor substitutor) {
		String subjectString = substitutor.replace(subject);
		if (subjectString.length() < 10) {
			throw new TemplateValidationException(String.format("Il subject di dimensione %d, è minore della dimensione minima ammessa.", subjectString.length()));
		}
		if  (subjectString.length() > 120) 
			throw new TemplateValidationException(String.format("Il subject di dimensione %d, supera la dimensione massima ammessa.", subjectString.length()));
		return substitutor.replace(subject);
	}

	protected String getMessage(StringSubstitutor substitutor) {
		String markdown = substitutor.replace(message);
		if (markdown.length() < 80)
			throw new TemplateValidationException(String.format("Il markdown di dimensione %d, è minore della dimensione minima ammessa.", markdown.length()));
		if (markdown.length() > 10000)
			throw new TemplateValidationException(String.format("Il markdown di dimensione %d, supera la dimensione massima ammessa.", markdown.length()));
		return markdown;
	}

}
