package it.govio.batch.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioFileMessageEntity;

public interface GovioFileMessagesRepository extends JpaRepositoryImplementation<GovioFileMessageEntity, Long> {

	@Query(value = "SELECT m FROM GovioFileMessageEntity m JOIN GovioFileEntity f JOIN FETCH GovioServiceInstanceEntity ")
	public Page<GovioFileMessageEntity> findAllProcessingFileMessage(Pageable pageable);

}
