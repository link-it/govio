package it.govhub.govio.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.ServiceInstanceEntity;

public interface GovioFileRepository extends JpaRepositoryImplementation<GovioFileEntity, Long> {
	
	public Optional<GovioFileEntity> findByNameAndServiceInstance(String name, ServiceInstanceEntity service);
}