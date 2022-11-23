package it.govio.batch.step.beans;

import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter	
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GovioMessage {
	
	GovioMessageEntity govioMessageEntity;
	GovioFileMessageEntity govioFileMessageEntity;

}
