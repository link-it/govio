package it.govhub.govio.api.test.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class MessageUtils {

	public static JsonArray createPlaceHolders(JsonObject ... placeholder) {
		JsonArrayBuilder placeholders = Json.createArrayBuilder();
		
		for (int i = 0; i < placeholder.length; i++) {
			placeholders.add(placeholder[i]);
		}
		
		return placeholders.build();
	}

	public static JsonObject createPlaceHolder(String name, String value) {
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		
		if(name != null) {
			objectBuilder = objectBuilder.add("name", name);
		}
		
		if(value != null) {
			objectBuilder = objectBuilder.add("value", value);
		}
		
		return objectBuilder.build();
	}

	public static JsonObject createMessage(Long amount, String noticeNumber, Boolean invalidAfterDueDate, String payEETaxCode,
			OffsetDateTime scheduledExpeditionDate, OffsetDateTime dueDate, String taxCode, String email, JsonArray placeHolders, DateTimeFormatter dt) {

		JsonObject payment = createPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		return createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, placeHolders, dt);
	}

	public static JsonObject createMessage(OffsetDateTime scheduledExpeditionDate, OffsetDateTime dueDate, String taxCode, String email, JsonObject payment, JsonArray placeHolders, DateTimeFormatter dt) {
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		
		if(taxCode != null) {
			objectBuilder = objectBuilder.add("taxcode", taxCode);
		}
		
		if(scheduledExpeditionDate != null) {
			objectBuilder = objectBuilder.add("scheduled_expedition_date",  dt.format(scheduledExpeditionDate));
		}
		
		if(payment != null) {
			objectBuilder = objectBuilder.add("payment", payment);
		}
		
		if(placeHolders != null) {
			objectBuilder = objectBuilder.add("placeholders", placeHolders);
		}
		
		if(email != null) {
			objectBuilder = objectBuilder.add("email", email);
		}
		
		if(dueDate != null) {
			objectBuilder = objectBuilder.add("due_date",  dt.format(dueDate));
		}
		
		return objectBuilder.build();
	}

	public static JsonObject createPaymentObject(Long amount, String noticeNumber, Boolean invalidAfterDueDate, String payEETaxCode) {
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		
		if(amount != null) {
			objectBuilder = objectBuilder.add("amount", amount);
		}
		
		if(noticeNumber != null) {
			objectBuilder = objectBuilder.add("notice_number", noticeNumber);
		}
		
		if(invalidAfterDueDate != null) {
			objectBuilder = objectBuilder.add("invalid_after_due_date", invalidAfterDueDate);
		}
		
		if(payEETaxCode != null) {
			objectBuilder = objectBuilder.add("payee_taxcode", payEETaxCode);
		}
		
		return objectBuilder.build();
	}
	
	public static String applyPlaceHolders(String template, Map<String,String> placholders ) {
		
		for (Map.Entry<String, String> entry : placholders.entrySet()) {
			String key = "${" + entry.getKey() + "}";
			String val = entry.getValue();
			
			template = template.replace(key, val);
		}
		
		return template;
	}
	
	public static JsonObject createInvalidDateMessage(String scheduledExpeditionDate, String dueDate, String taxCode, String email, JsonObject payment, JsonArray placeHolders) {
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		
		if(taxCode != null) {
			objectBuilder = objectBuilder.add("taxcode", taxCode);
		}
		
		if(scheduledExpeditionDate != null) {
			objectBuilder = objectBuilder.add("scheduled_expedition_date",  scheduledExpeditionDate);
		}
		
		if(payment != null) {
			objectBuilder = objectBuilder.add("payment", payment);
		}
		
		if(placeHolders != null) {
			objectBuilder = objectBuilder.add("placeholders", placeHolders);
		}
		
		if(email != null) {
			objectBuilder = objectBuilder.add("email", email);
		}
		
		if(dueDate != null) {
			objectBuilder = objectBuilder.add("due_date", dueDate);
		}
		
		return objectBuilder.build();
	}

	public static JsonObject createInvalidPaymentObject(String amount, String noticeNumber, String invalidAfterDueDate, String payEETaxCode) {
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		
		if(amount != null) {
			objectBuilder = objectBuilder.add("amount", amount);
		}
		
		if(noticeNumber != null) {
			objectBuilder = objectBuilder.add("notice_number", noticeNumber);
		}
		
		if(invalidAfterDueDate != null) {
			objectBuilder = objectBuilder.add("invalid_after_due_date", invalidAfterDueDate);
		}
		
		if(payEETaxCode != null) {
			objectBuilder = objectBuilder.add("payee_taxcode", payEETaxCode);
		}
		
		return objectBuilder.build();
	}
}
