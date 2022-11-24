package it.govio.batch.step.beans;

import java.time.LocalDateTime;
import java.util.Map;

import it.govio.batch.entity.GovioServiceInstanceEntity;
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
	LocalDateTime scheduledExpeditionDate;
	
	long rowNumber;
	String rawData;
	
	Map<String, String> placeholderValues;
	
	GovioServiceInstanceEntity serviceInstance;
}
