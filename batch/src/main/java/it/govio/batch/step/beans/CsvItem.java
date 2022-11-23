package it.govio.batch.step.beans;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvItem {
	
	String taxcode;
	LocalDateTime scheduledDate;
	
	long rowNumber;
	String rawData;
	
	Map<String, String> placeholderValues;
	
}
