package it.govhub.govio.api.repository;

import it.govhub.govio.api.entity.GovioMessageEntity.Status;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FileMessagesStatusCount {
	Status status;
	Long count;
	
	public FileMessagesStatusCount(Status status, Long count) {
		this.status = status;
		this.count = count;
	}
}