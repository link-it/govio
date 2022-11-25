package it.govio.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;

public interface GovioFilesRepository extends JpaRepositoryImplementation<GovioFileEntity, Long> {

	List<GovioFileEntity> findByStatus(Status status);
}
