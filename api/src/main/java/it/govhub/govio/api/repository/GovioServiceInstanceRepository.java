package it.govhub.govio.api.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.GovioServiceInstanceEntity;

public interface GovioServiceInstanceRepository extends JpaRepositoryImplementation<GovioServiceInstanceEntity, Long> {
	
}