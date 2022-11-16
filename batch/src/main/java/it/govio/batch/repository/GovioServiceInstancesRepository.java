package it.govio.batch.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioServiceInstanceEntity;

public interface GovioServiceInstancesRepository extends JpaRepositoryImplementation<GovioServiceInstanceEntity, Long> {

}