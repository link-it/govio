package it.govio.template;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class Placeholder {
	
	public enum Type { DATE, DATETIME, STRING }
	
	private boolean mandatory;
	private String name;
	private int position;
	private Type type;
	private String pattern;
}