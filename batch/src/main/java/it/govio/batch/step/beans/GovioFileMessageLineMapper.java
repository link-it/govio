package it.govio.batch.step.beans;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileMessageEntity;

@Component
public class GovioFileMessageLineMapper implements LineMapper<GovioFileMessageEntity> {
	
	public GovioFileMessageEntity mapLine(String line, int lineNumber) throws Exception {
		return GovioFileMessageEntity.builder()
				.lineNumber(Long.valueOf(lineNumber))
				.lineRecord(line)
				.build();
	}
}