package it.govio.template;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Template {

	private boolean hasDueDate;
	private boolean hasPayment;
	private String messageBody;
	private String subject;
	private List<Placeholder> placeholders;

}
