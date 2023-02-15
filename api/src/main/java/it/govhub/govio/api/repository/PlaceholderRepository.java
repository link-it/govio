package it.govhub.govio.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.GovioPlaceholderEntity;

public interface PlaceholderRepository extends JpaRepositoryImplementation<GovioPlaceholderEntity, Long> {
}
