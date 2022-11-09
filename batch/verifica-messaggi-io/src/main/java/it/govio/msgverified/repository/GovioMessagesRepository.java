package it.govio.msgverified.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.msgverified.entity.GovioMessageEntity;
import it.govio.msgverified.entity.GovioMessageEntity.Status;


public interface GovioMessagesRepository extends JpaRepositoryImplementation<GovioMessageEntity, Long> {
	
	// TODO aggiungere il metodo per cercare i record con expedition_date o last_status_update (quella più recente) decorsa da più di un X minuti (da configurazione)
	public Page<GovioMessageEntity> findAllByStatus(Status status, Pageable pageable);
	
}
