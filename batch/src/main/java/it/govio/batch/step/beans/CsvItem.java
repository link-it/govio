package it.govio.batch.step.beans;

import java.util.Properties;

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
	GovioServiceInstanceEntity govioServiceInstance;
	long rowNumber;
	String rawData;
	String taxcode;
	String scheduledDate;
	String noticeNumber;
	String amount;
	String invalidAfterDueDate;
	Properties placeholderValues;
}
