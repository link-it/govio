package it.govio.msgsender.repository;


import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govio.msgsender.entity.GovioMessagesEntity;

public interface GovioMessagesRepository extends JpaRepositoryImplementation<GovioMessagesEntity, Long> {
}