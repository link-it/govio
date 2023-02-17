package it.govhub.govio.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;

public interface TemplatePlaceholderRepository extends JpaRepositoryImplementation<GovioTemplatePlaceholderEntity, Long>{
}