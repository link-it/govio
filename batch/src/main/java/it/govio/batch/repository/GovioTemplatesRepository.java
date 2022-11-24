package it.govio.batch.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioTemplateEntity;

public interface GovioTemplatesRepository extends JpaRepositoryImplementation<GovioTemplateEntity, Long> {

}