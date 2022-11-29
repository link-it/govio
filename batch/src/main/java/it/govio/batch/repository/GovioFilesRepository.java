package it.govio.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;

public interface GovioFilesRepository extends JpaRepositoryImplementation<GovioFileEntity, Long> {

	List<GovioFileEntity> findByStatus(Status status);
	
    @Modifying
    @Query("UPDATE GovioFileEntity f SET f.status = :newStatus WHERE f.status = :oldStatus")
    int updateAllStatus(@Param("oldStatus") Status oldStatus, @Param("newStatus") Status newStatus);
}
