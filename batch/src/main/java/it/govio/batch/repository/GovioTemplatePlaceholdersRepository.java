package it.govio.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioTemplatePlaceholderEntity;

public interface GovioTemplatePlaceholdersRepository extends JpaRepositoryImplementation<GovioTemplatePlaceholderEntity, Long> {
	List<GovioTemplatePlaceholderEntity> findByGovioTemplateId(long idTemplate);
}

