package it.govio.batch.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;

public interface GovioMessagesRepository extends JpaRepositoryImplementation<GovioMessageEntity, Long> {
	
	public Page<GovioMessageEntity> findAllByStatusInAndScheduledExpeditionDateBefore(List<Status> statuses, LocalDateTime expeditionDate, Pageable pageable);

}