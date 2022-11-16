package it.govio.msgverified.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.msgverified.entity.GovioServiceInstanceEntity;


public interface GovioServiceInstancesRepository extends JpaRepositoryImplementation<GovioServiceInstanceEntity, Long> {

}