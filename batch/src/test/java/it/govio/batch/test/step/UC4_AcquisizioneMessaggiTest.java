package it.govio.batch.test.step;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.govio.batch.Application;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.step.CsvItemProcessor;
import it.govio.batch.test.utils.GovioMessageBuilder;



@SpringBootTest(classes = Application.class)
public class UC4_AcquisizioneMessaggiTest {
	@Autowired
	CsvItemProcessor processProcessor;
	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

}
