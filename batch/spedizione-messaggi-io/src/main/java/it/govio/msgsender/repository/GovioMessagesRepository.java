package it.govio.msgsender.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.msgsender.entity.GovioMessageEntity;

public interface GovioMessagesRepository extends JpaRepositoryImplementation<GovioMessageEntity, Long> {
}