package it.govio.msgsender.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.entity.GovioMessageEntity.Status;

public interface GovioMessagesRepository extends JpaRepositoryImplementation<GovioMessageEntity, Long> {
	
	public Page<GovioMessageEntity> findAllByStatusAndScheduledExpeditionDateBefore(Status status, LocalDateTime expeditionDate, Pageable pageable);
}