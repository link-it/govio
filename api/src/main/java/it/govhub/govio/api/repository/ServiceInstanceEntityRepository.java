package it.govhub.govio.api.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.ServiceInstanceEntity;

public interface ServiceInstanceEntityRepository extends JpaRepositoryImplementation<ServiceInstanceEntity, Long> {
	
	@Transactional
	public Optional<ServiceInstanceEntity> findByService_IdAndOrganization_Id(Long serviceId, Long organizationId);
}