package it.govio.batch.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.batch.entity.GovioPlaceholderEntity;

public interface GovioPlaceholdersRepository extends JpaRepositoryImplementation<GovioPlaceholderEntity, Long> {
}
