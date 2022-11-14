package it.govio.msgverified.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.msgverified.entity.GovioMessageEntity;
import it.govio.msgverified.entity.GovioMessageEntity.Status;


public interface GovioMessagesRepository extends JpaRepositoryImplementation<GovioMessageEntity, Long> {
	
	public Page<GovioMessageEntity> findAllByStatusAndExpeditionDateAndLastUpdateStatusBefore(Status status, LocalDateTime expeditionDate,LocalDateTime last_status_update, Pageable pageable);

	
}
