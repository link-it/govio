package it.govio.msgsender.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.repository.GovioMessagesRepository;

@Service
public class MsgsenderGovioService {
		
		@Autowired
		private GovioMessagesRepository msgsenderGovioRepository;
		
		@Transactional
		public void saveMessageGovio(GovioMessageEntity govioMessages) {
			this.msgsenderGovioRepository.save(govioMessages);
		}
	}