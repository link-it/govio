package it.govio.template;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;


@Data
@Builder
@Jacksonized
public class Template {

	private boolean hasDueDate;
	private boolean hasPayment;
	private String messageBody;
	private String subject;
	private List<Placeholder> placeholders;

}
