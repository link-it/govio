package it.govio.msgsender.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.msgsender.entity.GovioServiceInstancesEntity;

public interface GovioServiceInstancesRepository extends JpaRepositoryImplementation<GovioServiceInstancesEntity, Long> {

}