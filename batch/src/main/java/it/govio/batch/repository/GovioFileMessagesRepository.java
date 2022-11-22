package it.govio.batch.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioFileMessageEntity;

public interface GovioFileMessagesRepository extends JpaRepositoryImplementation<GovioFileMessageEntity, Long> {


}
