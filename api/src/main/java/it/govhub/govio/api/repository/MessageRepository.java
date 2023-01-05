package it.govhub.govio.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.GovioMessageEntity;

public interface MessageRepository extends JpaRepositoryImplementation<GovioMessageEntity, Long>{
}

