package it.govhub.govio.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.GovioFileMessageEntity;

public interface FileMessageRepository extends JpaRepositoryImplementation<GovioFileMessageEntity, Long>{

}

