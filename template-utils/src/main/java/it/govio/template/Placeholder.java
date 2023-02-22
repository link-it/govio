package it.govio.template;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Placeholder {
	
	public enum Type { DATE, DATETIME, STRING }
	
	private boolean mandatory;
	private String name;
	private int position;
	private Type type;
	private String pattern;
}