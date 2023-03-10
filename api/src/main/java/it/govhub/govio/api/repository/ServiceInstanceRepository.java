package it.govhub.govio.api.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.GovioServiceInstanceEntity;

public interface ServiceInstanceRepository extends JpaRepositoryImplementation<GovioServiceInstanceEntity, Long> {
	
	@Transactional
     public Optional<GovioServiceInstanceEntity> findByService_IdAndOrganization_Id(Long serviceId, Long organizationId);
	
}