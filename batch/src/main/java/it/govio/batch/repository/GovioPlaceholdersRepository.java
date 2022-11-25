package it.govio.batch.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioPlaceholderEntity;

public interface GovioPlaceholdersRepository extends JpaRepositoryImplementation<GovioPlaceholderEntity, Long> {
}
