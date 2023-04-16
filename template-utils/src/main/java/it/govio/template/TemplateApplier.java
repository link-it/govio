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
package it.govio.template;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import it.govio.template.exception.TemplateFreemarkerException;
import it.govio.template.exception.TemplateValidationException;
import it.govio.template.items.Item;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class TemplateApplier {

	protected String message;
	protected String subject;

	protected Map<String, Item<?>> items;

	protected String getSubject(Map<String, String> placeholderValues) {
		String subjectString = applyFreemarkerTemplate(subject, placeholderValues);
		if (subjectString.length() < 10) {
			throw new TemplateValidationException(String.format("Il subject di dimensione %d, è minore della dimensione minima ammessa di 10.", subjectString.length()));
		}
		if  (subjectString.length() > 120) 
			throw new TemplateValidationException(String.format("Il subject di dimensione %d, supera la dimensione massima ammessa di 120.", subjectString.length()));
		return subjectString;
	}

	protected String getMessage(Map<String, String> placeholderValues) {
		String markdown = applyFreemarkerTemplate(message, placeholderValues);
		if (markdown.length() < 80)
			throw new TemplateValidationException(String.format("Il markdown di dimensione %d, è minore della dimensione minima ammessa di 80.", markdown.length()));
		if (markdown.length() > 10000)
			throw new TemplateValidationException(String.format("Il markdown di dimensione %d, supera la dimensione massima ammessa di 10000.", markdown.length()));
		return markdown;
	}

	private String applyFreemarkerTemplate(String template, Map<String, String> placeholderValues) {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);
		cfg.setFallbackOnNullLoopVariable(false);
		cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

		Logger logger = LoggerFactory.getLogger(TemplateApplier.class);
		if(logger.isDebugEnabled()) {
			logger.debug("Applicazione del freemarker: ");
			for (Entry<String, String> entry : placeholderValues.entrySet()) {
				logger.debug(entry.toString());
			}
		}
		try {
			freemarker.template.Template t = new freemarker.template.Template("freemarker", new StringReader(template), cfg);
			Writer out = new StringWriter();
			t.process(placeholderValues, out);
			return out.toString();
		} catch (TemplateException | IOException e) {
			throw new TemplateFreemarkerException(e);
		}
	}
}
