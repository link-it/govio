package it.govhub.govio.api.repository.model;

import it.govhub.govio.api.entity.GovioMessageEntity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MessageStatusCount {
	
    private Status status;
    private Long count;

}
