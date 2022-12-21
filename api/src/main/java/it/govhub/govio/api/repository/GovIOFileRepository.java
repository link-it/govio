package it.govhub.govio.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.GovIOFileEntity;
import it.govhub.govio.api.entity.ServiceInstanceEntity;

public interface GovIOFileRepository extends JpaRepositoryImplementation<GovIOFileEntity, Long> {
	
	public Optional<GovIOFileEntity> findByNameAndServiceInstance(String name, ServiceInstanceEntity service);
}
