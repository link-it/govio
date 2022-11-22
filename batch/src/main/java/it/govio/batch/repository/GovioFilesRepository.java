package it.govio.batch.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioFileEntity;

public interface GovioFilesRepository extends JpaRepositoryImplementation<GovioFileEntity, Long> {

}
