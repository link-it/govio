package it.govhub.govio.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.TemplateEntity;

public interface TemplateRepository extends JpaRepositoryImplementation<TemplateEntity, Long>{

}
